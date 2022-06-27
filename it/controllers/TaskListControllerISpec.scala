
package controllers

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import scala.collection.JavaConverters._

class TaskListControllerISpec extends ControllerISpec {

  val url = "/application-progress"

  object ExpectedMessages {
    val section1 = new {
      val heading = "1. Check before you start"
      val row1 = "Registration reason Completed"
    }
  }

  def sectionMustExist(n: Int)(heading: String, rows: List[String])(implicit doc: Document) = {
    val sectionHeadingSelector = ".app-task-list__section"
    val rowSelector = "ul.app-task-list__items"

    doc.select(sectionHeadingSelector).eachText().asScala.toList.lift(n - 1) mustBe Some(heading)
    val items = doc.select(rowSelector).asScala.toList.apply(n - 1)
    items.select("li").eachText().asScala.toList mustBe rows
  }

  "GET /application-progress" when {
    "the TaskList feature switch is enabled" must {
      "return OK and render all relevant rows" in new Setup {
        enable(TaskList)

        val scheme = emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData))

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
      }
    }
    "the TaskList feature switch is disabled" must {
      "return OK and render all relevant sections" in new Setup {
        disable(TaskList)

        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)

        res.status mustBe NOT_FOUND
      }
    }
  }

}
