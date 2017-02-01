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

package services

import connectors.VatRegistrationConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import models.view.{Summary, SummaryRow, SummarySection}
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.FutureAwaits
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()
//  val mockRegConnector = mock[VatRegistrationConnector]

  class Setup {
//    val service = new VatRegistrationService(mockRegConnector)
  }

  "Calling registrationToSummary converts a VatScheme API Model to a summary model with a valid details" should {
    "return success" in {
      vatRegistrationService.registrationToSummary(validVatScheme) mustBe validSummaryView
    }
  }

  "Calling getRegistrationSummary" should {
    "return success" in new Setup {
//      when(mockRegConnector.getRegistration(Matchers.any())(Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(validVatScheme))

      vatRegistrationService.getRegistrationSummary()
    }
  }


}
