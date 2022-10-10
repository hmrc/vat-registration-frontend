

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

class BusinessLeadPartnerEntityControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.BusinessLeadPartnerEntityController.showPartnerEntityType.url

  s"GET $url" should {
    "display the page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

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
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(None, UkCompany, Some(true), None, None, None, None)), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" when {

    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in backend and begin an IncorpId journey" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
            .registrationApi.replaceSection(Entity(None, partyType, Some(true), None, None, None, None), idx = Some(1))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient("/business-type-in-partnership").post(Map("value" -> PartyType.stati(partyType))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerIncorpIdController.startJourney(1).url)
        }
      }
    }

    List(ScotLtdPartnership, LtdLiabilityPartnership).foreach { partyType =>
      s"the user selects $partyType" should {
        "store the partyType in backend and begin an PartnershipId journey" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
            .registrationApi.replaceSection(Entity(None, partyType, Some(true), None, None, None, None), idx = Some(1))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient("/business-type-in-partnership").post(Map("value" -> PartyType.stati(partyType))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerPartnershipIdController.startJourney(1).url)
        }
      }
    }

    s"the user selects $ScotPartnership" should {
      "store the partyType in backend and go to ScottishPartnershipName page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection(Entity(None, ScotPartnership, Some(true), None, None, None, None), idx = Some(1))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient("/business-type-in-partnership").post(Map("value" -> PartyType.stati(ScotPartnership))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ScottishPartnershipNameController.show.url)
      }
    }

    "the user submits an invalid lead partner" should {
      "throw an exception" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient("/business-type-in-partnership").post(Map("value" -> "55")))

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
