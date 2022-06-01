

package controllers.business

import itutil.ControllerISpec
import models.BusinessContact
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessWebsiteAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.BusinessWebsiteAddressController.show.url
  val businessWebsiteAddress = "https://www.example.com"

  val s4lData: BusinessContact = BusinessContact(
    website = Some(businessWebsiteAddress)
  )

  s"GET $url" should {
    "show the view correctly" in new Setup {
      implicit val format: Format[BusinessContact] = BusinessContact.apiFormat
      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].isEmpty
        .registrationApi.getSection[BusinessContact](Some(validBusinessContactDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url).get)
      response.status mustBe OK
    }

    "return OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(s4lData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("businessWebsiteAddress").attr("value") mustBe businessWebsiteAddress
      }
    }
  }

  s"POST $url" when {
    "BusinessContact is not complete" should {
      "Update S4L and redirect to the next page" in new Setup {
        implicit val format: Format[BusinessContact] = BusinessContact.apiFormat

        given()
          .user.isAuthorised()
          .s4lContainer[BusinessContact].contains(BusinessContact())
          .registrationApi.getSection[BusinessContact](None)
          .s4lContainer[BusinessContact].isUpdatedWith(
          BusinessContact().copy(website = Some(businessWebsiteAddress))
        )
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(businessWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
      }
    }

    "BusinessContact is complete" should {
      "Post the block to the backend and redirect to the next page" in new Setup {
        implicit val format: Format[BusinessContact] = BusinessContact.apiFormat

        given()
          .user.isAuthorised()
          .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(website = None))
          .registrationApi.replaceSection[BusinessContact](validBusinessContactDetails.copy(website = Some(businessWebsiteAddress)))
          .s4lContainer[BusinessContact].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(businessWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
      }
    }

    "return 400 when the user submits and invalid regex" in new Setup {
      implicit val format: Format[BusinessContact] = BusinessContact.apiFormat
      val invalidUrl: String = businessWebsiteAddress + "@"

      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(website = None))
        .registrationApi.replaceSection[BusinessContact](validBusinessContactDetails.copy(website = Some(invalidUrl)))
        .s4lContainer[BusinessContact].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(invalidUrl))))

      response.status mustBe BAD_REQUEST
    }
  }
}
