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

package controllers.vatLodgingOfficer

import controllers.vatLodgingOfficer
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatLodgingOfficer.FormerNameView
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormerNameControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestFormerNameController extends FormerNameController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatLodgingOfficer.routes.FormerNameController.show())

  s"GET ${vatLodgingOfficer.routes.FormerNameController.show()}" should {

    "return HTML when there's a former name in S4L" in {
      val formerName = FormerNameView(true, Some("Test Former Name"))

      save4laterReturns(formerName)

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing[FormerNameView]()

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing[FormerNameView]()

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }
  }

  s"POST ${vatLodgingOfficer.routes.FormerNameController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => result isA 400
      }

    }
  }

  s"POST ${vatLodgingOfficer.routes.FormerNameController.submit()} with valid data no former name" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(FormerNameView(false, None))))

      when(mockS4LService.saveForm[FormerNameView](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "false"
      )) {
        _ redirectsTo s"$contextRoot/your-date-of-birth"
      }

    }
  }

  s"POST ${vatLodgingOfficer.routes.FormerNameController.submit()} with valid data with former name" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(FormerNameView(true, Some("some name")))))

      when(mockS4LService.saveForm[FormerNameView](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "true",
        "formerName" -> "some name"
      )) {
        _ redirectsTo s"$contextRoot/your-date-of-birth"
      }

    }
  }

}
