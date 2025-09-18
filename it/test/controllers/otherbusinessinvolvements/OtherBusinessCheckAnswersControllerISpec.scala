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

class OtherBusinessCheckAnswersControllerISpec extends ControllerISpec {

  def url(index: Int) = s"/other-business-involvement/$index/check-answers"

  val testIndexBelow1: Int = -1
  val testIndex1 = 1
  val testIndex2 = 2
  val testIndex3 = 3
  val testIndex4 = 4
  val testIndexMax = 10
  val testIndexOverMax = 12

  val testVatNumber = "testVatNumber"
  val testOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testCompanyName),
    hasVrn = Some(true),
    vrn = Some(testVatNumber),
    stillTrading = Some(true)
  )

  "GET /other-business-involvements/check-answers/:index" when {
    "the section is complete and stored in the back end" must {
      "return OK when the user has a VRN" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[OtherBusinessInvolvement](Some(testOtherBusinessInvolvement), idx = Some(testIndex1))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url(testIndex1)).get())

        res.status mustBe OK
      }
    }
    "no data exists for the index" when {
      "the index is valid" must {
        "but higher than highest valid index, then redirect to max index" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex2))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List()))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndex2)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex1).url)
        }

        "and no data available, then redirect business name controller at given index" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex2))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List()))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndex1)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessNameController.show(testIndex1).url)
        }
      }

      "the index is higher than the highest possible index (n + 1)" must {
        "redirect to the first index without data" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex3))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndex3)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex2).url)
        }
        "redirect to the highest possible index" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex4))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndex4)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex2).url)
        }
      }
      "the index is higher than the allowed maximum (10)" must {
        "redirect to index 10" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndexOverMax))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement,
            testOtherBusinessInvolvement
          )))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndexOverMax)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndexMax).url)
        }
      }
      "the index is less than 1" must {
        "redirect to index 1" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndexBelow1))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url(testIndexBelow1)).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex1).url)
        }
      }
    }
  }

  "POST /other-business-involvements/check-answers" must {
    "redirect to the summary page" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(routes.OtherBusinessCheckAnswersController.submit.url).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
    }
  }

}
