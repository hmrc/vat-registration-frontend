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

import models.view.LodgingOfficer
import fixtures.VatRegistrationFixture
import models._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

class S4LServiceSpec extends VatRegSpec {

  trait Setup {
    val service = new S4LService(mockS4LConnector)
  }

  "S4L Service" should {
    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      mockS4LSaveForm[LodgingOfficer](cacheMap)
      service.save(emptyLodgingOfficer) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(S4LKey[LodgingOfficer].key, Some(emptyLodgingOfficer))
      service.fetchAndGet[LodgingOfficer] returns Some(emptyLodgingOfficer)
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LClear()
      service.clear.map(_.status) returns 200
    }
  }
}
