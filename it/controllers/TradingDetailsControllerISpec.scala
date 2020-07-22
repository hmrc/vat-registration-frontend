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

import com.github.tomakehurst.wiremock.client.WireMock.{findAll, postRequestedFor, urlMatching}
import models.{TradingDetails, TradingNameView}
import helpers.RequestsFinder
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

class TradingDetailsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {
  val companyName = "Test Company Ltd"

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

  "show Trading Name page" should {
    "return 200 and populated trading name field from pre pop" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .company.nameIs(companyName)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .businessRegistration.returnsGETTradingNamePrePopResponse("1", Some("foo bar from pre pop"), 200)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").get()
      whenReady(response) { res =>
        res.status mustBe 200

        val document = Jsoup.parse(res.body)
        val elems = document.getElementById("pageHeading")
        elems.text must include(companyName)
        document.getElementById("tradingName").`val` mustBe "foo bar from pre pop"
        document.getElementById("tradingNameRadio-false").attr("checked") mustBe "checked"
      }
    }
    "return 200 and no pre pop populated or answers populated if s4l returns nothing and pre pop returns nothing" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .vatScheme.doesNotHave("trading-details")
        .company.nameIs(companyName)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .businessRegistration.returnsGETTradingNamePrePopResponse("1", None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").get()
      whenReady(response) { res =>
        res.status mustBe 200

        val document = Jsoup.parse(res.body)
        val elems = document.getElementById("pageHeading")
        elems.text must include(companyName)
        document.getElementById("tradingName").`val` mustBe ""
        document.getElementById("tradingNameRadio-false").attr("checked") mustBe ""
        document.getElementById("tradingNameRadio-true").attr("checked") mustBe ""
      }
    }
  }
  "submit Trading Name page" should {
    "return 303 and post to pre pop" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .company.nameIs(companyName)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.isUpdatedWith(tradingDetails.copy(tradingNameView = Some(TradingNameView(true, Some("Test Trading Name")))))
        .businessRegistration.postsTradingNameToPrepop("1", Some("Test Trading Name"))
        .s4lContainer[TradingDetails].cleared

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post(Map("tradingNameRadio" -> Seq("true"), "tradingName" -> Seq("Test Trading Name")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingDetailsController.euGoodsPage().url)
        val prePopPost = findAll(postRequestedFor(urlMatching(s"/business-registration/1/trading-name")))
        val jsonOfPrePopPost =  Json.parse(prePopPost.get(0).getBodyAsString)
        (jsonOfPrePopPost \ "tradingName").as[String] mustBe "Test Trading Name"
      }
    }
  }
}