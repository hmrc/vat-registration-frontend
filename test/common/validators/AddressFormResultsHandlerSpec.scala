/*
 * Copyright 2024 HM Revenue & Customs
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

package common.validators

import config.FrontendAppConfig
import models.api.Country
import org.scalatest.matchers.must.Matchers
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException
import views.html.CaptureInternationalAddress

class AddressFormResultsHandlerSpec extends VatRegSpec with Matchers {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  "country code lookup" must {
    "return successfully if country config available in countries list" in {
      val resultsHandler = new AddressFormResultsHandler(app.injector.instanceOf[CaptureInternationalAddress])

      val expectedCountryName = "United Kingdom"
      val testCountriesList = List(Country(Some("UK"), Some(expectedCountryName)))
      val actualCountryName = resultsHandler.getCountryName(testCountriesList, Some(expectedCountryName))

      actualCountryName mustBe expectedCountryName
    }

    "fail if given country name is empty" in {
      val resultsHandler = new AddressFormResultsHandler(app.injector.instanceOf[CaptureInternationalAddress])
      val testCountriesList = List(Country(Some("UK"), Some("United Kingdom")))

      val caught = intercept[InternalServerException] {
        resultsHandler.getCountryName(testCountriesList, None)
      }

      caught.getMessage mustBe "[AddressFormResultsHandler] Missing country name"
    }

    "fail if given country name mapping is missing" in {
      val resultsHandler = new AddressFormResultsHandler(app.injector.instanceOf[CaptureInternationalAddress])

      val country = "missing-country"
      val testCountriesList = List(Country(Some("UK"), Some("United Kingdom")))

      val caught = intercept[InternalServerException] {
        resultsHandler.getCountryName(testCountriesList, Some(country))
      }

      caught.getMessage mustBe s"[AddressFormResultsHandler] Missing country mapping for '$country'"
    }
  }
}
