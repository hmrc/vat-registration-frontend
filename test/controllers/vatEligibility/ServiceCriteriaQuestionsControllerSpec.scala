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

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import forms.vatEligibility.ServiceCriteriaFormFactory
import helpers.VatRegSpec
import models.api.EligibilityQuestion._
import models.api.{EligibilityQuestion, VatServiceEligibility}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import services.VatRegistrationService

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceCriteriaQuestionsControllerSpec extends VatRegSpec with VatRegistrationFixture {

  import cats.instances.future._
  import cats.syntax.applicative._

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestServiceCriteriaQuestionsController
    extends ServiceCriteriaQuestionsController(
      ds, new ServiceCriteriaFormFactory()
    )(
      mockS4LService, mockVatRegistrationService
    ) {

    override val authConnector = mockAuthConnector

  }

  s"GET ServiceCriteriaQuestionsController.show()" should {

    "return HTML for relevant page with no data in the form" in {
      when(mockS4LService.fetchAndGet[VatServiceEligibility]()(any(), any(), any()))
        .thenReturn(Some(validServiceEligibility).pure)

      val eligibilityQuestions = Seq[(EligibilityQuestion, String)](
        HaveNinoQuestion -> "Do you have a National Insurance number?",
        DoingBusinessAbroadQuestion -> "Will you do any of the following once you&#x27;re registered for VAT?"
        //          DoAnyApplyToYouQuestion -> "Do you have a National Insurance number?"
        //        ApplyingForAnyOfQuestion -> "Do you have a National Insurance number?",
        //        CompanyWillDoAnyOfQuestion -> "Do you have a National Insurance number?"
      )

      forAll(eligibilityQuestions) { case (question, expectedTitle) =>
        AuthBuilder.submitWithAuthorisedUser(
          TestServiceCriteriaQuestionsController.show(question.name), FakeRequest().withFormUrlEncodedBody()) {
          _ includesText expectedTitle
        }
      }
    }

  }

}