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

package features.sicAndCompliance.controllers

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import features.sicAndCompliance.models.SicAndCompliance.{sicAndCompliance => sicAndCompKey}
import features.sicAndCompliance.models._
import features.sicAndCompliance.models.test.SicStub
import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import models.ModelKeys
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import repositories.ReactiveMongoRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class SicAndComplianceControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {
  val sicCodeId = "81300003"
  val sicCodeDesc = "test2 desc"
  val sicCodeDisplay = "test2 display"
  val businessActivityDescription = "test business desc"

  val jsonListSicCode = s"""
                           |  [
                           |    {
                           |      "id": "01110004",
                           |      "description": "gdfgdg d",
                           |      "displayDetails": "dfg dfg g fd"
                           |    },
                           |    {
                           |      "id": "$sicCodeId",
                           |      "description": "$sicCodeDesc",
                           |      "displayDetails": "$sicCodeDisplay"
                           |    },
                           |    {
                           |      "id": "82190004",
                           |      "description": "ry rty try rty ",
                           |      "displayDetails": " rtyrtyrty rt"
                           |    }
                           |  ]
        """.stripMargin

  val mainBusinessActivityView = MainBusinessActivityView(sicCodeId, Some(SicCode(sicCodeId, sicCodeDesc, sicCodeDisplay)))

  val fullModel = SicAndCompliance(
    description = Some(BusinessActivityDescription(businessActivityDescription)),
    mainBusinessActivity = Some(mainBusinessActivityView),
    companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
    workers =  Some(Workers(200)),
    temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
    skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
  )

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

    def insertCurrentProfileSicCodeIntoDb(sessionId : String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val sicCodeMapping: Map[String, JsValue] = Map(
        "CurrentProfile"        -> Json.toJson(currentProfile),
        ModelKeys.SIC_CODES_KEY -> Json.parse(jsonListSicCode)
      )
      val res = customAwait(repo.upsert(CacheMap(sessionId, sicCodeMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }


  "MainBusinessActivity on show returns 200" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileSicCodeIntoDb(sessionId)

    val response = buildClient("/main-source-of-income").get()
    whenReady(response) { res =>
      res.status mustBe 200
    }
  }

  "MainBusinessActivity on submit returns 303 vat Scheme is upserted because the model is NOW complete" in new Setup {

    val incompleteModelWithoutSicCode = fullModel.copy(
      mainBusinessActivity = None,
      companyProvideWorkers = None,
      workers = None,
      temporaryContracts = None,
      skilledWorkers = None
    )
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(incompleteModelWithoutSicCode)
      .vatScheme.isUpdatedWith[SicAndCompliance](incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivityView)))
      .s4lContainer.cleared
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileSicCodeIntoDb(sessionId)

    val response = buildClient("/main-source-of-income").post(Map("mainBusinessActivityRadio" -> Seq(sicCodeId)))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingDetailsController.tradingNamePage().url)
             val json = getPATCHRequestJsonBody(s"/vatreg/1/sicAndComp")

              (json \ "businessDescription").as[JsString].value mustBe businessActivityDescription
              (json \ "mainBusinessActivity" \ "id").as[JsString].value mustBe sicCodeId
              (json \ "mainBusinessActivity" \ "description").as[JsString].value mustBe sicCodeDesc
              (json \ "mainBusinessActivity" \ "displayDetails").as[JsString].value mustBe sicCodeDisplay
    }
  }

  "CompanyProvideWorkers should return 200 on Show AND users answer is prepopped on page" in new Setup {

    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-workers-to-other-employers").get()
    whenReady(response) { res =>
      res.status mustBe 200
      val document = Jsoup.parse(res.body)
      document.getElementById("companyProvideWorkersRadio-provide_workers_yes").attr("checked") mustBe "checked"
    }
  }
  "CompanyProvideWorkers should return 500 if not authorised on show" in new Setup {

    given()
      .user.isNotAuthorised
      .audit.writesAudit()
      .audit.writesAuditMerged()

    val response = buildClient("/provides-workers-to-other-employers").get()
    whenReady(response) { res =>
      res.status mustBe 500
    }
  }


  "CompanyProvideWorkers return 303 on submit to populate S4l not vat as model is incomplete" in new Setup {

    val incompleteModel = fullModel.copy(
      description = None
    )
    val toBeUpdatedModel = incompleteModel.copy(
      companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)))

    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(incompleteModel)
      .s4lContainer[SicAndCompliance].isUpdatedWith(toBeUpdatedModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-workers-to-other-employers").post(
      Map("companyProvideWorkersRadio" -> Seq(CompanyProvideWorkers.PROVIDE_WORKERS_YES)))

    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(features.sicAndCompliance.controllers.routes.LabourComplianceController.showWorkers().url)
    }
  }

  "SkilledWorkers should return 200 on show and users answer is prepopped on page" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-skilled-workers").get()
    whenReady(response) { res =>
      res.status mustBe 200
      val document = Jsoup.parse(res.body)
      document.getElementById("skilledWorkersRadio-skilled_workers_yes").attr("checked") mustBe "checked"

    }
  }
  "SkilledWorkers should return 303 on submit whereby model was already complete so vat backend is updated instead of s4l" in new Setup {
    given()
      .user.isAuthorised
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))))
      .s4lContainer[SicAndCompliance].cleared

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-skilled-workers").post(Map("skilledWorkersRadio" -> Seq(SkilledWorkers.SKILLED_WORKERS_YES)))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingDetailsController.tradingNamePage().url)

    }
  }
  "SkilledWorkers should return 500 where user is unauthorised on post" in new Setup {
    given()
      .user.isNotAuthorised
      .audit.writesAudit()
      .audit.writesAuditMerged()

    val response = buildClient("/provides-skilled-workers").post(Map("skilledWorkersRadio" -> Seq(SkilledWorkers.SKILLED_WORKERS_YES)))
    whenReady(response) { res =>
      res.status mustBe 500

    }
  }
  "SkilledWorkers should return 500 whereby vat backend returns a 500" in new Setup {
    given()
      .user.isAuthorised
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .vatScheme.isNotUpdatedWith[SicAndCompliance](fullModel.copy(skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))))

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-skilled-workers").post(Map("skilledWorkersRadio" -> Seq(SkilledWorkers.SKILLED_WORKERS_YES)))
    whenReady(response) { res =>
      res.status mustBe 500

    }
  }

  "TemporaryContracts should return 200 on show and users answer is prepopped on page" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-workers-on-temporary-contracts").get()
    whenReady(response) { res =>
      res.status mustBe 200
      val document = Jsoup.parse(res.body)
      document.getElementById("temporaryContractsRadio-temp_contracts_yes").attr("checked") mustBe "checked"

    }
  }
  "TemporaryContracts should return 303 on submit" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES))))
      .s4lContainer[SicAndCompliance].cleared
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/provides-workers-on-temporary-contracts").post(Map("temporaryContractsRadio" -> Seq(TemporaryContracts.TEMP_CONTRACTS_YES)))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(features.sicAndCompliance.controllers.routes.LabourComplianceController.showSkilledWorkers().url)

    }
  }

  "Workers should return 200 on show and users answer is prepopped on page" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/how-many-workers-does-company-provide-at-one-time").get()
    whenReady(response) { res =>
      res.status mustBe 200
      val document = Jsoup.parse(res.body)
      document.getElementById("numberOfWorkers").attr("value") mustBe fullModel.workers.get.numberOfWorkers.toString

    }
  }
  "Workers should return 303 on submit" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(200))))
      .s4lContainer[SicAndCompliance].cleared
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/how-many-workers-does-company-provide-at-one-time").post(Map("numberOfWorkers" -> Seq("200")))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(features.sicAndCompliance.controllers.routes.LabourComplianceController.showTemporaryContracts().url)

    }
  }

  "BusinessActivityDescription should return 200 on show and users answer is prepopped on page" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/describe-what-company-does").get()
    whenReady(response) { res =>
      res.status mustBe 200
      val document = Jsoup.parse(res.body)
      document.getElementById("description").html() mustBe fullModel.description.get.description
    }
  }
  "BusinessActivityDescription should return 303 on submit" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicAndCompliance].contains(fullModel)
      .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(description = Some(BusinessActivityDescription("foo"))))
      .s4lContainer[SicAndCompliance].cleared
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/describe-what-company-does").post(Map("description" -> Seq("foo")))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(features.sicAndCompliance.controllers.test.routes.SicStubController.show().url)

    }
  }

  "ComplianceIntroduction should return 200 on show" in new Setup {
    given()
      .user.isAuthorised
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/tell-us-more-about-the-company").get()
    whenReady(response) { res =>
      res.status mustBe 200
    }
  }

  "ComplianceIntroduction should return 303 for labour sic code on submit" in new Setup {
    given()
      .user.isAuthorised
      .s4lContainer[SicStub].contains(SicStub(Some("42110123"), Some("42910123"), None, None))
      .audit.writesAudit()
      .audit.writesAuditMerged()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/tell-us-more-about-the-company").post(Map("" -> Seq("")))
    whenReady(response) { res =>
      res.status mustBe 303
      res.header(HeaderNames.LOCATION) mustBe Some(features.sicAndCompliance.controllers.routes.LabourComplianceController.showProvideWorkers().url)
    }
  }

}