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

package controllers.vatEligibility

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatEligibility.HaveNinoForm
import models.YesOrNo
import models.api.VatServiceEligibility
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}

import scala.concurrent.Future


class ServiceCriteriaQuestionsController @Inject()(ds: CommonPlayDependencies)
                                                  (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  val form: Form[YesOrNo] = HaveNinoForm.form

  def show(question: String): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[VatServiceEligibility].fold(form)(eligibility =>
      form.fill(YesOrNo(question, VatServiceEligibility.getValue(question, eligibility)))

    ).map(f =>
      question match {
        case "haveNino" => Ok(views.html.pages.vatEligibility.have_nino(f))
      }
    )
  )

  def submit(question: String): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    HaveNinoForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vatEligibility.have_nino(formWithErrors)))
      }, {
        data: YesOrNo => {
          for {
            vatEligibility <- viewModel[VatServiceEligibility].getOrElse(VatServiceEligibility())
            updatedVatEligibility <- Future.successful(VatServiceEligibility.setValue(question, data.answer, vatEligibility))
            s4LUpdateVatEligibility <- s4LService.saveForm[VatServiceEligibility](updatedVatEligibility)
          } yield question match {
            case "haveNino" => Redirect(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
          }
        }
      })
  )

}


