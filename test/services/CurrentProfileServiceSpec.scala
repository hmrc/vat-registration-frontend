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

import java.time.LocalDate

import cats.data.OptionT
import common.enums.VatRegStatus
import helpers.VatRegSpec
import models.CurrentProfile
import models.external.{CoHoCompanyProfile, IncorpStatusEvent, IncorpSubscription, IncorporationInfo}
import org.mockito.Mockito.when
import org.mockito.Matchers
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class CurrentProfileServiceSpec extends VatRegSpec {

  val testService = new CurrentProfileSrv {
    override val keystoreConnector = mockKeystoreConnector
    override val incorpInfoService = mockIIService
  }

  val now = LocalDate.now()

  val testCurrentProfile = CurrentProfile(
    companyName           = "testCompanyName",
    registrationId        = "testRegId",
    transactionId         = "testTxId",
    vatRegistrationStatus = VatRegStatus.DRAFT,
    incorporationDate     = Some(now)
  )

  "buildCurrentProfile" should {
    "return a CurrentProfile" when {
      "the a CurrentProfile has been cached in Keystore" in {

        implicit val hc = HeaderCarrier()

        when(mockKeystoreConnector.fetchAndGet[CoHoCompanyProfile](Matchers.any())(Matchers.any[HeaderCarrier](), Matchers.any[Format[CoHoCompanyProfile]]()))
          .thenReturn(Future.successful(Some(CoHoCompanyProfile("status", "testTxId"))))

        when(mockIIService.getCompanyName(Matchers.any(), Matchers.any())(Matchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful("testCompanyName"))

        when(mockIIService.getIncorporationInfo(Matchers.any())(Matchers.any()))
          .thenReturn(OptionT.liftF(Future.successful(IncorporationInfo(
            IncorpSubscription("","","",""),
            IncorpStatusEvent("", None, Some(now), None)
          ))))

        when(mockKeystoreConnector.cache[CurrentProfile](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(testService.buildCurrentProfile("testRegId", "testTxId"))
        result mustBe testCurrentProfile
      }
    }
  }
}