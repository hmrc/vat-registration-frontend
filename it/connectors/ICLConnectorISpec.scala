
package connectors

import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.SicCode
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.UpstreamErrorResponse

class ICLConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector = app.injector.instanceOf[ICLConnector]
  val iclJourneySetupUrl = "/internal/initialise-journey"
  val iclGetResultUrl = "/test-url"

  val testJourneySetupJson = Json.obj(
    "redirectUrl" -> "/test",
    "journeySetupDetails" -> Json.obj(
      "customMessages" -> Json.obj(
        "summary" -> Json.obj(),
        "summaryCy" -> Json.obj()
      ),
      "sicCodes" -> Json.arr("1234" -> "desc")
    )
  )

  val testSicCode = SicCode(sicCodeId, sicCodeDesc, sicCodeDescCy)

  val testGetResultJson = Json.obj(
    "sicCodes" -> Json.arr(
      Json.toJson(testSicCode)
    )
  )

  val testIclResponseJson = Json.obj("link" -> "/test")

  "iclSetup" must {
    "return json containing a redirect link" in new Setup {
      stubPost(iclJourneySetupUrl, OK, testIclResponseJson.toString())

      val res = await(connector.iclSetup(testJourneySetupJson))

      res mustBe testIclResponseJson
    }
    "throw an exception if ICL returns an unexpected status" in new Setup {
      stubPost(iclJourneySetupUrl, INTERNAL_SERVER_ERROR, "")

      intercept[UpstreamErrorResponse] {
        await(connector.iclSetup(testJourneySetupJson))
      }
    }
  }

  "iclGetResult" must {
    "return the selected SIC codes" in new Setup {
      stubGet(iclGetResultUrl, OK, testGetResultJson.toString())

      val res = await(connector.iclGetResult(iclGetResultUrl))

      res mustBe testGetResultJson
    }
    "throw an exception if ICL returns an unexpected status" in new Setup {
      stubGet(iclGetResultUrl, INTERNAL_SERVER_ERROR, testGetResultJson.toString())

      intercept[UpstreamErrorResponse] {
        await(connector.iclGetResult(iclGetResultUrl))
      }
    }
  }

}
