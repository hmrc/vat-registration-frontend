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

package models.api

import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec

class AttachmentsSpec extends VatRegSpec {

  val minimalModel: Attachments = Attachments(method = None)
  val validMinimalJson: JsObject = Json.obj()

  val fullModel: Attachments = Attachments(method = Some(Post))
  val validFullJson: JsObject = Json.obj("method" -> "3")

  "the attachments model" must {
    "deserialize from minimal valid json" in {
      validMinimalJson.as[Attachments] mustBe minimalModel
    }
    "deserialise from full valid json" in {
      validFullJson.as[Attachments] mustBe fullModel
    }
    "serialise a minimal model to json correctly" in {
      Json.toJson(minimalModel) mustBe validMinimalJson
    }
    "serialise a full model to json correctly" in {
      Json.toJson(fullModel) mustBe validFullJson
    }
  }

}
