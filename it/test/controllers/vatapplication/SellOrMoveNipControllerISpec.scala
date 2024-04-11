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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import models.{ConditionalValue, NIPTurnover}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._


class SellOrMoveNipControllerISpec extends ControllerISpec {
  val testAmount: BigDecimal = 123456
  lazy val url = controllers.vatapplication.routes.SellOrMoveNipController.show.url
  val testNIPCompliance: NIPTurnover = NIPTurnover(Some(ConditionalValue(true, Some(testAmount))), None)

  "Show sell or move (NIP) page" should {
    "return OK with pre-pop when is no value for 'goodsToEU' in the backend" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/sell-or-move-nip").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "Return OK with pre-pop when there is a value for 'goodsToEU' in the backend" in {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(northernIrelandProtocol = Some(NIPTurnover(Some(ConditionalValue(true, Some(testAmount))))))))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }

    "Submit send goods to EU" should {
      "return SEE_OTHER for receive goods" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyVatSchemeNetp)
          .registrationApi.replaceSection[VatApplication](
            VatApplication(northernIrelandProtocol = Some(NIPTurnover(Some(ConditionalValue(true, Some(testAmount))))))
          )

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response = buildClient("/sell-or-move-nip").post(Map("value" -> Seq("true"), "sellOrMoveNip" -> Seq("123456")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ReceiveGoodsNipController.show.url)
        }
      }

      "return BAD_REQUEST when submitted with missing data" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyVatSchemeNetp)
          .registrationApi.replaceSection[VatApplication](
            VatApplication(northernIrelandProtocol = Some(NIPTurnover(Some(ConditionalValue(true, Some(testAmount))))))
          )

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response = buildClient("/sell-or-move-nip").post(Map.empty[String, String])
        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
