
package controllers.registration.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

import java.time.LocalDate

class VoluntaryStartDateNoChoiceControllerISpec extends ControllerISpec {

  val url = "/voluntary-vat-start-date"
  val testDate = LocalDate.of(2022, 2, 16)

  def fieldSelector(unit: String) = s"input[id=startDate.$unit]"

  "GET /voluntary-vat-start-date" when {
    "the user has previously provided a vat start date" when {
      "when S4L is empty and all data is in the backend" must {
        "return OK with the form populated" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[Returns].isEmpty
            .vatScheme.has(Returns.apiKey.key, Json.toJson(Returns(startDate = Some(testDate))))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.select(fieldSelector("day")).`val`() mustBe testDate.getDayOfMonth.toString
          doc.select(fieldSelector("month")).`val`() mustBe testDate.getMonthValue.toString
          doc.select(fieldSelector("year")).`val`() mustBe testDate.getYear.toString
        }
      }
      "when the data is stored in S4L" must {
        "return OK with the form populated" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[Returns].contains(Returns(startDate = Some(testDate)))
            .vatScheme.doesNotHave(Returns.apiKey.key)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

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
          .s4lContainer[Returns].isEmpty
          .vatScheme.doesNotHave(Returns.apiKey.key)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

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
    "the date entered is valid" when {
      "the returns block in S4L is complete" must {
        "store the data and redirect to the Returns Frequency page" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[Returns].contains(fullReturns)
            .vatScheme.isUpdatedWith[Returns](fullReturns.copy(startDate = Some(testDate)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj(
            "startDate.day" -> testDate.getDayOfMonth.toString,
            "startDate.month" -> testDate.getMonthValue.toString,
            "startDate.year" -> testDate.getYear.toString
          )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnsController.returnsFrequencyPage.url)
        }
      }
      "the returns block in S4L block is incomplete" must {
        "Update S4L and redirect to the Returns Frequency page" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[Returns].contains(returns)
            .s4lContainer[Returns].isUpdatedWith(returns.copy(startDate = Some(testDate)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj(
            "startDate.day" -> testDate.getDayOfMonth.toString,
            "startDate.month" -> testDate.getMonthValue.toString,
            "startDate.year" -> testDate.getYear.toString
          )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnsController.returnsFrequencyPage.url)
        }
      }
    }
    "the data entered is invalid" must {
      "return BAD REQUEST" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj(
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        )))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
