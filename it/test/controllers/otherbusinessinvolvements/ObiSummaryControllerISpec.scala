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

package controllers.otherbusinessinvolvements

import itutil.ControllerISpec
import models.OtherBusinessInvolvement
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


class ObiSummaryControllerISpec extends ControllerISpec {

  def pageUrl(): String = routes.ObiSummaryController.show.url
  def continueUrl(): String = routes.ObiSummaryController.continue.url

  private val otherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testCompanyName),
    hasVrn = Some(true),
    vrn = Some(testVrn),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  )

  val testObis: List[OtherBusinessInvolvement] = List(otherBusinessInvolvement)

  "GET" when {
    "the user has no OBIs" must {
      "redirect to the 'Do you have OBIs' page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(Nil))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl()).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
      }
    }

    "the user has 1 or more OBIs" must {
      "return OK with the view" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl()).get())

        res.status mustBe OK
      }

      "update OBIs with only the completed ones and return OK with the view" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(
            List(otherBusinessInvolvement, otherBusinessInvolvement.copy(businessName = None))
          ))
          .registrationApi.replaceListSection[OtherBusinessInvolvement](testObis)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl()).get())
        res.status mustBe OK
      }

      "return a redirect to 'Do you have OBIs' page if the OBI is missing business name" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis.map(_.copy(businessName = None))))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl()).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
      }
    }
  }

  "POST" when {
    "the user doesn't select an answer" when {
      "the user has no OBIs" must {
        "redirect to the 'Do you have OBIs' page" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(Nil))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(pageUrl()).post(Map("value" -> "")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
        }
      }

      "the user has 1 or more OBIs" must {
        "return BAD_REQUEST with the view" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(pageUrl()).post(Map("value" -> "")))

          res.status mustBe BAD_REQUEST
        }

        "return INTERNAL_SERVER_ERROR if OBI business name missing and form submitted with errors" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis.map(_.copy(businessName = None))))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(pageUrl()).post(Map("value" -> "")))

          res.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
    "the user selects an answer" when {
      "the answer is 'Yes" must {
        "redirect to the 'Other Business Name' page for idx + 1" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(pageUrl()).post(Map("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessNameController.show(2).url)
        }
      }
      "the answer is 'No" must {
        "redirect to the task list page if TaskList FS is on" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(pageUrl()).post(Map("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }
    }
  }

  "POST continue" when {
    "the user chooses to continue after reaching OBI limit" must {
      val limitReachedOBIList = List.fill(10)(otherBusinessInvolvement)

      "redirect to the task list page if TaskList FS is on" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(limitReachedOBIList))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(continueUrl()).post(""))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }
}
