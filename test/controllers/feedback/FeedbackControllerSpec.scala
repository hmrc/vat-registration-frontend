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

package controllers.feedback

import helpers.VatRegSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class FeedbackControllerSpec extends VatRegSpec {

  object TestController extends FeedbackController(ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.FeedbackController.show())

  "GET /feedback" should {

    "redirect to GG sign in when not authorized" in {
       new FeedbackController(ds).show()(fakeRequest) redirectsTo authUrl

    }

    "return HTML when user is authorized to access" in {
      callAuthorised(TestController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
  }

}
