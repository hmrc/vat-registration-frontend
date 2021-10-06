
package controllers.registration.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import models.{ConditionalValue, NIPCompliance}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._


class SellOrMoveNipControllerISpec extends ControllerISpec {
  val testAmount: BigDecimal = 1234.123
  lazy val url = controllers.registration.returns.routes.SellOrMoveNipController.show.url
  val testNIPCompliance: NIPCompliance = NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), None)

  "Show sell or move (NIP) page" should {
    "return OK with pre-pop when is no value for 'goodsToEU' in the backend" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAuditMerged()
        .audit.writesAudit()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/sell-or-move-nip").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "Return OK with pre-pop when there is a value for 'goodsToEU' in the backend" in {
      given()
        .user.isAuthorised
        .audit.writesAuditMerged()
        .audit.writesAudit()
        .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }

    "Submit send goods to EU" should {
      "return SEE_OTHER for receive goods" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAuditMerged()
          .audit.writesAudit()
          .s4lContainer[Returns].contains(Returns(northernIrelandProtocol = Some(testNIPCompliance)))
          .s4lContainer[Returns].isUpdatedWith(Returns(northernIrelandProtocol = Some(NIPCompliance(Some(ConditionalValue(true, Some(testAmount))), Some(ConditionalValue(true, Some(testAmount)))))))
          .vatScheme.contains(emptyVatSchemeNetp)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/sell-or-move-nip").post(Map("value" -> Seq("true"), "sellOrMoveNip" -> Seq("123456")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReceiveGoodsNipController.show.url)
        }
      }
    }
  }
}
