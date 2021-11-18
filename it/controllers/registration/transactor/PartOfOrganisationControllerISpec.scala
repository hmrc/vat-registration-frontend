
package controllers.registration.transactor

import forms.PartOfOrganisationForm._
import controllers.registration.transactor.{routes => transactorRoutes}
import itutil.ControllerISpec
import models._
import play.api.http.HeaderNames
import scala.concurrent.Future
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class PartOfOrganisationControllerISpec extends ControllerISpec {

  val url: String = transactorRoutes.PartOfOrganisationController.show().url

  val testDetails = TransactorDetails(
    isPartOfOrganisation = Some(true)
  )

  s"GET $url" should {
    "show the view" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    s"POST $url" should {
      "redirect to Organisation Name page when yes is selected" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .vatScheme.contains(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("true")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.OrganisationNameController.show().url)
        }
      }
      "redirect to Declaration Capacity page when no is selected" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .vatScheme.contains(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("false")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.DeclarationCapacityController.show().url)
        }
      }
    }
  }
}