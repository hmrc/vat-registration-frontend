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

package controllers.fileupload

import itutil.ControllerISpec
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class DocumentUploadTypeErrorControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.DocumentUploadTypeErrorController.show.url

  s"GET $pageUrl" when {
    "the user has 1 or more documents uploaded" must {
      "return OK with the view" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }
    }
  }
}
