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

package controllers.feedback

import helpers.VatRegSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.{CoreGet, HttpResponse}
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, FormPartialRetriever}

import scala.concurrent.Future

class FeedbackControllerSpec extends VatRegSpec {

  class Setup {

    val controller = new FeedbackController(ds, mockAuthConnector, mockWSHttp)
  }

  "GET /feedback" should {
    val fakeRequest = FakeRequest("GET", "/")

    "return feedback page" in new Setup {
      val result = controller.feedbackShow(fakeRequest)
      result isA Status.SEE_OTHER
    }

    "capture the referrer in the session on initial session on the feedback load" in new Setup {
      val result = controller.feedbackShow(fakeRequest.withHeaders("Referer" -> "Blah"))
      result isA Status.SEE_OTHER
    }
  }

  "POST /feedback" should {
    val fakeRequest = FakeRequest("GET", "/")
    val fakePostRequest = FakeRequest("POST", "/register-for-paye/feedback").withFormUrlEncodedBody("test" -> "test")
    "return form with thank you for valid selections" in new Setup {
      when(mockWSHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
        Future.successful(HttpResponse(Status.OK, responseString = Some("1234"))))
    }
  }
}