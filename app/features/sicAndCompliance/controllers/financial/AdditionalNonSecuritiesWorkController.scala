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

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import controllers.sicAndCompliance.ComplianceExitController
import forms.sicAndCompliance.financial.AdditionalNonSecuritiesWorkForm
import models.S4LVatSicAndCompliance
import models.S4LVatSicAndCompliance.dropFromAddNonSecurities
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, RegistrationService, S4LService, SessionProfile}


class AdditionalNonSecuritiesWorkController @Inject()(ds: CommonPlayDependencies)
                                                     (implicit s4lService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  val form = AdditionalNonSecuritiesWorkForm.form

  import cats.syntax.flatMap._

  def show: Action[AnyContent] = authorised.async{
    implicit user =>
      implicit request =>
        withCurrentProfile{ implicit profile =>
          viewModel[AdditionalNonSecuritiesWork]().fold(form)(form.fill)
            .map(f => Ok(features.sicAndCompliance.views.html.financial.additional_non_securities_work(f)))
        }
  }

  def submit: Action[AnyContent] = authorised.async{
    implicit user =>
      implicit request =>
        withCurrentProfile{ implicit profile =>
          form.bindFromRequest().fold(
            badForm => BadRequest(features.sicAndCompliance.views.html.financial.additional_non_securities_work(badForm)).pure,
            view => save(view).map(_ => view.yesNo).ifM(
              ifTrue = for {
                container <- s4lContainer[S4LVatSicAndCompliance]()
                _ <- s4lService.save(dropFromAddNonSecurities(container))
                _ <- vrs.submitSicAndCompliance()
              } yield controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show(),
              ifFalse = controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show().pure
            ).map(Redirect))
        }
  }

  }
}

package forms.sicAndCompliance.financial {

  import forms.FormValidation.missingBooleanFieldMapping
  import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
  import play.api.data.Form
  import play.api.data.Forms._

  object AdditionalNonSecuritiesWorkForm {
    val RADIO_YES_NO: String = "additionalNonSecuritiesWorkRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("additionalNonSecuritiesWork")
      )(AdditionalNonSecuritiesWork.apply)(AdditionalNonSecuritiesWork.unapply)
    )

  }

}