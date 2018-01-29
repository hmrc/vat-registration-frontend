/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDate

import cats.data.OptionT
import common.enums.VatRegStatus
import features.officer.services.IVService
import helpers.VatRegSpec
import models.CurrentProfile
import models.external.{IncorpStatusEvent, IncorpSubscription, IncorporationInfo}
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatchers => Matchers}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class CurrentProfileServiceSpec extends VatRegSpec {

  val testService = new CurrentProfileSrv {
    override val vatRegistrationService = mockVatRegistrationService
    override val keystoreConnector = mockKeystoreConnector
    override val incorpInfoService = mockIIService
    override val ivService: IVService = mockIVService
  }

  val now = LocalDate.now()

  val testCurrentProfile = CurrentProfile(
    companyName           = "testCompanyName",
    registrationId        = "testRegId",
    transactionId         = "testTxId",
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate     = Some(now),
    ivPassed              = None
  )

  "buildCurrentProfile" should {
    "return a CurrentProfile" when {
      "the a CurrentProfile has been cached in Keystore" in {
        implicit val hc = HeaderCarrier()

        when(mockIIService.getCompanyName(Matchers.any(), Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorporationInfo(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(
            IncorporationInfo(
              IncorpSubscription("","","",""),
              IncorpStatusEvent("", None, Some(now), None)
            )
          )))

        when(mockVatRegistrationService.getStatus(Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockIVService.getIVStatus(Matchers.any())(Matchers.any[HeaderCarrier]()))
            .thenReturn(Future.successful(None))

        when(mockKeystoreConnector.cache[CurrentProfile](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(testService.buildCurrentProfile("testRegId", "testTxId"))
        result mustBe testCurrentProfile
      }

      "the a CurrentProfile has been cached in Keystore with ivPassed set to true" in {
        implicit val hc = HeaderCarrier()

        when(mockIIService.getCompanyName(Matchers.any(), Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorporationInfo(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(
            IncorporationInfo(
              IncorpSubscription("","","",""),
              IncorpStatusEvent("", None, Some(now), None)
            )
          )))

        when(mockVatRegistrationService.getStatus(Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockIVService.getIVStatus(Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        when(mockKeystoreConnector.cache[CurrentProfile](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(testService.buildCurrentProfile("testRegId", "testTxId"))
        result mustBe testCurrentProfile.copy(ivPassed = Some(true))
      }
    }
  }
}
