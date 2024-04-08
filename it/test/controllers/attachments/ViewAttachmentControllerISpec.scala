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

package controllers.attachments

import controllers.BaseControllerISpec
import play.api.http.Status.OK
import play.api.test.Helpers._

class ViewAttachmentControllerISpec extends BaseControllerISpec {
  val url: String = controllers.fileupload.routes.ViewAttachmentController.show.url

  "GET /attachment-details must" must {
    "display the attachment details page" in {

      given().user.isAuthorised()
      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
  }

}
