/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.sicAndCompliance {

  import javax.inject.Inject

  import cats.data.OptionT
  import controllers.CommonPlayDependencies
  import forms.sicAndCompliance.MainBusinessActivityForm
  import models.ModelKeys._
  import models.S4LFlatRateScheme
  import models.api.SicCode
  import models.view.sicAndCompliance.MainBusinessActivityView
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}
  import uk.gov.hmrc.play.http.HeaderCarrier

  import scala.concurrent.Future


  class MainBusinessActivityController @Inject()(ds: CommonPlayDependencies)
                                                (implicit s4l: S4LService,
                                                 vrs: VatRegistrationService)
    extends ComplianceExitController(ds) with SessionProfile {

    import cats.syntax.cartesian._
    import common.ConditionalFlatMap._

    private val form = MainBusinessActivityForm.form

    private def fetchSicCodeList()(implicit hc: HeaderCarrier): Future[List[SicCode]] =
      OptionT(keystoreConnector.fetchAndGet[List[SicCode]](SIC_CODES_KEY)).getOrElse(List.empty[SicCode])

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              for {
                sicCodeList <- fetchSicCodeList
                res <- viewModel[MainBusinessActivityView]().fold(form)(form.fill)
              } yield Ok(features.sicAndCompliance.views.html.main_business_activity(res, sicCodeList))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => fetchSicCodeList().map(sicCodeList =>
                  BadRequest(features.sicAndCompliance.views.html.main_business_activity(badForm, sicCodeList))),
                view => fetchSicCodeList().flatMap(sicCodeList =>
                  sicCodeList.find(_.id == view.id).fold(
                    BadRequest(features.sicAndCompliance.views.html.main_business_activity(form, sicCodeList)).pure
                  )(selected => for {
                    mainSic <- viewModel[MainBusinessActivityView]().value
                    selectionChanged = mainSic.exists(_.id != selected.id)
                    _ <- s4l.save(S4LFlatRateScheme()).flatMap(_ => vrs.submitVatFlatRateScheme()) onlyIf selectionChanged
                    _ <- save(MainBusinessActivityView(selected))
                    result <- selectNextPage(sicCodeList)
                  } yield result)))
            }
          }
    }

    def redirectToNext: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              fetchSicCodeList().flatMap {
                case Nil => selectNextPage(Nil)
                case sicCodeList@(head :: _) => save(MainBusinessActivityView(head)).flatMap(_ => selectNextPage(sicCodeList))
              }
            }
          }
    }

  }

}

package forms.sicAndCompliance {

  import forms.FormValidation.textMapping
  import models.view.sicAndCompliance.MainBusinessActivityView
  import play.api.data.Form
  import play.api.data.Forms.mapping


  object MainBusinessActivityForm {


    val NAME_ID: String = "mainBusinessActivityRadio"

    val form = Form(
      mapping(
        NAME_ID -> textMapping()("mainBusinessActivity")
      )(MainBusinessActivityView(_))(view => Option(view.id))

    )
  }

}
