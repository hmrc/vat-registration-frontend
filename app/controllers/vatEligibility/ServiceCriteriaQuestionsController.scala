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
import play.api.mvc.{Action, AnyContent, Call, Request}
import services.{RegistrationService, S4LService}

import scala.concurrent.ExecutionContext.Implicits.global


class ServiceCriteriaQuestionsController @Inject()(ds: CommonPlayDependencies, formFactory: ServiceCriteriaFormFactory)
                                                  (implicit s4LService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  // $COVERAGE-OFF$
  lazy val keystore: KeystoreConnector = KeystoreConnector
  // $COVERAGE-ON$

  val INELIGIBILITY_REASON_KEY: String = "ineligibility-reason"

  private def nextQuestion(question: EligibilityQuestion): Call = question match {
    case HaveNinoQuestion => eligibilityRoutes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name)
    case DoingBusinessAbroadQuestion => eligibilityRoutes.ServiceCriteriaQuestionsController.show(DoAnyApplyToYouQuestion.name)
    case DoAnyApplyToYouQuestion => eligibilityRoutes.ServiceCriteriaQuestionsController.show(ApplyingForAnyOfQuestion.name)
    case ApplyingForAnyOfQuestion => eligibilityRoutes.ServiceCriteriaQuestionsController.show(CompanyWillDoAnyOfQuestion.name)
    case CompanyWillDoAnyOfQuestion => controllers.routes.TwirlViewController.renderViewAuthorised()
  }

  private def viewForQuestion(q: EligibilityQuestion, form: Form[YesOrNoQuestion])(implicit r: Request[AnyContent]) = q match {
    case HaveNinoQuestion => views.html.pages.vatEligibility.have_nino(form)
    case DoingBusinessAbroadQuestion => views.html.pages.vatEligibility.doing_business_abroad(form)
    case DoAnyApplyToYouQuestion => views.html.pages.vatEligibility.do_any_apply_to_you(form)
    case ApplyingForAnyOfQuestion => views.html.pages.vatEligibility.applying_for_any_of(form)
    case CompanyWillDoAnyOfQuestion => views.html.pages.vatEligibility.company_will_do_any_of(form)
  }

  def show(q: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val question = EligibilityQuestion(q)
    val form: Form[YesOrNoQuestion] = formFactory.form(question.name)
    viewModel[VatServiceEligibility]
      .flatMap(e => OptionT.fromOption(e.getAnswer(question)))
      .fold(form)(answer => form.fill(YesOrNoQuestion(question.name, answer)))
      .map(f => Ok(viewForQuestion(question, f)))
  })

  def submit(q: String): Action[AnyContent] = authorised.async(implicit user => implicit request => {
    val question = EligibilityQuestion(q)
    import common.ConditionalFlatMap._
    formFactory.form(question.name).bindFromRequest().fold(
      badForm => BadRequest(viewForQuestion(question, badForm)).pure,
      (data: YesOrNoQuestion) =>
        for {
          vatEligibility <- viewModel[VatServiceEligibility].getOrElse(VatServiceEligibility())
          _ <- s4LService.saveForm(vatEligibility.setAnswer(question, data.answer))
          exit = data.answer == question.exitAnswer
          _ <- keystore.cache(INELIGIBILITY_REASON_KEY, question.name) onlyIf exit
        } yield Redirect(
          if (exit) eligibilityRoutes.ServiceCriteriaQuestionsController.ineligible() else nextQuestion(question)
        )
    )
  })

  def ineligible(): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    OptionT(keystore.fetchAndGet[String](INELIGIBILITY_REASON_KEY)).getOrElse("").map {
      failedQuestion => Ok(views.html.pages.vatEligibility.ineligible(failedQuestion))
    })

}
