/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import java.time.LocalDate

import features.officer.models.view.LodgingOfficer
import features.returns.models.{Frequency, Returns, Stagger}
import features.sicAndCompliance.models.SicAndCompliance
import it.fixtures.ITRegistrationFixtures
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import repositories.ReactiveMongoRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class SummaryControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with ITRegistrationFixtures {

  val thresholdUrl = s"/vatreg/threshold/${LocalDate.now()}"
  val currentThreshold = "50000"

  val officerJson = Json.parse(
    s"""
       |{
       |  "name": {
       |    "first": "${validOfficer.name.forename}",
       |    "middle": "${validOfficer.name.otherForenames}",
       |    "last": "${validOfficer.name.surname}"
       |  },
       |  "role": "${validOfficer.role}",
       |  "dob": "$officerDob",
       |  "nino": "$officerNino",
       |  "details": {
       |    "currentAddress": {
       |      "line1": "${validCurrentAddress.line1}",
       |      "line2": "${validCurrentAddress.line2}",
       |      "postcode": "${validCurrentAddress.postcode}"
       |    },
       |    "contact": {
       |      "email": "$officerEmail",
       |      "tel": "1234",
       |      "mobile": "5678"
       |    },
       |    "changeOfName": {
       |      "name": {
       |        "first": "New",
       |        "middle": "Name",
       |        "last": "Cosmo"
       |      },
       |      "change": "2000-07-12"
       |    }
       |  }
       |}""".stripMargin)

  class Setup {
    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
    val repo = new ReactiveMongoRepository(app.configuration, mongo)
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId : String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  "GET Summary page" should {
    "display the summary page correctly" when {
      "the company is NOT incorporated" in new Setup {

        given()
          .user.isAuthorised
          .vatScheme.contains(vatReg)
          .vatScheme.has("officer", officerJson)
          .s4lContainer[SicAndCompliance].isEmpty
          .s4lContainer[SicAndCompliance].isUpdatedWith(vatRegIncorporated.sicAndCompliance.get)
          .vatScheme.has("sicAndComp",SicAndCompliance.toApiWrites.writes(vatRegIncorporated.sicAndCompliance.get))
          .s4lContainer[LodgingOfficer].isUpdatedWith(validFullLodgingOfficer)
          .s4lContainer[SicAndCompliance].cleared
          .s4lContainer[Returns].contains(Returns(None, Some(Frequency.quarterly), Some(Stagger.jan), None))
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatRegistration.threshold(thresholdUrl, currentThreshold)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/check-your-answers").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          document.title() mustBe "Summary"

          document.getElementById("frs.joinFrsAnswer").text mustBe "No"
          document.getElementById("vatDetails.overThresholdThirtySelectionAnswer").text mustBe "No"
          document.getElementById("vatDetails.overThresholdThirtySelectionChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/make-more-taxable-sales"
          document.getElementById("vatDetails.necessityAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.necessityChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/register-voluntarily"
          document.getElementById("vatDetails.voluntaryRegistrationReasonAnswer").text mustBe "The company is already selling goods or services"
          document.getElementById("vatDetails.voluntaryRegistrationReasonChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/applies-company"

          document.getElementById("directorDetails.formerNameAnswer").text mustBe "New Name Cosmo"
          document.getElementById("taxableSales.estimatedSalesValueAnswer").text mustBe "£30000"
          document.getElementById("annualAccountingScheme.accountingPeriodAnswer").text mustBe "January, April, July and October"
        }
      }

      "the company is incorporated" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.contains(vatRegIncorporated.copy(flatRateScheme = None))
          .s4lContainer[SicAndCompliance].isEmpty
          .s4lContainer[SicAndCompliance].isUpdatedWith(vatRegIncorporated.sicAndCompliance.get)
          .vatScheme.has("sicAndComp",SicAndCompliance.toApiWrites.writes(vatRegIncorporated.sicAndCompliance.get))
          .vatScheme.has("officer", officerJson)
          .s4lContainer[LodgingOfficer].contains(validFullLodgingOfficer)
          .s4lContainer[SicAndCompliance].cleared
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatRegistration.threshold(thresholdUrl, currentThreshold)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response = buildClient("/check-your-answers").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)

          a[NullPointerException] mustBe thrownBy(document.getElementById("vatDetails.taxableTurnoverQuestion").text)
          document.getElementById("vatDetails.overThresholdSelectionQuestion").text must include("05 August 2016")
          document.getElementById("vatDetails.overThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.overThresholdSelectionChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/gone-over-threshold"
          document.getElementById("vatDetails.overThresholdDateAnswer").text mustBe "January 2017"
          document.getElementById("vatDetails.overThresholdDateChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/gone-over-threshold"
          document.getElementById("vatDetails.expectationOverThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("vatDetails.expectationOverThresholdSelectionChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/gone-over-threshold-period"
          document.getElementById("vatDetails.expectationOverThresholdDateAnswer").text mustBe "30 September 2016"
          document.getElementById("vatDetails.expectationOverThresholdDateChangeLink").attr("href") mustBe s"http://localhost:$wiremockPort/vat-eligibility-uri/gone-over-threshold-period"
        }
      }
    }
  }

  "POST Summary Page" should {
    "redirect to the confirmation page" when {
      "the user is in draft with a vat ready submission" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.contains(vatReg)
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .incorpInformation.cancelsSubscription()
          .vatRegistration.status(s"/vatreg/${vatReg.id}/status", "draft")
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration")

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response = buildClient("/check-your-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/submission-confirmation")
        }
      }
    }
  }
}
