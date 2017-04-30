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

import cats.data.OptionT
import connectors.KeystoreConnector
import controllers.vatEligibility.{routes => eligibilityRoutes}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatEligibility.ServiceCriteriaFormFactory
import models.YesOrNoQuestion
import models.api.EligibilityQuestion._
import models.api.{EligibilityQuestion, VatServiceEligibility}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call}
import services.{RegistrationService, S4LService}


class ServiceCriteriaQuestionsController @Inject()(ds: CommonPlayDependencies, formFactory: ServiceCriteriaFormFactory)
                                                  (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val keystore = KeystoreConnector

  private def nextQuestion(question: EligibilityQuestion): Call = question match {
    case HaveNinoQuestion => eligibilityRoutes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name)
    case DoingBusinessAbroadQuestion => controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show()
  }

  private val INELIGIBILITY_REASON: String = "ineligibility-reason"


  def show(q: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val question = EligibilityQuestion(q)
    val form: Form[YesOrNoQuestion] = formFactory.form(question.name)

    viewModel[VatServiceEligibility]
      .flatMap(e => OptionT.fromOption(e.getAnswer(question)))
      .fold(form)(answer => form.fill(YesOrNoQuestion(question.name, answer)))
      .map(f => Ok(question match {
        case HaveNinoQuestion => views.html.pages.vatEligibility.have_nino(f)
        case DoingBusinessAbroadQuestion => views.html.pages.vatEligibility.doing_business_abroad(f)
      }))
  })

  import common.ConditionalFlatMap._

  def submit(q: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val question = EligibilityQuestion(q)
    formFactory.form(question.name).bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatEligibility.have_nino(badForm)).pure,
      (data: YesOrNoQuestion) =>
        for {
          vatEligibility <- viewModel[VatServiceEligibility].getOrElse(VatServiceEligibility())
          _ <- s4LService.saveForm(vatEligibility.setAnswer(question, data.answer))
          exit = data.answer == question.exitAnswer
          _ <- keystore.cache(INELIGIBILITY_REASON, question.name) onlyIf exit
        } yield Redirect(
          if (exit) eligibilityRoutes.ServiceCriteriaQuestionsController.ineligible() else nextQuestion(question)
        )
    )
  })

  def ineligible(): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    OptionT(keystore.fetchAndGet[String](INELIGIBILITY_REASON)).getOrElse("").map {
      failedQuestion => Ok(views.html.pages.vatEligibility.ineligible(s => if (s != failedQuestion) "hidden" else ""))
    })

}
