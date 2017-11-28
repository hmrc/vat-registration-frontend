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
  import forms.sicAndCompliance.financial.InvestmentFundManagementForm
  import models.S4LVatSicAndCompliance
  import models.S4LVatSicAndCompliance.dropFromInvFundManagement
  import models.view.sicAndCompliance.financial.InvestmentFundManagement
  import play.api.mvc.{Action, AnyContent}
  import services.{CommonService, RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class InvestmentFundManagementController @Inject()(ds: CommonPlayDependencies,
                                                     val keystoreConnector: KeystoreConnect,
                                                     val authConnector: AuthConnector,
                                                     implicit val s4lService: S4LService,
                                                     implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with CommonService with SessionProfile {

    val form = InvestmentFundManagementForm.form

    import cats.syntax.flatMap._

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[InvestmentFundManagement]().fold(form)(form.fill)
                .map(f => Ok(features.sicAndCompliance.views.html.financial.investment_fund_management(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.sicAndCompliance.views.html.financial.investment_fund_management(badForm)).pure,
                data => save(data).map(_ => data.yesNo).ifM(
                  ifTrue = controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show().pure,
                  ifFalse = for {
                    container <- s4lContainer[S4LVatSicAndCompliance]()
                    _         <- s4lService.save(dropFromInvFundManagement(container))
                    _         <- vrs.submitSicAndCompliance
                  } yield controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()
                ).map(Redirect))
            }
          }
    }

  }

}

package forms.sicAndCompliance.financial {

  import forms.FormValidation.missingBooleanFieldMapping
  import models.view.sicAndCompliance.financial.InvestmentFundManagement
  import play.api.data.Form
  import play.api.data.Forms._

  object InvestmentFundManagementForm {
    val RADIO_YES_NO: String = "investmentFundManagementRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("investmentFundManagement")
      )(InvestmentFundManagement.apply)(InvestmentFundManagement.unapply)
    )

  }

}

