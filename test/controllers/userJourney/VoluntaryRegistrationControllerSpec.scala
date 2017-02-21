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
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.VoluntaryRegistration
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class VoluntaryRegistrationControllerSpec extends VatRegSpec with VatRegistrationFixture {
  implicit val hc = HeaderCarrier()
  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVoluntaryRegistrationController extends VoluntaryRegistrationController(mockS4LService, mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationController.show())

  s"GET ${routes.VoluntaryRegistrationController.show()}" should {

    "return HTML Voluntary Registration  page with no Selection" in {
      val voluntaryRegistration = VoluntaryRegistration("")

      when(mockS4LService.fetchAndGet[VoluntaryRegistration](Matchers.eq(CacheKeys.VoluntaryRegistration.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(voluntaryRegistration)))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> ""
      )){

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you want to register voluntarily for VAT?")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[VoluntaryRegistration](Matchers.eq(CacheKeys.VoluntaryRegistration.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[VoluntaryRegistration]]()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVoluntaryRegistrationController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you want to register voluntarily for VAT?")
      }
    }
  }


  s"POST ${routes.VoluntaryRegistrationController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe  Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected Yes" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistration.empty)))

      when(mockS4LService.saveForm[VoluntaryRegistration](Matchers.eq(CacheKeys.VoluntaryRegistration.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_YES
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/start-date"
     }

    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected No" should {

    "redirect to the welcome page" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistration.empty)))

      when(mockS4LService.saveForm[VoluntaryRegistration](Matchers.eq(CacheKeys.VoluntaryRegistration.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))
      when(mockVatRegistrationService.deleteVatScheme())
        .thenReturn(Future.successful(true))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_NO
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration"
      }

    }
  }

}
