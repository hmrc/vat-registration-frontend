

package controllers.registration.applicant

import itutil.ControllerISpec
import models.PartnerEntity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class LeadPartnerEntityControllerISpec extends ControllerISpec {

  val url: String = controllers.registration.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url

  s"GET $url" should {
    "display the page" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "display the page with pre-pop" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.has("partners", Json.toJson(List(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" when {
    List(Individual, NETP).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in session repo and begin a STI journey" in new Setup {
          given()
            .user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(partyType))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.SoleTraderIdentificationController.startPartnerJourney.url)
        }
      }
    }

    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in session repo and begin an IncorpId journey" in new Setup {
          given()
            .user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(partyType))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.IncorpIdController.startPartnerJourney.url)
        }
      }
    }

    List(ScotLtdPartnership, LtdLiabilityPartnership).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in session repo and begin an PartnershipId journey" in new Setup {
          given()
            .user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(partyType))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnershipIdController.startPartnerJourney.url)
        }
      }
    }

    s"the user selects $ScotPartnership" should {
      "store the partyType in session repo and go to ScottishPartnershipName page" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> PartyType.stati(ScotPartnership))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.ScottishPartnershipNameController.show.url)
      }
    }

    "the user submits an invalid lead partner" should {
      "throw an exception" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient("/lead-partner-entity").post(Map("value" -> "55")))

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
