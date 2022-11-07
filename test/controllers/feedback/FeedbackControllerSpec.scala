/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.feedback

import play.api.http.Status
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

class FeedbackControllerSpec extends ControllerSpec with FutureAssertions {

  class Setup {

    val controller: FeedbackController = new FeedbackController(
      mockAuthClientConnector,
      mockSessionService
    )
  }

  "GET /feedback" should {
    val fakeRequest = FakeRequest("GET", "/")

    "return feedback page" in new Setup {
      val result = controller.feedbackShow(fakeRequest)
      result isA Status.SEE_OTHER

      redirectLocation(result).map { url =>
        url.contains("/contact/beta-feedback") mustBe true
        url.contains(s"service=vrs") mustBe true
      }
    }

    "capture the referrer in the session on initial session on the feedback load" in new Setup {
      val result = controller.feedbackShow(fakeRequest.withHeaders("Referer" -> "Blah"))
      result isA Status.SEE_OTHER
    }
  }

}