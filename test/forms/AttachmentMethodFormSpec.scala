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

package forms

import models.api.{EmailMethod, Post}
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class AttachmentMethodFormSpec extends VatRegSpec {

  val form = app.injector.instanceOf[AttachmentMethodForm]

  "The attachment method form" must {
    "bind" when {
      "post is selected" in {
        val res = form().bind(Json.obj("value" -> "3"))
        res.value mustBe Some(Post)
      }
      "email is selected" in {
        val res = form().bind(Json.obj("value" -> "email"))
        res.value mustBe Some(EmailMethod)
      }
    }
    "return a form error" when {
      "nothing is selected" in {
        val res = form().bind(Json.obj())
        res.error("value").map(_.message) mustBe Some("attachmentMethod.error.missing")
      }
    }
  }

}
