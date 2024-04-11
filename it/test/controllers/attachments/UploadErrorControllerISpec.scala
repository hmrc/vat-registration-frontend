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
import itFixtures.ITRegistrationFixtures
import play.api.test.Helpers._

class UploadErrorControllerISpec extends BaseControllerISpec with ITRegistrationFixtures {

  val url: String => String = controllers.fileupload.routes.UploadErrorController.show(_).url
  val testReference = "testReference"


  "GET /upload-error/VAT2" must {
    "show the upload error page" in {
      given().user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List())

      val res = await(buildClient(url("VAT2")).get())

      res.status mustBe OK

    }
  }

}
