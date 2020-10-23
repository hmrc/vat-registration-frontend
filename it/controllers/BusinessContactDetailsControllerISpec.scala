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

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.BusinessContact
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class BusinessContactDetailsControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

  class Setup {

    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    lazy val repo = app.injector.instanceOf[SessionRepository]
    lazy val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  "GET redirectToAlf" should {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised
        .alfeJourney.initialisedSuccessfully()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.BusinessContactDetailsController.ppobRedirectToAlf().url).get()

      whenReady(response) { res =>
        res.status mustBe 303
      }
    }
    "return 500 when not authorised" in new Setup {
      given()
        .user.isNotAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient(controllers.routes.BusinessContactDetailsController.ppobRedirectToAlf().url).get()
      whenReady(response) { res =>
        res.status mustBe 500

      }
    }
  }

  "returnFromTxm GET" should {
    "return 303 save to vat as model is complete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", addressWithCountry.line1, addressWithCountry.line2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].cleared

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.BusinessContactDetailsController.returnFromTxm(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/business-contact")
        json mustBe validBusinessContactDetailsJson
      }

    }
    "returnFromTxm should return 303 save to s4l as model is incomplete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", addressWithCountry.line1, addressWithCountry.line2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")
        .s4lContainer[BusinessContact].isUpdatedWith(validBusinessContactDetails.copy(companyContactDetails = None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.BusinessContactDetailsController.returnFromTxm(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails().url)
      }
    }
  }
  "showCompanyContactDetails" should {
    "return 200" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/company-contact-details").get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

  }
  "submitCompanyContactDetails" should {
    "return 303 and submit to s4l because the model is incomplete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("foo@foo.com"), "daytimePhone" -> Seq("0121401890")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ContactPreferenceController.showContactPreference().url)

      }
    }
    "return 303 and submit to vat reg because the model is complete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(companyContactDetails = None))
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].cleared

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ContactPreferenceController.showContactPreference().url)
      }
    }
    //TODO - See if this test is still valid
    "return 404 when vat returns a 404" ignore new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .vatScheme.doesNotExistForKey("business-contact")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 404
      }
    }
    "return 500 when update to vat reg returns an error (s4l is not cleared)" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isNotUpdatedWith[BusinessContact](validBusinessContactDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 500
      }
    }
  }
}