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
import models.view.{StartDate => StartDateView}
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

  val tstStartDateModel = StartDateView("", None, None, None)

  "S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockS4LSaveForm[StartDateView]("startDate", CacheMap("s-date", Map.empty))
      await(service.saveForm[StartDateView]("startDate", tstStartDateModel)).id shouldBe "s-date"
    }

    "fetch a form with the correct key" in new Setup {
      mockS4LFetchAndGet[StartDateView]("startDate2", Some(tstStartDateModel))
      await(service.fetchAndGet[StartDateView]("startDate2")) shouldBe Some(tstStartDateModel)
    }

    "clear down S4L data" in new Setup {
      mockS4LClear()
      await(service.clear()).status shouldBe 200
    }

    "fetch all data" in new Setup {
      mockS4LFetchAll(Some(CacheMap("allData", Map.empty)))
      await(service.fetchAll()) shouldBe Some(CacheMap("allData", Map.empty))
    }

  }

}
