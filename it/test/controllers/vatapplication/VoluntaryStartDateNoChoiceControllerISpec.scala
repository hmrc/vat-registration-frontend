/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate

class VoluntaryStartDateNoChoiceControllerISpec extends ControllerISpec {

  val url = "/voluntary-vat-start-date"
  val testDate: LocalDate = LocalDate.now.minusYears(2)

  def fieldSelector(unit: String) = s"input[id=startDate.$unit]"

  "GET /voluntary-vat-start-date" when {
    "the user has previously provided a vat start date" when {
      "when backend is empty and all data is in the backend" must {
        "return OK with the form populated" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection(Some(VatApplication(startDate = Some(testDate))))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).get())
          val doc: Document = Jsoup.parse(res.body)

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

          val res: WSResponse = await(buildClient(url).get())
          val doc: Document = Jsoup.parse(res.body)

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

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document = Jsoup.parse(res.body)

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
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[VatApplication](vatApplication.copy(startDate = Some(testDate)))
          .registrationApi.getSection[VatApplication](Some(vatApplication.copy(startDate = Some(testDate))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(
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
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        )))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
