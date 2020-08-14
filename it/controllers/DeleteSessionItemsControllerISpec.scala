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

import models.test.SicStub
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class DeleteSessionItemsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  class Setup {

    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5 seconds

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

  "deleteVatRegistration" should {
    "return an OK" in new Setup {

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vrefe.deleteVREFESession()
        .s4lContainer[SicStub].cleared
        //.keystore.deleteKS()
        .vatScheme.deleted

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildInternalClient("/1/delete")
        .withHeaders("X-Session-ID" -> "session-1112223355556")
        .delete()

      whenReady(response) {
        _.status mustBe 200
      }
    }
  }

  "deleteIfRejected" should {
    "return an OK" when {
      "the incorp update is accepted" in {
        val txId: String = "000-431-TEST"

        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val json = Json.parse(
          s"""{
            "SCRSIncorpStatus" : {
              "IncorpSubscriptionKey" : {
                "transactionId":"$txId"
              },
              "IncorpStatusEvent" : {
                "status":"acccepted"
              }
            }
          }""".stripMargin).as[JsObject]

        val response = buildInternalClient("/incorp-update").post(json)

        whenReady(response) {
          _.status mustBe 200
        }
      }

      "the incorp update is not accepted" in {
        val txId: String = "000-431-TEST"

        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatRegistration.clearsUserData()

        val json = Json.parse(
          s"""{
            "SCRSIncorpStatus" : {
              "IncorpSubscriptionKey" : {
                "transactionId":"$txId"
              },
              "IncorpStatusEvent" : {
                "status":"rejected"
              }
            }
          }""".stripMargin).as[JsObject]

        val response = buildInternalClient("/incorp-update").post(json)

        whenReady(response) {
          _.status mustBe 200
        }
      }
    }
  }
}
