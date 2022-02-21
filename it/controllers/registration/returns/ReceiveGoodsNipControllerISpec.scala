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

package controllers.registration.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import models.api._
import models.{ConditionalValue, NIPCompliance, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class ReceiveGoodsNipControllerISpec extends ControllerISpec {
  val testAmount: BigDecimal = 1234.123
  lazy val url: String = controllers.registration.returns.routes.ReceiveGoodsNipController.show.url
  val testNIPCompliance: NIPCompliance = NIPCompliance(None, Some(ConditionalValue(true, Some(testAmount))))

  "show Northern Ireland Receive page" should {
    "return OK with no prepop when there is no value for 'receiveGoods' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "Return OK with prepop when there is a value for 'receiveGoods' in the backend" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  "submit Receive Goods page" should {
    "redirect to the returns frequency page when NETP" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), Some(ConditionalValue(true, Some(testAmount)))))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the returns frequency page when NonUkNonEstablished" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), Some(ConditionalValue(true, Some(testAmount)))))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the returns frequency page when the registration reason is TOGC" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), Some(ConditionalValue(true, Some(testAmount)))))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the start date resolver in all other cases" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), Some(ConditionalValue(true, Some(testAmount)))))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/receive-goods-nip").post(Map("value" -> Seq("true"), "northernIrelandReceiveGoods" -> Seq("123456")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.VatRegStartDateResolverController.resolve.url)
      }
    }

  }
}