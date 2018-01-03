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

package features.officers.transformers

import java.time.LocalDate

import features.officers.models.view.LodgingOfficer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class ToLodgingOfficerViewSpec extends UnitSpec {
  "fromApi" should {
    "return a correct full LodgingOfficer view model" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)

      val lodgingOfficer = LodgingOfficer(Some("FirstMiddleLast"), Some(OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")))

      ToLodgingOfficerView.fromApi(json) shouldBe lodgingOfficer
    }

    "return a correct partial Name LodgingOfficer view model" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)

      val lodgingOfficer = LodgingOfficer(Some("FirstLast"), Some(OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")))

      ToLodgingOfficerView.fromApi(json) shouldBe lodgingOfficer
    }
  }
}
