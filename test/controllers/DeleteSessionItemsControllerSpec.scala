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

package controllers

import connectors.KeystoreConnect
import controllers.internal.DeleteSessionItemsController
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class DeleteSessionItemsControllerSpec extends ControllerSpec with MockMessages with FutureAssertions with VatRegistrationFixture {

  trait Setup {
    val deleteSessionController = new DeleteSessionItemsController {
      val authConnector = mockAuthClientConnector
      val cancellationService = mockCancellationService
      val regConnector = mockRegConnector
      override val vatRegistrationService: VatRegistrationService = mockVatRegistrationService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      val messagesApi: MessagesApi = mockMessagesAPI
    }
    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest: FakeRequest[JsObject] =
    FakeRequest(internal.routes.DeleteSessionItemsController.deleteIfRejected())
      .withBody[JsObject](Json.parse("""{"_id":"transId","transaction_status":"accepted"}""").as[JsObject])
  val fakeRequestRejected: FakeRequest[JsObject] =
    FakeRequest(internal.routes.DeleteSessionItemsController.deleteIfRejected())
      .withBody[JsObject](Json.parse("""{"_id":"transId","transaction_status":"rejected"}""").as[JsObject])

  "deleteIfRejected" should {
    "not clear a registration if the incorp update is accepted" in new Setup {
      val resp = deleteSessionController.deleteIfRejected()(fakeRequest)

      status(resp) mustBe 200
    }
    "clear a registration if the incorp update is rejected" in new Setup {
      when(mockRegConnector.clearVatScheme(any())(any(), any())) thenReturn Future.successful(HttpResponse(OK))

      val resp = deleteSessionController.deleteIfRejected()(fakeRequestRejected)

      status(resp) mustBe 200
    }
  }
}
