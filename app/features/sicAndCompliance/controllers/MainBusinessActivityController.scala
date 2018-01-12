/*
 * Copyright 2018 HM Revenue & Customs
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


  import javax.inject.{Inject, Singleton}

  import cats.data.OptionT
  import connectors.KeystoreConnect
  import controllers.CommonPlayDependencies
  import features.sicAndCompliance.services.SicAndComplianceService
  import forms.sicAndCompliance.MainBusinessActivityForm
  import models.ModelKeys._
  import models.S4LFlatRateScheme
  import models.api.SicCode
  import models.view.sicAndCompliance.MainBusinessActivityView
  import play.api.mvc.{Action, AnyContent}
  import services.{FlatRateService, RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  @Singleton
  class MainBusinessActivityController @Inject()(ds: CommonPlayDependencies,
                                                 val keystoreConnector: KeystoreConnect,
                                                 override val authConnector: AuthConnector,
                                                 implicit val s4l: S4LService,
                                                 override val sicAndCompService: SicAndComplianceService,
                                                 override implicit val vrs: RegistrationService)
    extends ComplianceExitController(ds, authConnector, vrs, sicAndCompService, s4l) with SessionProfile {

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
                res <- sicAndCompService.getSicAndCompliance.map(a => a.mainBusinessActivity.fold(form)(form.fill))
              } yield Ok(features.sicAndCompliance.views.html.main_business_activity(res, sicCodeList))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              fetchSicCodeList flatMap { sicCodeList =>
                form.bindFromRequest().fold(
                  badForm => Future.successful(BadRequest(features.sicAndCompliance.views.html.main_business_activity(badForm, sicCodeList))),
                  data => sicCodeList.find(_.id == data.id).fold(
                    Future.successful(BadRequest(features.sicAndCompliance.views.html.main_business_activity(form, sicCodeList)))
                  )(selected => sicAndCompService.saveMainBusinessActivity(selected) map { _ =>
                    if (sicAndCompService.needComplianceQuestions(sicCodeList)) {
                      controllers.sicAndCompliance.routes.ComplianceIntroductionController.show()
                    } else {
                      features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView()
                    }
                  } map Redirect)
                )
              }
            }
          }
    }

//    def redirectToNext: Action[AnyContent] = authorised.async {
//      implicit user =>
//        implicit request =>
//          withCurrentProfile { implicit profile =>
//            ivPassedCheck {
//              fetchSicCodeList().flatMap { sicCodeList =>
//                if (!sicAndCompService.needComplianceQuestions(sicCodeList)) {
//                  Future.successful(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
//                } else {
//                  sicAndCompService.updateSicAndCompliance(MainBusinessActivityView(sicCodeList.head)) map {
//                    _ => controllers.sicAndCompliance.routes.ComplianceIntroductionController.show()
//                  }
//                }
//              } map Redirect
//            }
//          }
//    }
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
