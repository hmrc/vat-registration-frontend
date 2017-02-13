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

package controllers.userJourney

import builders.AuthBuilder
import enums.CacheKeys
import helpers.VatRegSpec
import models.view.StartDate
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class MandatoryStartDateControllerSpec extends VatRegSpec {

  object MandatoryStartDateController extends MandatoryStartDateController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.MandatoryStartDateController.show()}" should {

    "display the mandatory start date confirmation page to the user" in {
      callAuthorised(MandatoryStartDateController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }
  }

  s"POST ${routes.MandatoryStartDateController.submit()}" should {

    "redirect the user to the trading name page after clicking continue on the mandatory start date confirmation page" in {
      callAuthorised(MandatoryStartDateController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
      }
    }
  }

}
