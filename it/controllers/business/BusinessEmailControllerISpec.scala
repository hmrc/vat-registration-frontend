

package controllers.business

import itutil.ControllerISpec
import models.BusinessContact
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessEmailControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.BusinessEmailController.show.url
  val businessEmail = "test@test.com"

  val s4lData: BusinessContact = BusinessContact(
    email = Some(businessEmail)
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
        Jsoup.parse(res.body).getElementById("business-email-address").attr("value") mustBe businessEmail
      }
    }
  }

  s"POST $url" when {
    "BusinessContact is not complete" should {
      //TODO Update below line and redirect
      "Update S4L and redirect to the next page" in new Setup {
        implicit val format: Format[BusinessContact] = BusinessContact.apiFormat
        given()
          .user.isAuthorised()
          .s4lContainer[BusinessContact].contains(BusinessContact())
          .registrationApi.getSection[BusinessContact](None)
          .s4lContainer[BusinessContact].isUpdatedWith(
          BusinessContact().copy(email = Some(businessEmail))
        )
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("business-email-address" -> Seq(businessEmail))))
//
//        response.status mustBe SEE_OTHER
//        response.header("LOCATION") mustBe Some(controllers.business.routes.nextPage)
        response.status mustBe NOT_IMPLEMENTED
      }
    }

    "BusinessContact is complete" should {
      //TODO Update below line and redirect
      "Post the block to the backend and redirect to the next page" in new Setup {
        implicit val format: Format[BusinessContact] = BusinessContact.apiFormat
        given()
          .user.isAuthorised()
          .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(email = None))
          .registrationApi.replaceSection[BusinessContact](validBusinessContactDetails.copy(email = Some(businessEmail)))
          .s4lContainer[BusinessContact].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: WSResponse = await(buildClient(url).post(Map("business-email-address" -> Seq(businessEmail))))
//
//        response.status mustBe SEE_OTHER
//        response.header("LOCATION") mustBe Some(controllers.business.routes.nextPage)
        response.status mustBe NOT_IMPLEMENTED
      }
    }
  }

}
