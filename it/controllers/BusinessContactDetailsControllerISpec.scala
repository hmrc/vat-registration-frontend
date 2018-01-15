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
import models.S4LVatContact
import models.api.{ScrsAddress, VatContact}
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.JsString
import support.AppAndStubs

class BusinessContactDetailsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder {
  "POST Business Contact Details page" should {
    "upsert Vat Contact in backend" in {
      val email = "test@test.com"
      val mobile = "07123456789"

      val addrLine1 = "8 Case Dodo"
      val addrLine2 = "seashore next to the pebble beach"
      val postcode = "TE1 1ST"

      val ppob = ScrsAddress(line1 = addrLine1, line2 = addrLine2, postcode = Some(postcode))
      val s4lVatContact = S4LVatContact(
        businessContactDetails = Some(BusinessContactDetails(email = email, mobile = Some(mobile))),
        ppob = Some(PpobView("123456", Some(ppob)))
      )

      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .s4lContainerInScenario[S4LVatContact].isEmpty(Some(STARTED))
        .s4lContainerInScenario[S4LVatContact].isUpdatedWith(s4lVatContact, Some(STARTED), Some("Vat Contact updated"))
        .vatScheme.isBlank
        .s4lContainerInScenario[S4LVatContact].contains(s4lVatContact, Some("Vat Contact updated"))
        .vatScheme.isUpdatedWith[VatContact](S4LVatContact.apiT.toApi(s4lVatContact))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq(email), "mobile" -> Seq(mobile)))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatTradingDetails.routes.TradingNameController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/vat-contact")
        (json \ "digitalContact" \ "email").as[JsString].value mustBe email
        (json \ "digitalContact" \ "mobile").as[JsString].value mustBe mobile
        (json \ "ppob" \ "line1").as[JsString].value mustBe addrLine1
        (json \ "ppob" \ "line2").as[JsString].value mustBe addrLine2
        (json \ "ppob" \ "postcode").as[JsString].value mustBe postcode
      }
    }
  }
}
