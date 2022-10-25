
package controllers.partners

import controllers.partners.PartnerIndexValidation.minPartnerIndex
import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.Entity
import models.api._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class PartnerSummaryControllerISpec extends ControllerISpec {

  def pageUrl(): String = routes.PartnerSummaryController.show.url

  "GET" when {
    "the journey has no partner entity types" must {
      "redirect to the task list controller page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(Nil))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl()).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "the journey has just a single partner entity" must {
      "redirect to partner entity type selection page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(Entity(None, ScotPartnership, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl()).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(minPartnerIndex).url)
      }
    }
  }

  "POST" when {
    val entities: List[Entity] = List(
      Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
      Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
    )

    "the user doesn't select an answer" when {
      "scheme has multiple partner entities" must {
        "return BAD_REQUEST with the view" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "")))

          res.status mustBe BAD_REQUEST
        }
      }
    }
    "the user selects an answer" when {
      "the answer is 'Yes" must {
        "redirect to the partner entity type selection page for idx + 1" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(entities.size + 1).url)
        }
      }

      "the answer is 'No" must {
        "redirect to the task list page if TaskList FS is on" in new Setup {
          val testAttachmentDetails: Attachments = Attachments(Some(Attached), None, None, Some(true))

          enable(TaskList)
          given
            .user.isAuthorised()
            .registrationApi.getListSection[Entity](Some(entities))
            .registrationApi.getSection[Attachments](Some(testAttachmentDetails))
            .registrationApi.replaceSection[Attachments](testAttachmentDetails.copy(additionalPartnersDocuments = Some(false)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
          disable(TaskList)
        }
      }
    }
  }

  "POST" when {
    "the user chooses to continue after reaching partner entity limit" must {
      val limitReachedPartnerEntities = List.fill(10)(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
      )

      "redirect to the additional partners controller" in new Setup {
        val testAttachmentDetails: Attachments = Attachments(Some(Attached), None, None, Some(true))

        given
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(limitReachedPartnerEntities))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Attachments](Some(testAttachmentDetails))
          .registrationApi.replaceSection[Attachments](testAttachmentDetails.copy(additionalPartnersDocuments = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AdditionalPartnerEntityController.show.url)
      }
    }
  }
}
