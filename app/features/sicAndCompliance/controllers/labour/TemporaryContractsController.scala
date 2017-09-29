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

package controllers.sicAndCompliance.labour {

  import javax.inject.Inject

  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import controllers.sicAndCompliance.ComplianceExitController
  import forms.sicAndCompliance.labour.TemporaryContractsForm
  import models.S4LVatSicAndCompliance
  import models.S4LVatSicAndCompliance.dropFromTemporaryContracts
  import models.view.sicAndCompliance.labour.TemporaryContracts
  import play.api.mvc.{Action, AnyContent}
  import services.{CommonService, S4LService, SessionProfile, VatRegistrationService}

  class TemporaryContractsController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4lService: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with CommonService with SessionProfile {

    import cats.syntax.flatMap._

    val form = TemporaryContractsForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[TemporaryContracts]().fold(form)(form.fill)
              .map(f => Ok(features.sicAndCompliance.views.html.labour.temporary_contracts(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.sicAndCompliance.views.html.labour.temporary_contracts(badForm)).pure,
              data => save(data).map(_ => data.yesNo == TemporaryContracts.TEMP_CONTRACTS_YES).ifM(
                ifTrue = controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show().pure,
                ifFalse = for {
                  container <- s4lContainer[S4LVatSicAndCompliance]()
                  _ <- s4lService.save(dropFromTemporaryContracts(container))
                  _ <- vrs.submitSicAndCompliance()
                } yield controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()
              ).map(Redirect))
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