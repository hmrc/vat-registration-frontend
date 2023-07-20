
package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

import java.time.LocalDate

class VoluntaryStartDateNoChoiceControllerISpec extends ControllerISpec {

  val url = "/voluntary-vat-start-date"
  val testDate = LocalDate.of(2022, 2, 16)

  def fieldSelector(unit: String) = s"input[id=startDate.$unit]"

  "GET /voluntary-vat-start-date" when {
    "the user has previously provided a vat start date" when {
      "when backend is empty and all data is in the backend" must {
        "return OK with the form populated" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection(Some(VatApplication(startDate = Some(testDate))))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.select(fieldSelector("day")).`val`() mustBe testDate.getDayOfMonth.toString
          doc.select(fieldSelector("month")).`val`() mustBe testDate.getMonthValue.toString
          doc.select(fieldSelector("year")).`val`() mustBe testDate.getYear.toString
        }
      }
      "when the data is stored in backend" must {
        "return OK with the form populated" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(startDate = Some(testDate))))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.select(fieldSelector("day")).`val`() mustBe testDate.getDayOfMonth.toString
          doc.select(fieldSelector("month")).`val`() mustBe testDate.getMonthValue.toString
          doc.select(fieldSelector("year")).`val`() mustBe testDate.getYear.toString
        }
      }
    }
    "the user hasn't previously provided a vat start date" must {
      "return OK with an empty form" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select(fieldSelector("day")).`val`() mustBe ""
        doc.select(fieldSelector("month")).`val`() mustBe ""
        doc.select(fieldSelector("year")).`val`() mustBe ""
      }
    }
  }

  "POST /voluntary-vat-start-date" when {
    "the date entered is valid" must {
      "Update backend and redirect to the Returns Frequency page" in new Setup {
        val vatApplication: VatApplication = fullVatApplication.copy(turnoverEstimate = None)
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[VatApplication](vatApplication.copy(startDate = Some(testDate)))
          .registrationApi.getSection[VatApplication](Some(vatApplication.copy(startDate = Some(testDate))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(Map(
          "startDate.day" -> testDate.getDayOfMonth.toString,
          "startDate.month" -> testDate.getMonthValue.toString,
          "startDate.year" -> testDate.getYear.toString
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)
      }
    }
    "the data entered is invalid" must {
      "return BAD REQUEST" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(Map(
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        )))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
