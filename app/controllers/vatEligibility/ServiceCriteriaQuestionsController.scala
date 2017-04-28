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
import forms.vatEligibility.ServiceCriteriaFormFactory
import models.YesOrNoQuestion
import models.api.VatServiceEligibility
import models.api.VatServiceEligibility._
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}

import scala.concurrent.Future


class ServiceCriteriaQuestionsController @Inject()(ds: CommonPlayDependencies, formFactory: ServiceCriteriaFormFactory)
                                                  (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._


  def show(question: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val form: Form[YesOrNoQuestion] = formFactory.form(question)

    viewModel[VatServiceEligibility].fold(form)(eligibility =>
      form.fill(YesOrNoQuestion(question, VatServiceEligibility.getValue(question, eligibility)))

    ).map(f =>
      question match {
        case HAVE_NINO => Ok(views.html.pages.vatEligibility.have_nino(f))
        case DOING_BUSINESS_ABROAD => Ok(views.html.pages.vatEligibility.doing_business_abroad(f))
      }
    )
  })


  def submit(question: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val form: Form[YesOrNoQuestion] = formFactory.form(question)

    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vatEligibility.have_nino(formWithErrors)))
      }, {
        data: YesOrNoQuestion => {
          for {
            vatEligibility <- viewModel[VatServiceEligibility].getOrElse(VatServiceEligibility())
            updatedVatEligibility <- Future.successful(VatServiceEligibility.setValue(question, data.answer, vatEligibility))
            s4LUpdateVatEligibility <- s4LService.saveForm[VatServiceEligibility](updatedVatEligibility)
          } yield question match {
            case HAVE_NINO => Redirect(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DOING_BUSINESS_ABROAD))
            case DOING_BUSINESS_ABROAD => Redirect(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
          }
        }
      }
    )
  })


}


