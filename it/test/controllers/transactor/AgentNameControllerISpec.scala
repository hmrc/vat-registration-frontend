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

package controllers.transactor

import itutil.ControllerISpec
import models.{AccountantAgent, DeclarationCapacityAnswer, PersonalDetails, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class AgentNameControllerISpec extends ControllerISpec {

  val url = "/agent-name"

  val firstNameField = "firstName"
  val lastNameField = "lastName"

  def fieldValue(body: String, fieldName: String): String =
    Jsoup.parse(body).getElementById(fieldName).`val`()

  "GET /agent-name" must {
    "return OK with an empty form" in new Setup {
      given()
        .user.isAuthorised(arn = Some(testArn))
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      fieldValue(res.body, firstNameField) mustBe ""
      fieldValue(res.body, lastNameField) mustBe ""
    }
    "return OK with a filled form" in new Setup {
      given()
        .user.isAuthorised(arn = Some(testArn))
        .registrationApi.getSection[TransactorDetails](Some(TransactorDetails(personalDetails = Some(testPersonalDetails))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      fieldValue(res.body, firstNameField) mustBe testFirstName
      fieldValue(res.body, lastNameField) mustBe testLastName
    }
  }

  "POST /agent-name" when {
    "the name is valid" when {
      "redirect to the task list" in new Setup {
        val firstUpdateDetails: TransactorDetails = TransactorDetails(personalDetails = Some(PersonalDetails(
          firstName = testFirstName,
          lastName = testLastName,
          identifiersMatch = true,
          arn = Some(testArn)
        )))

        val secondUpdateDetails: TransactorDetails = firstUpdateDetails.copy(declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent)))

        given()
          .user.isAuthorised(arn = Some(testArn))
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](firstUpdateDetails)
          .registrationApi.getSection[TransactorDetails](Some(firstUpdateDetails))
          .registrationApi.replaceSection[TransactorDetails](secondUpdateDetails)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(firstNameField -> testFirstName, lastNameField -> testLastName)))

        res.status mustBe SEE_OTHER
        res.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
      }
    }
    "the name is invalid" must {
      "return BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised(arn = Some(testArn))
          .registrationApi.getSection[TransactorDetails](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(firstNameField -> "%%%", lastNameField -> testLastName)))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
