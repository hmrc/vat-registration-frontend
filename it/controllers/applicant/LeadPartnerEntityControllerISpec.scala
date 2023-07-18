

package controllers.applicant

import itutil.ControllerISpec
import models.Entity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class LeadPartnerEntityControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url

  s"GET $url" must {
    "display the page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](None, idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "display the page for transactor journey" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[Entity](None, idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "display the page with pre-pop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }

    "display the page with pre-pop for a business" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None)), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" when {
    s"the user selects a individual person party type" must {
      "store the partyType in backend and begin a STI journey" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Entity](None, idx = Some(1))
          .registrationApi.replaceSection(Entity(None, Individual, Some(true), None, None, None, None), idx = Some(1))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(Individual))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerSoleTraderIdController.startJourney(1).url)
      }
    }

    s"the user selects a business party type" must {
      "not store the partyType in backend and begin a business party type selection" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(BusinessEntity))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessLeadPartnerEntityController.showPartnerEntityType.url)
      }
    }

    "the user submits an invalid form" must {
      "return the page with errors" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> "")))

        res.status mustBe BAD_REQUEST
      }

      "return the page with errors for transactor journey" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> "")))

        res.status mustBe BAD_REQUEST
      }
    }

    "the user submits an invalid lead partner" must {
      "throw an exception" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> "55")))

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
