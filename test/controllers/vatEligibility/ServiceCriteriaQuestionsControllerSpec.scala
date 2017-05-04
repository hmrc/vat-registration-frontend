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
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import forms.vatEligibility.ServiceCriteriaFormFactory
import helpers.VatRegSpec
import models.api.EligibilityQuestion._
import models.api.{EligibilityQuestion, VatServiceEligibility}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceCriteriaQuestionsControllerSpec extends VatRegSpec with VatRegistrationFixture {

  import cats.instances.future._
  import cats.syntax.applicative._

  val mockVatRegService = mock[VatRegistrationService]

  object TestController extends ServiceCriteriaQuestionsController(ds, new ServiceCriteriaFormFactory())(
    mockS4LService, mockVatRegService
  ) {
    override val authConnector: AuthConnector = mockAuthConnector
    override lazy val keystore: KeystoreConnector = mockKeystoreConnector
  }

  private def setupIneligibilityReason(keystoreConnector: KeystoreConnector, question: EligibilityQuestion) =
    when(mockKeystoreConnector.fetchAndGet[String](Matchers.eq(TestController.INELIGIBILITY_REASON_KEY))(any(), any()))
      .thenReturn(Some(question.name).pure)

  "GET ServiceCriteriaQuestionsController.show()" should {

    "return HTML for relevant page with no data in the form" in {
      when(mockS4LService.fetchAndGet[VatServiceEligibility]()(any(), any(), any()))
        .thenReturn(Some(validServiceEligibility).pure)

      val eligibilityQuestions = Seq[(EligibilityQuestion, String)](
        HaveNinoQuestion -> "Do you have a National Insurance number?",
        DoingBusinessAbroadQuestion -> "Will you do any of the following once you&#x27;re registered for VAT?",
        DoAnyApplyToYouQuestion -> "Do any of the following apply to you or the business?",
        ApplyingForAnyOfQuestion -> "Will you apply for any of the following?",
        CompanyWillDoAnyOfQuestion -> "Will you do any of the following?"
      )

      forAll(eligibilityQuestions) { case (question, expectedTitle) =>
        callAuthorised(TestController.show(question.name))(_ includesText expectedTitle)
      }
    }

  }

  "POST ServiceCriteriaQuestionsController.submit()" should {

    val dummyCacheMap = CacheMap("id", Map())

    def urlForQuestion(eligibilityQuestion: EligibilityQuestion): String =
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(eligibilityQuestion.name).url

    val questions = Seq(
      HaveNinoQuestion -> urlForQuestion(DoingBusinessAbroadQuestion),
      DoingBusinessAbroadQuestion -> urlForQuestion(DoAnyApplyToYouQuestion),
      DoAnyApplyToYouQuestion -> urlForQuestion(ApplyingForAnyOfQuestion),
      ApplyingForAnyOfQuestion -> urlForQuestion(CompanyWillDoAnyOfQuestion),
      CompanyWillDoAnyOfQuestion -> controllers.routes.TwirlViewController.renderViewAuthorised().url
    )

    "redirect to next screen when user is eligible to register for VAT using this service" in {
      forAll(questions) { case (currentQuestion, nextScreenUrl) =>

        setupIneligibilityReason(mockKeystoreConnector, currentQuestion)
        when(mockS4LService.fetchAndGet[VatServiceEligibility]()(any(), any(), any()))
          .thenReturn(Some(validServiceEligibility).pure)
        when(mockS4LService.saveForm[VatServiceEligibility](any())(any(), any(), any()))
          .thenReturn(dummyCacheMap.pure)

        AuthBuilder.submitWithAuthorisedUser(TestController.submit(currentQuestion.name),
          FakeRequest().withFormUrlEncodedBody(
            "question" -> currentQuestion.name,
            s"${currentQuestion.name}Radio" -> (!currentQuestion.exitAnswer).toString)
        )(_ redirectsTo nextScreenUrl)
      }
    }

    "redirect to next screen when eligible and nothing in s4l or backend" in {
      forAll(questions) { case (currentQuestion, nextScreenUrl) =>

        setupIneligibilityReason(mockKeystoreConnector, currentQuestion)
        when(mockS4LService.fetchAndGet[VatServiceEligibility]()(any(), any(), any()))
          .thenReturn(Option.empty[VatServiceEligibility].pure)
        when(mockVatRegService.getVatScheme()(any()))
          .thenReturn(validVatScheme.copy(vatServiceEligibility = None).pure)
        when(mockS4LService.saveForm[VatServiceEligibility](any())(any(), any(), any()))
          .thenReturn(dummyCacheMap.pure)

        AuthBuilder.submitWithAuthorisedUser(TestController.submit(currentQuestion.name),
          FakeRequest().withFormUrlEncodedBody(
            "question" -> currentQuestion.name,
            s"${currentQuestion.name}Radio" -> (!currentQuestion.exitAnswer).toString)
        )(_ redirectsTo nextScreenUrl)
      }
    }


    "redirect to ineligible screen when user is NOT eligible to register for VAT using this service" in {
      forAll(questions) { case (currentQuestion, nextScreenUrl) =>

        setupIneligibilityReason(mockKeystoreConnector, currentQuestion)
        when(mockS4LService.fetchAndGet[VatServiceEligibility]()(any(), any(), any()))
          .thenReturn(Some(validServiceEligibility).pure)
        when(mockS4LService.saveForm[VatServiceEligibility](any())(any(), any(), any()))
          .thenReturn(dummyCacheMap.pure)
        when(mockKeystoreConnector.cache[String](any(), any())(any(), any()))
          .thenReturn(dummyCacheMap.pure)

        AuthBuilder.submitWithAuthorisedUser(TestController.submit(currentQuestion.name),
          FakeRequest().withFormUrlEncodedBody(
            "question" -> currentQuestion.name,
            s"${currentQuestion.name}Radio" -> currentQuestion.exitAnswer.toString)
        ) {
          _ redirectsTo controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.ineligible().url
        }

        verify(mockKeystoreConnector, times(1)).cache[String](Matchers.eq(TestController.INELIGIBILITY_REASON_KEY), any())(any(), any())
        reset(mockKeystoreConnector)
      }
    }

    "400 for malformed requests" in {
      forAll(questions) { case (q, _) =>
        AuthBuilder.submitWithAuthorisedUser(TestController.submit(q.name),
          FakeRequest().withFormUrlEncodedBody(s"${q.name}Radio" -> "foo")
        )(result => result isA 400)
      }
    }
  }


  "GET ineligible screen" should {

    "return HTML for relevant ineligibility page" in {

      //below the "" empty css class indicates that the section is showing (not "hidden")
      val eligibilityQuestions = Seq[(EligibilityQuestion, String)](
        HaveNinoQuestion -> """id="nino-text" class=""""",
        DoingBusinessAbroadQuestion -> """id="business-abroad-text" class=""""",
        DoAnyApplyToYouQuestion -> """id="do-any-apply-to-you-text" class=""""",
        ApplyingForAnyOfQuestion -> """id="applying-for-any-of-text" class=""""",
        CompanyWillDoAnyOfQuestion -> """id="company-will-do-any-of-text" class="""""
      )

      forAll(eligibilityQuestions) { case (question, expectedTitle) =>
        setupIneligibilityReason(mockKeystoreConnector, question)
        callAuthorised(TestController.ineligible())(_ includesText expectedTitle)
      }
    }

    "return HTML for ineligibility page even when no reason found in keystore" in {
      val hiddenReasonSections = Seq(
        """id="nino-text" class="hidden"""",
        """id="business-abroad-text" class="hidden"""",
        """id="do-any-apply-to-you-text" class="hidden"""",
        """id="applying-for-any-of-text" class="hidden"""",
        """id="company-will-do-any-of-text" class="hidden""""
      )

      when(mockKeystoreConnector.fetchAndGet[String](Matchers.eq(TestController.INELIGIBILITY_REASON_KEY))(any(), any()))
        .thenReturn(Option.empty[String].pure)
      when(mockVatRegService.getVatScheme()(any())).thenReturn(validVatScheme.copy(vatServiceEligibility = None).pure)

      callAuthorised(TestController.ineligible()) {
        result => forAll(hiddenReasonSections)(result includesText _)
      }
    }

  }

}