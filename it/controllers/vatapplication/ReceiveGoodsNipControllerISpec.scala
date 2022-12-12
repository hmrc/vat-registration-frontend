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
import models.api._
import models.api.vatapplication.VatApplication
import models.{ConditionalValue, NIPTurnover, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class ReceiveGoodsNipControllerISpec extends ControllerISpec {
  val testAmount: BigDecimal = 123456
  lazy val url: String = controllers.vatapplication.routes.ReceiveGoodsNipController.show.url
  val testNIPCompliance: NIPTurnover = NIPTurnover(None, Some(ConditionalValue(true, Some(testAmount))))

  "show Northern Ireland Receive page" should {
    "return OK with no prepop when there is no value for 'receiveGoods' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "Return OK with prepop when there is a value for 'receiveGoods' in the backend" in {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  "submit Receive Goods page" should {
    "redirect to the claim refund page when NETP" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))
        .registrationApi.replaceSection[VatApplication](VatApplication(northernIrelandProtocol = Some(NIPTurnover(goodsToEU = None, goodsFromEU = Some(ConditionalValue(true, Some(testAmount)))))))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ClaimRefundsController.show.url)
      }
    }

    "redirect to the returns frequency page when NonUkNonEstablished" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.replaceSection[VatApplication](VatApplication(northernIrelandProtocol = Some(NIPTurnover(goodsToEU = None, goodsFromEU = Some(ConditionalValue(true, Some(testAmount)))))))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ClaimRefundsController.show.url)
      }
    }

    "redirect to the returns frequency page when the registration reason is TOGC" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))
        .registrationApi.replaceSection[VatApplication](VatApplication(northernIrelandProtocol = Some(NIPTurnover(goodsToEU = None, goodsFromEU = Some(ConditionalValue(true, Some(testAmount)))))))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ClaimRefundsController.show.url)
      }
    }

    "redirect to the Claim VAT Refund page in all other cases" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(northernIrelandProtocol = Some(testNIPCompliance)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[VatApplication](VatApplication(northernIrelandProtocol = Some(NIPTurnover(goodsToEU = None, goodsFromEU = Some(ConditionalValue(true, Some(testAmount)))))))
        .s4lContainer[VatApplication].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ClaimRefundsController.show.url)
      }
    }

  }
}