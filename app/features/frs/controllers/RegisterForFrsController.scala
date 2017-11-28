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

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LFlatRateScheme, ViewModelFormat}
  import play.api.libs.json.Json

  final case class RegisterForFrsView(selection: Boolean)

  object RegisterForFrsView {
    implicit val format = Json.format[RegisterForFrsView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LFlatRateScheme) => group.registerForFrs,
      updateF = (c: RegisterForFrsView, g: Option[S4LFlatRateScheme]) =>
        g.getOrElse(S4LFlatRateScheme()).copy(registerForFrs = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[RegisterForFrsView] { (vs: VatScheme) =>
      vs.vatFlatRateScheme.flatMap(answers => answers.doYouWantToUseThisRate.map(RegisterForFrsView.apply))
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import connectors.KeystoreConnect
  import controllers.VatRegistrationControllerNoAux
  import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  class RegisterForFrsControllerImpl @Inject()(val messagesApi: MessagesApi,
                                               val service: RegistrationService,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnect) extends RegisterForFrsController

  trait RegisterForFrsController extends VatRegistrationControllerNoAux with SessionProfile {

    val formFactory: YesOrNoFormFactory = YesOrNoFormFactory
    val service: RegistrationService

    val form: Form[YesOrNoAnswer] = formFactory.form("registerForFrs")("frs.registerFor")

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              // TODO no fetch from S4L / backend
              Future.successful(Ok(features.frs.views.html.frs_register_for(form)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => Future.successful(BadRequest(features.frs.views.html.frs_register_for(badForm))),
                view =>
                  service.saveRegisterForFRS(view.answer) map { _ =>
                    if (view.answer) {
                      Redirect(controllers.frs.routes.FrsStartDateController.show())
                    } else {
                      Redirect(controllers.routes.SummaryController.show())
                    }
                  }
              )
            }
          }
    }
  }
}
