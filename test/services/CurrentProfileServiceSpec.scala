/*
 * Copyright 2020 HM Revenue & Customs
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

import common.enums.VatRegStatus
import models.CurrentProfile
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class CurrentProfileServiceSpec extends VatRegSpec {

  class Setup {
    val service = new CurrentProfileService {
      override def ifRegIdNotWhitelisted[T](regId: String)(f: => Future[T])(implicit default: (String) => T): Future[T] =
        if(regId == "99") Future.successful(default(regId)) else f

      override val vatRegistrationService = mockVatRegistrationService
      override val keystoreConnector = mockKeystoreConnector
      override val incorpInfoService = mockIIService
      override val ivService: IVService = mockIVService
    }
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
      "the a CurrentProfile has been cached in Keystore" in new Setup {
        implicit val hc = HeaderCarrier()

        when(mockIIService.getCompanyName(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorpDate(any(), any())(any()))
          .thenReturn(Future.successful(Some(now)))

        when(mockVatRegistrationService.getStatus(any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockIVService.getIVStatus(any())(any[HeaderCarrier]()))
            .thenReturn(Future.successful(None))

        when(mockIIService.registerInterest(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(true))

        when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(service.buildCurrentProfile("testRegId", "testTxId"))
        result mustBe testCurrentProfile
      }

      "the a CurrentProfile has been cached in Keystore with ivPassed set to true" in new Setup {
        implicit val hc = HeaderCarrier()

        when(mockIIService.getCompanyName(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorpDate(any(), any())(any()))
          .thenReturn(Future.successful(Some(now)))

        when(mockVatRegistrationService.getStatus(any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockIVService.getIVStatus(any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        when(mockIIService.registerInterest(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(true))

        when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(service.buildCurrentProfile("testRegId", "testTxId"))
        result mustBe testCurrentProfile.copy(ivPassed = Some(true))
      }
      "the currentProfile is created if the regId is whitelisted setting the ivPassed to true by default" in new Setup {
        implicit val hc = HeaderCarrier()

        when(mockIIService.getCompanyName(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorpDate(any(), any())(any()))
          .thenReturn(Future.successful(Some(now)))

        when(mockVatRegistrationService.getStatus(any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockIIService.registerInterest(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(true))

        when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(service.buildCurrentProfile("99", "testTxId"))
        result mustBe testCurrentProfile.copy(registrationId = "99", ivPassed = Some(true))
      }
    }
  }

  "addRejectionFlag" should {

    val exisitingProfile = CurrentProfile(
      "companyName",
      "registrationID",
      "transactionID",
      VatRegStatus.draft,
      None
    )


    "return some registration id" when {

      val expectedProfile = CurrentProfile(
        "companyName",
        "registrationID",
        "transactionID",
        VatRegStatus.draft,
        None,
        None,
        Some(true)
      )

      "current profile successfully updated with flag" in new Setup {
        when(mockKeystoreConnector.addRejectionFlag(any())(any()))
          .thenReturn(Future.successful(Some("RegId")))

        await(service.addRejectionFlag("transactionID")) mustBe Some("RegId")
      }
    }
    "return none" when {
      "there is no current profile" in new Setup {
        when(mockKeystoreConnector.addRejectionFlag(any())(any()))
          .thenReturn(Future.successful(None))

        await(service.addRejectionFlag("transactionID")) mustBe None
      }
    }
  }
}
