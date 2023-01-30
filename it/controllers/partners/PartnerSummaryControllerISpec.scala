
package controllers.partners

import itutil.ControllerISpec
import models.Entity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class PartnerSummaryControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.PartnerSummaryController.show.url
  val continueUrl: String = routes.PartnerSummaryController.continue.url

  s"GET $pageUrl" when {
    "the journey has no partner entity types" must {
      "redirect to the task list controller page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(Nil))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "the journey has just a single partner entity" must {
      "show the summary page without radios" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(Entity(Some(testPartnership), ScotPartnership, Some(true), Some(testCompanyName), None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttributeValue("type", "radio") size() mustBe 0
      }
    }

    "the journey has multiple partner entities" must {
      "show the summary page with radios" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(
          Entity(Some(testPartnership), ScotPartnership, Some(true), Some(testCompanyName), None, None, None),
          Entity(Some(testIncorpDetails), UkCompany, Some(false), None, Some(address), Some(applicantEmail), Some(testApplicantPhone))
        )))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttributeValue("type", "radio").size() mustBe 2
      }

      "show the summary page with radios and clear down any incomplete partners" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(
          Entity(Some(testPartnership), ScotLtdPartnership, Some(true), None, None, None, None),
          Entity(Some(testIncorpDetails), UkCompany, Some(false), None, Some(address), None, None),
          Entity(Some(testIncorpDetails), UkCompany, Some(false), None, Some(address), Some(applicantEmail), Some(testApplicantPhone)),
          Entity(Some(testIncorpDetails), UkCompany, Some(false), None, Some(address), None, None)
        )))
          .registrationApi.replaceListSection[Entity](List(
          Entity(Some(testPartnership), ScotLtdPartnership, Some(true), None, None, None, None),
          Entity(Some(testIncorpDetails), UkCompany, Some(false), None, Some(address), Some(applicantEmail), Some(testApplicantPhone))
        ))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttributeValue("type", "radio").size() mustBe 2
      }
    }
  }

  s"POST $pageUrl" when {
    val entities: List[Entity] = List(
      Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
      Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
    )

    "the user doesn't select an answer" when {
      "scheme has no partner entities" must {
        "redirect to task list controller" in new Setup {
          given().user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl).post(Map("value" -> "")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "scheme has multiple partner entities" must {
        "return BAD_REQUEST with the view" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl).post(Map("value" -> "")))

          res.status mustBe BAD_REQUEST
        }
      }
    }
    "the user selects an answer" when {
      "the answer is 'Yes" must {
        "redirect to the partner entity type selection page for idx + 1" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(entities.size + 1).url)
        }
      }

      "the answer is 'No" must {
        "redirect to the task list page" in new Setup {
          val testAttachmentDetails: Attachments = Attachments(Some(Attached), None, None, Some(true))

          given()
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))
            .registrationApi.getSection[Attachments](Some(testAttachmentDetails))
            .registrationApi.replaceSection[Attachments](testAttachmentDetails.copy(additionalPartnersDocuments = Some(false)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }
    }
    "the user chooses to continue after reaching partner entity limit" must {
      val limitReachedPartnerEntities = List.fill(10)(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)
      )

      "redirect to the additional partners controller" in new Setup {
        val testAttachmentDetails: Attachments = Attachments(Some(Attached), None, None, Some(true))

        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(limitReachedPartnerEntities))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Attachments](Some(testAttachmentDetails))
          .registrationApi.replaceSection[Attachments](testAttachmentDetails.copy(additionalPartnersDocuments = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AdditionalPartnerEntityController.show.url)
      }
    }
  }

  s"POST $continueUrl" must {
    "redirect to the start of 2nd partner journey" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(continueUrl).post(Map("" -> Seq(""))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
    }
  }
}
