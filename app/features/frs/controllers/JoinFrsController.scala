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

package models.view.frs {

  import models.ApiModelTransformer
  import models.api.{VatFlatRateScheme, VatScheme}
  import play.api.libs.json.Json

  case class JoinFrsView(selection: Boolean)

  object JoinFrsView {
    implicit val format = Json.format[JoinFrsView]

    def from(vatFlatRateScheme: VatFlatRateScheme): JoinFrsView = {
      JoinFrsView(vatFlatRateScheme.joinFrs)
    }

    implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
      vs.vatFlatRateScheme.map(_.joinFrs).map(JoinFrsView(_))
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import config.FrontendAuthConnector
  import controllers.VatRegistrationControllerNoAux
  import models.{S4LFlatRateScheme, S4LKey, S4LModelTransformer}
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
  import connectors.KeystoreConnector
  import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
  import models.view.frs.JoinFrsView
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}

  import scala.concurrent.Future

  class JoinFrsControllerImpl @Inject()(formFactory: YesOrNoFormFactory,
                                    val service: VatRegistrationService,
                                    val messagesApi: MessagesApi) extends JoinFrsController {

    val authConnector: AuthConnector = FrontendAuthConnector
    val keystoreConnector: KeystoreConnector = KeystoreConnector
    val form: Form[YesOrNoAnswer] = formFactory.form("joinFrs")("frs.join")
  }

  trait JoinFrsController extends VatRegistrationControllerNoAux with SessionProfile {

    val service: VatRegistrationService
    val form: Form[YesOrNoAnswer]

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            service.fetchFlatRateScheme map { flatRateScheme =>
              val joinFrsForm = flatRateScheme.joinFrs match {
                case Some(joinFrs) => form.fill(YesOrNoAnswer(joinFrs.selection))
                case None => form
              }
              Ok(features.frs.views.html.frs_join(joinFrsForm))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
              joiningFRS => {
                service.saveJoinFRS(JoinFrsView(joiningFRS.answer)) map { _ =>
                  if (joiningFRS.answer) {
                    Redirect(controllers.frs.routes.AnnualCostsInclusiveController.show())
                  } else {
                    Redirect(controllers.routes.SummaryController.show())
                  }
                }
              }
            )
          }
    }
  }
}
