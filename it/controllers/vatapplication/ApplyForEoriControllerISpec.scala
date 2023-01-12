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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import models.api.{EligibilitySubmissionData, NETP}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ApplyForEoriControllerISpec extends ControllerISpec {

  s"GET ${routes.ApplyForEoriController.show.url}" must {
    "return OK when trading details aren't stored" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).select("main p:nth-of-type(1)").first().text() mustBe
          "The business may need an Economic Operators Registration and Identification number (EORI number) if it moves goods:"
      }
    }

    "return OK for overseas page when trading details aren't stored" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NETP,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.getSection[VatApplication](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).select("main p:nth-of-type(1)").first().text() mustBe
          "If your business is not based in the country you’re moving goods to or from, you should get an EORI number if you’re:"
      }
    }

    "return OK when trading details are stored in backend" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(VatApplication(tradeVatGoodsOutsideUk = Some(false), eoriRequested = Some(false))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "No"
      }
    }
    "return OK when trading details are stored in the backend" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[VatApplication](Some(VatApplication(eoriRequested = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST ${routes.ApplyForEoriController.submit.url}" must {
    "redirect to the next page" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(tradeVatGoodsOutsideUk = Some(true))))
        .registrationApi.replaceSection[VatApplication](VatApplication(tradeVatGoodsOutsideUk = Some(true), eoriRequested = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.vatapplication.routes.TurnoverEstimateController.show.url)
      }
    }

    "return BAD_REQUEST if no radio option selected" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
