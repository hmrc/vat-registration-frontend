
package controllers.transactor

import forms.PartOfOrganisationForm._
import controllers.transactor.{routes => transactorRoutes}
import itutil.ControllerISpec
import models._
import play.api.http.HeaderNames
import scala.concurrent.Future
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class PartOfOrganisationControllerISpec extends ControllerISpec {

  val url: String = transactorRoutes.PartOfOrganisationController.show.url

  val testDetails: TransactorDetails = TransactorDetails(isPartOfOrganisation = Some(true))

  s"GET $url" should {
    List(None, Some(testDetails)).foreach { transactorDetails =>
      s"show the view with transactor details ${transactorDetails.fold("not set")(_ => "set")}" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](transactorDetails)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe OK
        }
      }
    }

    s"POST $url" should {
      "redirect to Organisation Name page when yes is selected" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .registrationApi.getRegistration(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("true")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.OrganisationNameController.show.url)
        }
      }

      "redirect to Declaration Capacity page when no is selected" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .registrationApi.getRegistration(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("false")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.DeclarationCapacityController.show.url)
        }
      }

      "return BAD_REQUEST if no option selected" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post("")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}