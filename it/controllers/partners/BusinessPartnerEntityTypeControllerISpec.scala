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

package controllers.partners

import itutil.ControllerISpec
import models.Entity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class BusinessPartnerEntityTypeControllerISpec extends ControllerISpec {

  val url: Int => String = (idx: Int) => controllers.partners.routes.BusinessPartnerEntityTypeController.showPartnerType(idx).url

  s"GET business partner entity type" should {
    "display the page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(Entity(None, ScotPartnership, Some(true), None, None, None, None))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "display the page with pre-pop" in new Setup {
      val partnerIndex = 2

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(partnerIndex)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST business partner entity type" when {

    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in backend and begin an IncorpId journey" in new Setup {
          val partnerIndex = 2

          given()
            .user.isAuthorised()
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
            .registrationApi.getListSection[Entity](Some(List(Entity(None, partyType, Some(true), None, None, None, None))))
            .registrationApi.replaceSection(Entity(None, partyType, Some(false), None, None, None, None), idx = Some(partnerIndex))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val formData: Map[String, String] = Map("value" -> PartyType.stati(partyType))
          val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/business-type-in-partnership").post(formData))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerIncorpIdController.startJourney(partnerIndex).url)
        }
      }
    }

    List(ScotLtdPartnership, LtdLiabilityPartnership).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in backend and begin an PartnershipId journey" in new Setup {
          val partnerIndex = 2

          given()
            .user.isAuthorised()
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
            .registrationApi.getListSection[Entity](Some(List(Entity(None, partyType, Some(true), None, None, None, None))))
            .registrationApi.replaceSection(Entity(None, partyType, Some(false), None, None, None, None), idx = Some(partnerIndex))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val formData: Map[String, String] = Map("value" -> PartyType.stati(partyType))
          val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/business-type-in-partnership").post(formData))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerPartnershipIdController.startJourney(partnerIndex).url)
        }
      }
    }

    s"the user selects $ScotPartnership" should {
      "store the partyType in backend and go to ScottishPartnershipName page" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getListSection[Entity](Some(List(Entity(None, ScotPartnership, Some(true), None, None, None, None))))
          .registrationApi.replaceSection(Entity(None, ScotPartnership, Some(false), None, None, None, None), idx = Some(partnerIndex))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val formData: Map[String, String] = Map("value" -> PartyType.stati(ScotPartnership))
        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/business-type-in-partnership").post(formData))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.partners.routes.PartnerScottishPartnershipNameController.show(partnerIndex).url)
      }
    }

    "the user submits an invalid business partner type" should {
      "throw an exception" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(Entity(None, ScotPartnership, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/business-type-in-partnership").post(Map("value" -> "55")))

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
