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

package controllers.sicAndCompliance.financial {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.sicAndCompliance.financial.ChargeFeesForm
  import models.view.sicAndCompliance.financial.ChargeFees
  import play.api.data.Form
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class ChargeFeesController @Inject()(ds: CommonPlayDependencies,
                                       val keystoreConnector: KeystoreConnect,
                                       val authConnector: AuthConnector,
                                       implicit val s4LService: S4LService,
                                       implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form: Form[ChargeFees] = ChargeFeesForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[ChargeFees]().fold(form)(form.fill)
                .map(f => Ok(features.sicAndCompliance.views.html.financial.charge_fees(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.sicAndCompliance.views.html.financial.charge_fees(badForm)).pure,
                view => save(view).map(_ =>
                  Redirect(controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show())))
            }
          }
    }
  }
}

package forms.sicAndCompliance.financial {

  import forms.FormValidation.missingBooleanFieldMapping
  import models.view.sicAndCompliance.financial.ChargeFees
  import play.api.data.Form
  import play.api.data.Forms._

  object ChargeFeesForm {
    val RADIO_YES_NO: String = "chargeFeesRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("chargeFees")
      )(ChargeFees.apply)(ChargeFees.unapply)
    )
  }
}
