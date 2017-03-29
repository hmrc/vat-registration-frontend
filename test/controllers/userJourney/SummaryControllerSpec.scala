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

package controllers.userJourney

import enums.DownstreamOutcome
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.Summary
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.UnexpectedException
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  implicit val materializer = app.materializer

  object TestSummaryController extends SummaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] = Future.successful(Summary(sections = Seq()))
  }

  object TestSummaryController2 extends SummaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  "Calling summary to show the summary page" should {
    "return HTML with a valid summary view" in {
      when(mockVatRegistrationService.submitVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(DownstreamOutcome.Success))
      when(mockS4LService.clear()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(validHttpResponse))

      callAuthorised(TestSummaryController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Check your answers")
      }
    }

    "getRegistrationSummary maps a valid VatScheme object to a Summary object" in {
      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(validVatScheme))
      TestSummaryController2.getRegistrationSummary()(new HeaderCarrier).map(summary => summary.sections.length mustEqual 2)
    }

    "registrationToSummary maps a valid VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(validVatScheme).sections.length mustEqual 2
    }

    "registrationToSummary maps a valid empty VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency).sections.length mustEqual 2
    }

//    "registrationToSummary throws an exception when a non-valid empty VatScheme object is mapped to a Summary object" in {
//      assertThrows[UnexpectedException] {
//        TestSummaryController.registrationToSummary(emptyVatScheme)
//      }
//    }

    // TODO: Need to resolve why raising a new InternalError gives a Boxed Error exception yet this works for PAYE
    "return an Internal Server Error response when no valid model is returned from the microservice" ignore {
      when(mockVatRegistrationService.submitVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(DownstreamOutcome.Success))
      when(mockS4LService.clear()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(validHttpResponse))

      callAuthorised(TestSummaryController.show, mockAuthConnector) {
        (response: Future[Result]) =>
          status(response) mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }


}
