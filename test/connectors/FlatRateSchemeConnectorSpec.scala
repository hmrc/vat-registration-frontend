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

package connectors

import java.time.LocalDate

import config.WSHttp
import features.returns.Start
import fixtures.VatRegistrationFixture
import frs.{AnnualCosts, FlatRateScheme}
import helpers.VatRegSpec
import play.api.libs.json.{JsBoolean, JsObject, Json}
import uk.gov.hmrc.http.HttpResponse

import scala.language.postfixOps

class FlatRateSchemeConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new RegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val vatRegElUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  val start = Start(Some(LocalDate.now().plusDays(5)))
  val fullS4L = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(AnnualCosts.AlreadyDoesSpend),
    vatTaxableTurnover = Some(12345678L),
    overBusinessGoodsPercent = Some(AnnualCosts.AlreadyDoesSpend),
    useThisRate = Some(true),
    frsStart = Some(start),
    categoryOfBusiness = Some("testCategory"),
    percent = Some(15)
  )

  "Calling upsertFlatRate" should {
    "return the correct VatResponse when the microservice completes and returns a FlatRateScheme model" in new Setup {
      val resp = HttpResponse(200)

      mockHttpPATCH[FlatRateScheme, HttpResponse]("tst-url", resp)
      connector.upsertFlatRate("tstID", fullS4L) returns resp
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[FlatRateScheme, HttpResponse]("tst-url", forbidden)
      connector.upsertFlatRate("tstID", fullS4L) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[FlatRateScheme, HttpResponse]("tst-url", notFound)
      connector.upsertFlatRate("tstID", fullS4L) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[FlatRateScheme, HttpResponse]("tst-url", internalServiceException)
      connector.upsertFlatRate("tstID", fullS4L) failedWith internalServiceException
    }
  }

  "Calling getFlatRate" should {
    "return the correct VatResponse when the microservice completes and returns a FlatRateScheme model" in new Setup {
      val json: JsObject = Json.obj(
        "joinFrs" -> true,
        "frsDetails"           -> Json.obj(
          "businessGoods"      -> Json.obj(
              "estimatedTotalSales" -> 12345678L,
            "overTurnover"        -> true),
          "startDate"          -> start.date,
          "categoryOfBusiness" -> "testCategory",
          "percent"            -> 15
        )
      )
      val resp = HttpResponse(200, Some(json))

      mockHttpGET[HttpResponse]("tst-url", resp)
      connector.getFlatRate("tstID") returns Some(fullS4L)
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getFlatRate("tstID") failedWith forbidden
    }
    "return a Not Found S4LFlatRateScheme when the microservice returns a NoContent response (No VatRegistration in database)" in new Setup {
      val resp = HttpResponse(204)

      mockHttpGET[HttpResponse]("tst-url", resp)
      connector.getFlatRate("tstID") returns None
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServiceException)
      connector.getFlatRate("tstID") failedWith internalServiceException
    }
  }


  "Calling clearFlatRate" should {
    "return the correct VatResponse when the microservice completes and clears flat rate" in new Setup {
      val resp = HttpResponse(200)

      mockHttpDELETE[HttpResponse]("tst-url", resp)
      connector.clearFlatRate("tstID") returns resp
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", forbidden)
      connector.clearFlatRate("tstID") failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", notFound)
      connector.clearFlatRate("tstID") failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedDELETE[HttpResponse]("tst-url", internalServiceException)
      connector.clearFlatRate("tstID") failedWith internalServiceException
    }
  }
}
