
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

      insertCurrentProfileIntoDb(currentProfile, sessionId)

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
          .registrationApi.replaceSection[VatApplication](VatApplication(northernIrelandProtocol = Some(NIPTurnover(Some(ConditionalValue(true, Some(testAmount)))))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/sell-or-move-nip").post(Map("value" -> Seq("true"), "sellOrMoveNip" -> Seq("123456")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ReceiveGoodsNipController.show.url)
        }
      }
    }
  }
}
