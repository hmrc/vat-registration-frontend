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

package models.external

import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class CoHoCompanyProfileSpec extends UnitSpec {

  val reader = CoHoCompanyProfile.reader
  val writer = CoHoCompanyProfile.writer

  val json: JsValue = Json.parse(
    """
      |{
      |  "status":"held",
      |  "confirmationReferences":{
      |    "transaction-id":"000-434-1"
      |  }
      |}""".stripMargin)

  val coHoCompanyProfile = CoHoCompanyProfile("held", "000-434-1")

    "CoHoCompanyProfile" should {
      "reads should return coHoCompanyProfile" in {
        reader.reads(json) shouldBe JsSuccess(coHoCompanyProfile)
      }
      "writes should return json" in {
        writer.writes(coHoCompanyProfile) shouldBe json
      }
    }
}
