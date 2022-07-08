
package controllers

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{EligibilitySubmissionData, UkCompany}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.Format
import play.api.test.Helpers._

import scala.collection.JavaConverters._

class TaskListControllerISpec extends ControllerISpec {

  val url = "/application-progress"

  object ExpectedMessages {
    val section1 = new {
      val heading = "1. Check before you start"
      val row1 = "Registration reason Completed"
    }

    val section2 = new {
      val heading = "2. Verify the business"
      val row1 = "Business information Completed"
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
      "return OK and render all relevant rows when all data is present in the BE" in new Setup {
        enable(TaskList)

        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData),
          applicantDetails = Some(validFullApplicantDetails)
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
      }
      "return OK and render all relevant rows when Applicant Details is partially complete in S4L" in new Setup {
        enable(TaskList)

        val scheme = emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData))

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](None)
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
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
