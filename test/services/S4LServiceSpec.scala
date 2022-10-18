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

import models.api.UkCompany
import models._
import play.api.libs.json.{Reads, Writes}
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
      mockS4LSaveForm[ApplicantDetails](cacheMap)
      implicit val writes: Writes[ApplicantDetails] = ApplicantDetails.s4LWrites
      service.save(emptyApplicantDetails) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(S4LKey[ApplicantDetails].key, Some(emptyApplicantDetails))
      implicit val reads: Reads[ApplicantDetails]= ApplicantDetails.s4LReads(UkCompany)
      service.fetchAndGet[ApplicantDetails] returns Some(emptyApplicantDetails)
    }

    "clear down S4L data" in new Setup {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LClear()
      service.clear.map(_.status) returns 200
    }
  }
}
