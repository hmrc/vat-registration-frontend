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

package models

import models.ComplianceQuestions.SicCodeMap
import org.scalatest.{Inside, Inspectors}
import uk.gov.hmrc.play.test.UnitSpec

class ComplianceQuestionsSpec extends UnitSpec with Inspectors with Inside {

  private class TestSetup {
    implicit val testCodesMappings: SicCodeMap = Map(
      LabourComplianceQuestions -> Vector("10", "20", "30", "40", "50")
    )
  }

  "Compliance questions selection" must {

    "select NoComplianceQuestions for an empty list of SIC codes" in new TestSetup {
      ComplianceQuestions(List.empty[String].toArray) shouldBe NoComplianceQuestions
    }

    "select NoComplianceQuestions for when not all codes fall in the same group" in new TestSetup {
      ComplianceQuestions(List("1", "2", "20").toArray) shouldBe NoComplianceQuestions
    }

    "select LabourComplianceQuestions for when all codes fall in the labour compliance group" in new TestSetup {
      ComplianceQuestions(List("10", "20").toArray) shouldBe LabourComplianceQuestions
    }
  }

}
