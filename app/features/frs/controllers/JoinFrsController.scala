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

package models.view.frs {

  import models.ApiModelTransformer
  import models.api.VatScheme
  import play.api.libs.json.Json

  case class JoinFrsView(selection: Boolean)

  object JoinFrsView {
    implicit val format = Json.format[JoinFrsView]

    implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
      vs.vatFlatRateScheme.map(_.joinFrs).map(JoinFrsView(_))
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import connectors.KeystoreConnect
  import controllers.VatRegistrationControllerNoAux
  import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
  import models.view.frs.JoinFrsView
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  class JoinFrsControllerImpl @Inject()(formFactory: YesOrNoFormFactory,
                                        val service: RegistrationService,
                                        val messagesApi: MessagesApi,
                                        val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnect) extends JoinFrsController {
    val form: Form[YesOrNoAnswer] = formFactory.form("joinFrs")("frs.join")
  }

  trait JoinFrsController extends VatRegistrationControllerNoAux with SessionProfile {

    val service: RegistrationService
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
