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

import fixtures.{S4LFixture, VatRegistrationFixture}
import models.CacheKey
import models.view.vatChoice.StartDate
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

class S4LServiceSpec extends VatRegSpec with S4LFixture with VatRegistrationFixture {

  trait Setup {
    val service = new S4LService {
      override val s4LConnector = mockS4LConnector
      override val keystoreConnector = mockKeystoreConnector
    }
  }

  implicit val hc = new HeaderCarrier()

  val tstStartDateModel = StartDate("", None)

  "S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LSaveForm[StartDate](CacheMap("s-date", Map.empty))
      await(service.saveForm[StartDate](tstStartDateModel)).id shouldBe "s-date"
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LFetchAndGet[StartDate](CacheKey[StartDate].cacheKey, Some(tstStartDateModel))
      await(service.fetchAndGet[StartDate]()) shouldBe Some(tstStartDateModel)
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LClear()
      await(service.clear()).status shouldBe 200
    }

    "fetch all data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LFetchAll(Some(CacheMap("allData", Map.empty)))
      await(service.fetchAll()) shouldBe Some(CacheMap("allData", Map.empty))
    }

  }

}
