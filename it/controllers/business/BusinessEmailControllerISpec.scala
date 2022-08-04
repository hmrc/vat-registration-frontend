

package controllers.business

import itutil.ControllerISpec
import models.Business
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessEmailControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.BusinessEmailController.show.url
  val businessEmail = "test@test.com"
  val invalidBusinessEmail = "test@@test.com"

  val s4lData: Business = Business(email = Some(businessEmail))

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](Some(businessDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url).get)
      response.status mustBe OK
    }

    "return OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(s4lData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("businessEmailAddress").attr("value") mustBe businessEmail
      }
    }
  }

  s"POST $url" when {
    "BusinessContact is not complete" should {
      "Update S4L and redirect to the next page" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[Business].contains(businessDetails.copy(email = None))
          .registrationApi.getSection[Business](None)
          .s4lContainer[Business].isUpdatedWith(businessDetails.copy(email = Some(businessEmail)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("businessEmailAddress" -> Seq(businessEmail))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)
      }
    }

    "BusinessContact is complete" should {
      "Post the block to the backend and redirect to the next page" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[Business].contains(businessDetails.copy(email = None))
          .registrationApi.replaceSection[Business](businessDetails.copy(email = Some(businessEmail)))
          .s4lContainer[Business].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("businessEmailAddress" -> Seq(businessEmail))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)
      }
    }

    "Return BAD_REQUEST if invalid email provided" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(businessDetails.copy(email = None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url).post(Map("businessEmailAddress" -> Seq(invalidBusinessEmail))))

      response.status mustBe BAD_REQUEST
    }
  }
}
