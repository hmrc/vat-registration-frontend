
package controllers.vatapplication

import featureswitch.core.config.TaskList
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, StoringWithinUk, VatApplication}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class StoringGoodsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/storing-goods-for-dispatch"

  val testOverseasCompliance = OverseasCompliance(
    goodsToOverseas = Some(true),
    goodsToEu = Some(true)
  )

  val testVatApplication: VatApplication = fullVatApplication.copy(overseasCompliance = Some(testOverseasCompliance))

  "GET /storing-goods-for-dispatch" when {
    "the user is unauthorised" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        given()
          .user.isNotAuthorised
          .s4lContainer[VatApplication].contains(testVatApplication)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "the user is authorised" must {
      "S4L doesn't contain an answer" must {
        "return OK with the correct page" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(testVatApplication)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get())

          res.status mustBe OK
        }
      }
      "S4L contains an answer" must {
        "return OK and pre-populate the form if the answer is UK" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(
            testVatApplication.copy(
              overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk)))
            )
          )

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get())

          res.status mustBe OK
          Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked")
        }
      }
      "return OK and pre-populate the form if the answer is OVERSEAS" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(
          testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          )
        )

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[id=value-2]").hasAttr("checked")
      }
    }

    "POST /storing-goods-for-dispatch" when {
      "the user submits Storing Within the UK" must {
        "redirect to the dispatch from warehouse controller" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(testVatApplication)
            .s4lContainer[VatApplication].clearedByKey
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj("value" -> "UK")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.DispatchFromWarehouseController.show.url)
        }
      }
      "the user submits Storing Overseas" must {
        "redirect to the returns frequency page" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(testVatApplication)
            .s4lContainer[VatApplication].clearedByKey
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj("value" -> "OVERSEAS")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
        }

        "redirect to the application-progress page if TaskList FS enabled" in new Setup {
          enable(TaskList)
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(testVatApplication)
            .s4lContainer[VatApplication].clearedByKey
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj("value" -> "OVERSEAS")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
          disable(TaskList)
        }
      }
      "the user submits an invalid answer" must {
        "return BAD_REQUEST" in new Setup {
          given()
            .user.isAuthorised()
            .s4lContainer[VatApplication].contains(testVatApplication)
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Json.obj()))

          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
