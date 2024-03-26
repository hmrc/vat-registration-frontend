/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.Accordion
import viewmodels.SummaryCheckYourAnswersBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import scala.concurrent.Future

class SummaryServiceSpec extends VatRegSpec {

  implicit val fakeRequest: Request[_] = FakeRequest()

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

  "getRegistrationSummary" should {
    "map a valid VatScheme object to a Summary object" in new Setup {
      val testVatScheme = validVatScheme.copy(eligibilityJson = Some(fullEligibilityDataJson))
      when(mockVatRegistrationService.getVatScheme(any(), any(), any()))
        .thenReturn(Future.successful(testVatScheme))
      when(mockSummaryCheckYourAnswersBuilder.generateSummaryAccordion(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages), ArgumentMatchers.eq(appConfig), ArgumentMatchers.any()))
        .thenReturn(Accordion())

      await(testService.getSummaryData) mustBe Accordion()
    }
  }
}