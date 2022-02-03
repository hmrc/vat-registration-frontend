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

import testHelpers.VatRegSpec

class AgentNameFormSpec extends VatRegSpec {

  val form = app.injector.instanceOf[AgentNameForm]

  val testValidCharacters = Seq("A", "a", "Z", "z", "-", ",", ".", "1", "9", "\'", "\\", "/", "&", "a b")
  val testInvalidCharacters = Seq("#", "$", "*", "(", ")", "~", "{", "}", ":", ";")

  "The Agent Name form" when {
    testValidCharacters.foreach { char =>
      s"the first name contains the valid character '$char'" must {
        "bind successfully" in {
          val res = form.apply.bind(Map(
            AgentNameForm.firstNameField -> char,
            AgentNameForm.lastNameField -> "test"
          ))

          res.errors.isEmpty mustBe true
        }
      }
    }

    "the first name is exactly 100 characters long" must {
      "bind successfully" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> ('w' * 35).toString,
          AgentNameForm.lastNameField -> "test"
        ))

        res.errors.isEmpty mustBe true
      }
    }
    "the first name is too long" must {
      "return the correct error message" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> "123456789012345678901234567890123456",
          AgentNameForm.lastNameField -> "test"
        ))

        res.error(AgentNameForm.firstNameField).map(_.message) mustBe Some("validation.agentName.firstName.maxLen")
      }
    }
    "the first name is empty" must {
      "return the correct error message" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> "",
          AgentNameForm.lastNameField -> "test"
        ))

        res.error(AgentNameForm.firstNameField).map(_.message) mustBe Some("validation.agentName.firstName.missing")
      }
    }

    testInvalidCharacters.foreach { char =>
      s"the first name contains the invalid character '$char''" must {
        "return the correct error message" in {
          val res = form.apply.bind(Map(
            AgentNameForm.firstNameField -> char,
            AgentNameForm.lastNameField -> "test"
          ))

          res.error(AgentNameForm.firstNameField).map(_.message) mustBe Some("validation.agentName.firstName.invalid")
        }
      }
    }

    testValidCharacters.foreach { char =>
      s"the last name contains the valid character '$char'" must {
        "bind successfully" in {
          val res = form.apply.bind(Map(
            AgentNameForm.firstNameField -> "test",
            AgentNameForm.lastNameField -> char
          ))

          res.errors.isEmpty mustBe true
        }
      }
    }

    "the last name is exactly 35 characters long" must {
      "bind successfully" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> "test",
          AgentNameForm.lastNameField -> ('w' * 35).toString
        ))

        res.errors.isEmpty mustBe true
      }
    }
    "the last name is too long" must {
      "return the correct error message" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> "test",
          AgentNameForm.lastNameField -> "123456789012345678901234567890123456"
        ))

        res.error(AgentNameForm.lastNameField).map(_.message) mustBe Some("validation.agentName.lastName.maxLen")
      }
    }
    "the last name is empty" must {
      "return the correct error message" in {
        val res = form.apply.bind(Map(
          AgentNameForm.firstNameField -> "test",
          AgentNameForm.lastNameField -> ""
        ))

        res.error(AgentNameForm.lastNameField).map(_.message) mustBe Some("validation.agentName.lastName.missing")
      }
    }

    testInvalidCharacters.foreach { char =>
      s"the last name contains the invalid character '$char''" must {
        "return the correct error message" in {
          val res = form.apply.bind(Map(
            AgentNameForm.firstNameField -> "test",
            AgentNameForm.lastNameField -> char
          ))

          res.error(AgentNameForm.lastNameField).map(_.message) mustBe Some("validation.agentName.lastName.invalid")
        }
      }
    }
  }

}
