/*
 * Copyright 2022 HM Revenue & Customs
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

import models._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

class S4LServiceSpec extends VatRegSpec {

  trait Setup {
    val service = new S4LService(mockS4LConnector)
  }

  "S4L Service" should {
    "save a form with the correct key" in new Setup {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      implicit val key = OtherBusinessInvolvement.s4lKey(1)
      mockS4LSaveForm[OtherBusinessInvolvement](cacheMap)
      service.save(OtherBusinessInvolvement()) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      implicit val key = OtherBusinessInvolvement.s4lKey(1)
      mockS4LFetchAndGet[OtherBusinessInvolvement](key.key, Some(otherBusinessInvolvementWithVrn))
      service.fetchAndGet[OtherBusinessInvolvement] returns Some(otherBusinessInvolvementWithVrn)
    }

    "clear down S4L data" in new Setup {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LClear()
      service.clear.map(_.status) returns 200
    }
  }
}
