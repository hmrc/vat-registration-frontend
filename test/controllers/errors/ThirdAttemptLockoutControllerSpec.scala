/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.errors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.errors.ThirdAttemptLockoutPage

import scala.concurrent.Future

class ThirdAttemptLockoutControllerSpec extends ControllerSpec with FutureAssertions {

  val view: ThirdAttemptLockoutPage = app.injector.instanceOf[ThirdAttemptLockoutPage]

  trait Setup {
    val testController = new ThirdAttemptLockoutController(
      messagesControllerComponents,
      view,
      mockAuthConnector
    )

    // ThirdAttemptLockoutController uses AuthorisedFunctions which calls authConnector.authorise
    // directly, so we stub mockAuthConnector (uk.gov.hmrc.auth.core.AuthConnector) rather than
    // mockAuthClientConnector.
    when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
      .thenReturn(Future.successful(()))
  }

  "show" should {
    "return 200 and render the lockout page" in new Setup {
      val result = testController.show()(FakeRequest())
      status(result)      mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }
}

