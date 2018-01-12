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

package controllers.sicAndCompliance.labour {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import features.sicAndCompliance.services.SicAndComplianceService
  import forms.sicAndCompliance.labour.TemporaryContractsForm
  import models.S4LVatSicAndCompliance
  import models.S4LVatSicAndCompliance.dropFromTemporaryContracts
  import models.view.sicAndCompliance.labour.TemporaryContracts
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class TemporaryContractsController @Inject()(ds: CommonPlayDependencies,
                                               val keystoreConnector: KeystoreConnect,
                                               val authConnector: AuthConnector,
                                               implicit val s4lService: S4LService,
                                               val sicAndCompService: SicAndComplianceService,
                                               implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val form = TemporaryContractsForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              sicAndCompService.getSicAndCompliance.map { sicAndComp =>
                val formFilled = sicAndComp.temporaryContracts.fold(form)(form.fill)
                Ok(features.sicAndCompliance.views.html.labour.temporary_contracts(formFilled))
              }

            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.sicAndCompliance.views.html.labour.temporary_contracts(badForm)).pure,
                data => sicAndCompService.updateSicAndCompliance(data) map { _ =>
                  if(data.yesNo == TemporaryContracts.TEMP_CONTRACTS_YES) {
                    controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show()
                  } else {
                    controllers.routes.TradingDetailsController.euGoodsPage()
                  }
                } map Redirect
              )
            }
          }
    }

  }

}

package forms.sicAndCompliance.labour {

  import forms.FormValidation.textMapping
  import models.view.sicAndCompliance.labour.TemporaryContracts
  import play.api.data.Form
  import play.api.data.Forms._

  object TemporaryContractsForm {
    val RADIO_YES_NO: String = "temporaryContractsRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("labourCompliance.temporaryContracts").verifying(TemporaryContracts.valid)
      )(TemporaryContracts.apply)(TemporaryContracts.unapply)
    )

  }

}
