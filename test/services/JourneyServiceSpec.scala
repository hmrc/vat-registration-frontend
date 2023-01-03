/*
 * Copyright 2023 HM Revenue & Customs
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

import common.enums.VatRegStatus
import config.FrontendAppConfig
import models.CurrentProfile
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate
import scala.concurrent.Future

class JourneyServiceSpec extends VatRegSpec {

  lazy val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  class Setup {
    val service = new JourneyService(
      mockVatRegistrationService,
      mockSessionService,
      frontendAppConfig
    )
  }

  val now = LocalDate.now()

  val testCurrentProfile: CurrentProfile = CurrentProfile(
    registrationId = "testRegId",
    vatRegistrationStatus = VatRegStatus.draft
  )

  "buildCurrentProfile" should {
    "return a CurrentProfile" when {
      "the a CurrentProfile has been cached in Keystore" in new Setup {
        implicit val hc = HeaderCarrier()

        when(mockVatRegistrationService.getStatus(any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(service.buildCurrentProfile("testRegId"))
        result mustBe testCurrentProfile
      }
    }
  }
}
