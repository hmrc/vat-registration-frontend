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
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatChoice.StartDateView
import uk.gov.hmrc.http.cache.client.CacheMap
import scala.concurrent.ExecutionContext.Implicits.global

class S4LServiceSpec extends VatRegSpec with S4LFixture with VatRegistrationFixture {

  trait Setup {
    val service = new S4LService {
      override val s4LConnector = mockS4LConnector
      override val keystoreConnector = mockKeystoreConnector
    }
  }

  val tstStartDateModel = StartDateView("", None)

  "S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      mockS4LSaveForm[StartDateView](cacheMap)
      service.saveForm[StartDateView](tstStartDateModel) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LFetchAndGet[StartDateView](S4LKey[StartDateView].key, Some(tstStartDateModel))
      service.fetchAndGet[StartDateView]() returns Some(tstStartDateModel)
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LClear()
      service.clear().map(_.status) returns 200
    }

    "fetch all data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      private val cacheMap = CacheMap("allData", Map.empty)
      mockS4LFetchAll(Some(cacheMap))
      service.fetchAll() returns Some(cacheMap)
    }

  }

}
