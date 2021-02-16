
package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class NonRepudiationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector = app.injector.instanceOf[NonRepudiationConnector]
  val storeNrsApiUrl = s"/vatreg/$testRegId/nrs-payload"
  val testBase64 = "SFRNTCBUTyBFTkNPREUi"
  val requestBody = Json.obj(
    "payload" -> testBase64
  )

  "storeEncodedUserAnswers" must {
    "return StoreNrsPayloadSuccess if the backend API returns OK" in {
      stubPatch(storeNrsApiUrl, OK, "")

      val res = await(connector.storeEncodedUserAnswers(testRegId, testBase64))

      verify(patchRequestedFor(urlEqualTo(storeNrsApiUrl)).withRequestBody(equalToJson(requestBody.toString)))
      res mustBe StoreNrsPayloadSuccess
    }
    "throw an exception for any other status" in {
      stubPatch(storeNrsApiUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(connector.storeEncodedUserAnswers(testRegId, testBase64))
      }

      verify(1, patchRequestedFor(urlEqualTo(storeNrsApiUrl)).withRequestBody(equalToJson(requestBody.toString)))
    }
  }

}