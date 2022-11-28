
package controllers.transactor

import controllers.transactor.{routes => transactorRoutes}
import forms.PartOfOrganisationForm._
import itutil.ControllerISpec
import models._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartOfOrganisationControllerISpec extends ControllerISpec {

  val url: String = transactorRoutes.PartOfOrganisationController.show.url

  val testDetails: TransactorDetails = TransactorDetails(isPartOfOrganisation = Some(true))

  s"GET $url" must {
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

    s"POST $url" must {
      "redirect to Organisation Name page when yes is selected" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .s4lContainer[TransactorDetails].clearedByKey
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(isPartOfOrganisation = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("true")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.OrganisationNameController.show.url)
        }
      }

      "clear organisationName and redirect to Declaration Capacity page when no is selected" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .s4lContainer[TransactorDetails].clearedByKey
          .registrationApi.getSection[TransactorDetails](Some(TransactorDetails(isPartOfOrganisation = Some(true), organisationName = Some("test"))))
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(isPartOfOrganisation = Some(false)))

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