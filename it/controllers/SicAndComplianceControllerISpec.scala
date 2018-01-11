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

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import helpers.RequestsFinder
import models.ModelKeys.SIC_CODES_KEY
import models.S4LVatSicAndCompliance
import models.api.{SicCode, VatSicAndCompliance}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.JsString
import support.AppAndStubs

class SicAndComplianceControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder {
  "POST Main Business Activity page" should {
    "upsert SIC and Compliance in backend" in {
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

      val s4l = S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription(businessActivityDescription)),
        mainBusinessActivity = Some(mainBusinessActivityView)
      )

      val s4lWithoutCompliance = S4LVatSicAndCompliance.dropLabour(s4l)

      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .keystoreInScenario.hasKeyStoreValue(SIC_CODES_KEY, jsonListSicCode, Some("Current Profile"))
        .s4lContainerInScenario[S4LVatSicAndCompliance].isEmpty(Some(STARTED))
        .vatScheme.isBlank
        .s4lContainerInScenario[S4LVatSicAndCompliance].isUpdatedWith(mainBusinessActivityView, Some(STARTED), Some("Sic Code updated"))
        .s4lContainerInScenario[S4LVatSicAndCompliance].contains(s4l, Some("Sic Code updated"))
        .s4lContainerInScenario[S4LVatSicAndCompliance].isUpdatedWith(s4lWithoutCompliance, Some("Sic Code updated"), Some("Drop all compliance updated"))
        .s4lContainerInScenario[S4LVatSicAndCompliance].contains(s4lWithoutCompliance, Some("Drop all compliance updated"))
        .vatScheme.isUpdatedWith[VatSicAndCompliance](S4LVatSicAndCompliance.apiT.toApi(s4lWithoutCompliance))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient("/main-source-of-income").post(Map("mainBusinessActivityRadio" -> Seq(sicCodeId)))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/sic-and-compliance")
        (json \ "businessDescription").as[JsString].value mustBe businessActivityDescription
        (json \ "mainBusinessActivity" \ "id").as[JsString].value mustBe sicCodeId
        (json \ "mainBusinessActivity" \ "description").as[JsString].value mustBe sicCodeDesc
        (json \ "mainBusinessActivity" \ "displayDetails").as[JsString].value mustBe sicCodeDisplay
      }
    }
  }
}
