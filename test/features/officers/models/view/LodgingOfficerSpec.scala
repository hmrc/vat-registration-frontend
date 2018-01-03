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

package features.officers.models.view

import java.time.LocalDate

import models.api.Name
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class LodgingOfficerSpec extends UnitSpec {
  "Calling apiWrites" should {
    "return a correct full JsValue" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val data = LodgingOfficer(
        Some("FirstMiddleLast"),
        Some(OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z"))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z"
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites(officer)) shouldBe validJson
    }

    "return a correct partial JsValue" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val data = LodgingOfficer(
        Some("FirstLast"),
        Some(OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z"))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z"
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites(officer)) shouldBe validJson
    }
  }
}
