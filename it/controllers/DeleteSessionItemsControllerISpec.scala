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

import itutil.ControllerISpec
import models.external.{IncorpStatusEvent, IncorpSubscription, IncorporationInfo}
import models.test.SicStub
import play.api.libs.json.Json
import play.api.test.Helpers._

class DeleteSessionItemsControllerISpec extends ControllerISpec {

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
        _.status mustBe OK
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

        val json = Json.toJson(
          IncorporationInfo(
            IncorpSubscription(
              transactionId = txId,
              regime = "vat",
              subscriber = "scrs",
              callbackUrl = "backUrl"),
            IncorpStatusEvent(
              status = "accepted",
              crn = Some("90000001"),
              incorporationDate = Some(LocalDate.parse("2016-08-05")),
              description = None)
          )
        )

        val response = buildInternalClient("/incorp-update").post(json)

        whenReady(response) {
          _.status mustBe OK
        }
      }

      "the incorp update is not accepted" in {
        val txId: String = "000-431-TEST"

        given()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatRegistration.clearsUserData()

        val json = Json.toJson(
          IncorporationInfo(
            IncorpSubscription(
              transactionId = txId,
              regime = "vat",
              subscriber = "scrs",
              callbackUrl = "backUrl"),
            IncorpStatusEvent(
              status = "rejected",
              crn = Some("90000001"),
              incorporationDate = Some(LocalDate.parse("2016-08-05")),
              description = None)
          )
        )

        val response = buildInternalClient("/incorp-update").post(json)

        whenReady(response) {
          _.status mustBe OK
        }
      }
    }
  }
}
