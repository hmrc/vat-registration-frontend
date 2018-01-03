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

package models.view.vatTradingDetails.vatChoice {

  import models.api.VatEligibilityChoice.NECESSITY_VOLUNTARY
  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatEligibilityChoice, ViewModelFormat}
  import play.api.libs.json.Json

  case class VoluntaryRegistration(yesNo: String)

  object VoluntaryRegistration {

    val REGISTER_YES = "REGISTER_YES"
    val REGISTER_NO = "REGISTER_NO"

    val yes = VoluntaryRegistration(REGISTER_YES)
    val no = VoluntaryRegistration(REGISTER_NO)

    val valid = (item: String) => List(REGISTER_YES, REGISTER_NO).contains(item.toUpperCase)

    implicit val format = Json.format[VoluntaryRegistration]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatEligibilityChoice) => group.voluntaryRegistration,
      updateF = (c: VoluntaryRegistration, g: Option[S4LVatEligibilityChoice]) =>
        g.getOrElse(S4LVatEligibilityChoice().copy(voluntaryRegistration = Some(c)))
    )

    implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
      vs.vatServiceEligibility.flatMap(a => a.vatEligibilityChoice.map(a => a.necessity)).collect {
        case NECESSITY_VOLUNTARY => VoluntaryRegistration(REGISTER_YES)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatChoice.VoluntaryRegistrationForm
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
  import play.api.mvc._
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class VoluntaryRegistrationController @Inject()(ds: CommonPlayDependencies,
                                                  val keystoreConnector: KeystoreConnect,
                                                  val authConnector: AuthConnector,
                                                  implicit val s4l: S4LService,
                                                  implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val form = VoluntaryRegistrationForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[VoluntaryRegistration]().fold(form)(form.fill)
              .map(f => Ok(features.tradingDetails.views.html.vatChoice.voluntary_registration(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.tradingDetails.views.html.vatChoice.voluntary_registration(badForm)).pure,
              goodForm => (VoluntaryRegistration.REGISTER_YES == goodForm.yesNo).pure.ifM(
                save(goodForm).map(_ => controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationReasonController.show()),
                s4l.clear.flatMap(_ => vrs.deleteVatScheme).map(_ => controllers.routes.WelcomeController.show())
              ).map(Redirect))
          }
    }
  }
}

package forms.vatTradingDetails.vatChoice {

  import forms.FormValidation.textMapping
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object VoluntaryRegistrationForm {
    val RADIO_YES_NO: String = "voluntaryRegistrationRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("voluntary.registration").verifying(VoluntaryRegistration.valid)
      )(VoluntaryRegistration.apply)(VoluntaryRegistration.unapply)
    )
  }
}
