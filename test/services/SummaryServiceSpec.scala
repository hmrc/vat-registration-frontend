/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import config.FrontendAppConfig
import models.view.EligibilityJsonParser
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.SummaryCheckYourAnswersBuilder

import scala.concurrent.Future

class SummaryServiceSpec extends VatRegSpec {

  class Setup {
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
    val mockSummaryCheckYourAnswersBuilder: SummaryCheckYourAnswersBuilder = mock[SummaryCheckYourAnswersBuilder]
    val testService: SummaryService = new SummaryService(
      mockVatRegistrationService,
      mockSummaryCheckYourAnswersBuilder
    )
  }

  "eligibilityCall" should {
    "return a full url" in new Setup {
      val res: String = testService.eligibilityCall("page1OfEligibility")
      res mustBe "http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=page1OfEligibility"
    }
  }

  "getRegistrationSummary" should {
    "map a valid VatScheme object to a Summary object" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme))
      when(mockSummaryCheckYourAnswersBuilder.generateSummaryList(ArgumentMatchers.eq(validVatScheme), ArgumentMatchers.eq(messages)))
        .thenReturn(SummaryList())

      await(testService.getRegistrationSummary) mustBe SummaryList()
    }
  }

  "getEligibilitySummary" should {
    "return a Summary when valid json is returned from vatregservice" in new Setup {
      when(mockVatRegistrationService.getEligibilityData(any(), any())) thenReturn Future.successful(fullEligibilityDataJson.as[JsObject])

      await(testService.getEligibilityDataSummary).rows.length mustBe 8
    }
    "return an exception when json is invalid returned from vatregservice" in new Setup {
      when(mockVatRegistrationService.getEligibilityData(any(), any())) thenReturn Future.successful(Json.obj("foo" -> "bar"))

      intercept[Exception](await(testService.getEligibilityDataSummary))
    }
  }
}