/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import controllers.internal.DeleteSessionItemsController
import fixtures.VatRegistrationFixture
import models.external.{IncorpStatusEvent, IncorpSubscription, IncorporationInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class DeleteSessionItemsControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  trait Setup {
    val deleteSessionController: DeleteSessionItemsController = new DeleteSessionItemsController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockVatRegistrationService,
      mockKeystoreConnector,
      mockCurrentProfileService,
      mockS4LConnector,
      mockCancellationService,
      mockVatRegistrationConnector
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  def body(status: String) = IncorporationInfo(
    subscription = IncorpSubscription("txId", "regime", "subscriber", "callbackUrl"),
    statusEvent = IncorpStatusEvent(status, None, None, None)
  )

  val fakeRequest: FakeRequest[JsObject] =
    FakeRequest(internal.routes.DeleteSessionItemsController.deleteIfRejected())
      .withBody(Json.toJson(body("accepted")).as[JsObject])
  val fakeRequestRejected: FakeRequest[JsObject] =
    FakeRequest(internal.routes.DeleteSessionItemsController.deleteIfRejected())
      .withBody[JsObject](Json.toJson(body("rejected")).as[JsObject])

  "deleteIfRejected" should {
    "not clear a registration if the incorp update is accepted" in new Setup {
      val resp = deleteSessionController.deleteIfRejected()(fakeRequest)

      status(resp) mustBe 200
    }
    "clear a registration if the incorp update is rejected" in new Setup {
      when(mockVatRegistrationConnector.clearVatScheme(any())(any(), any())) thenReturn Future.successful(HttpResponse(OK))
      when(mockCurrentProfileService.addRejectionFlag(any())) thenReturn Future.successful(Some("regid"))
      when(mockS4LConnector.clear(any())(any())) thenReturn Future.successful(HttpResponse(200))

      val resp = deleteSessionController.deleteIfRejected()(fakeRequestRejected)

      status(resp) mustBe 200
    }
  }
}
