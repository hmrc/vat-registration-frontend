/*
 * Copyright 2021 HM Revenue & Customs
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

package models

import fixtures.VatRegistrationFixture
import play.api.libs.json.{Format, Json}
import testHelpers.VatRegSpec

class TransactorDetailsSpec extends VatRegSpec with JsonFormatValidation with VatRegistrationFixture {
  private def writeAndRead[T](t: T)(implicit fmt: Format[T]) = fmt.reads(Json.toJson(fmt.writes(t)))

  "Creating a Json from a valid TransactorDetails model" should {
    "parse successfully" in {
      val transactorDetails = TransactorDetails(
        Some(testPersonalDetails),
        Some(true),
        Some(testCompanyName),
        Some("1234"),
        Some("test@t.test"),
        Some(true),
        Some(testAddress),
        Some(DeclarationCapacityAnswer(AuthorisedEmployee))
      )

      writeAndRead(transactorDetails) resultsIn validTransactorDetails
    }
  }

  "Parsing an invalid json should" should {
    "fail with a JsonValidationError" in {
      Json.fromJson[TransactorDetails](Json.obj(
        "isPartOfOrganisation" -> "yes"
      )).isError mustBe true
    }
  }
}
