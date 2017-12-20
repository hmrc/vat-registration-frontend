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

package helpers

import java.time.LocalDate

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import mocks.VatMocks
import models.CurrentProfile
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.HeaderCarrier

trait VatSpec extends PlaySpec with MockitoSugar with VatRegistrationFixture with VatMocks
  with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val currentProfile: CurrentProfile = CurrentProfile(
    companyName = "Test Me",
    registrationId = testRegId,
    transactionId = "000-434-1",
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate = Some(LocalDate.of(2017, 12, 21)),
    ivPassed = Some(true)
  )

  val dummyCacheMap: CacheMap = CacheMap("", Map.empty)

  override protected def beforeEach() {
    resetMocks()
  }
}
