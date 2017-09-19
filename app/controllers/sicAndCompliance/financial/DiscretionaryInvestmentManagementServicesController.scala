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

package controllers.sicAndCompliance.financial

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import controllers.sicAndCompliance.ComplianceExitController
import forms.sicAndCompliance.financial.DiscretionaryInvestmentManagementServicesForm
import models.S4LVatSicAndCompliance
import models.S4LVatSicAndCompliance.dropFromDiscInvManServices
import models.view.sicAndCompliance.financial.DiscretionaryInvestmentManagementServices
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, RegistrationService, S4LService, SessionProfile}


class DiscretionaryInvestmentManagementServicesController @Inject()(ds: CommonPlayDependencies)
                                                                   (implicit s4lService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile{

  import cats.syntax.flatMap._

  val form: Form[DiscretionaryInvestmentManagementServices] = DiscretionaryInvestmentManagementServicesForm.form

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          viewModel[DiscretionaryInvestmentManagementServices]().fold(form)(form.fill)
            .map(f => Ok(views.html.pages.sicAndCompliance.financial.discretionary_investment_management_services(f)))
        }
  }
  def submit: Action[AnyContent] = authorised.async{
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          form.bindFromRequest().fold(
            badForm => BadRequest(views.html.pages.sicAndCompliance.financial.discretionary_investment_management_services(badForm)).pure,
            view => save(view).map(_ => view.yesNo).ifM(
              ifTrue = for {
                container <- s4lContainer[S4LVatSicAndCompliance]()
                _ <- s4lService.save(dropFromDiscInvManServices(container))
                _ <- vrs.submitSicAndCompliance()
              } yield controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show(),
              ifFalse = controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show().pure
            ).map(Redirect))
        }
  }

}
