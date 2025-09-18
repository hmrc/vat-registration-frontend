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

package controllers

import common.enums.VatRegStatus
import itutil.ControllerISpec
import models.api.VatSchemeHeader
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class ManageRegistrationsControllerISpec extends ControllerISpec {

  val url = "/manage-registrations"

  def vatSchemeHeader(regId: String, status: VatRegStatus.Value): VatSchemeHeader = VatSchemeHeader(
    registrationId = regId,
    status = status,
    applicationReference = Some(s"Application for $regId"),
    createdDate = testCreatedDate,
    requiresAttachments = false
  )

  "GET /manage-registrations" must {
    "return OK and present a list of only draft, submitted or contact registrations" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getAllRegistrations(List(
          vatSchemeHeader(testRegId, VatRegStatus.submitted),
          vatSchemeHeader("2", VatRegStatus.draft),
          vatSchemeHeader("3", VatRegStatus.contact)
        ))

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("tr a").text mustBe s"Application for $testRegId Application for 2 Application for 3"
    }
  }

}
