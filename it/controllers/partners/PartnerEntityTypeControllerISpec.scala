

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

class PartnerEntityTypeControllerISpec extends ControllerISpec {

  val url: Int => String = (idx: Int) =>  controllers.partners.routes.PartnerEntityTypeController.showPartnerType(idx).url

  s"GET $url" should {
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
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" when {
    s"the user selects a individual person party type" should {
      "store the partyType in backend and begin a STI journey" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))
          .registrationApi.replaceSection(Entity(None, Individual, Some(false), None, None, None, None), idx = Some(partnerIndex))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> PartyType.stati(Individual))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerSoleTraderIdController.startJourney(partnerIndex).url)
      }
    }

    s"the user selects a business party type" should {
      "not store the partyType in backend and begin a business party type selection" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> PartyType.stati(BusinessEntity))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.partners.routes.BusinessPartnerEntityTypeController.showPartnerType(partnerIndex).url)
      }
    }

    "the user submits an invalid lead partner" should {
      "throw an exception" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> "55")))
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
