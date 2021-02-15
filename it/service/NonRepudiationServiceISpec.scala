
package service

import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, patchRequestedFor, urlEqualTo, verify}
import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.Html
import services.NonRepudiationService
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class NonRepudiationServiceISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val service = app.injector.instanceOf[NonRepudiationService]
  val storeNrsApiUrl = s"/vatreg/$testRegId/nrs-payload"
  val testHtml  = Html("<html></html>")
  val testBase64 = "PGh0bWw+PC9odG1sPg=="
  val requestBody = Json.obj(
    "payload" -> testBase64
  )

  "storeEncodedUserAnswers" must {
    "store base 64 encoded user answers and return StoreNrsPayloadSuccess" in {
      stubPatch(storeNrsApiUrl, OK, "")

      val res = await(service.storeEncodedUserAnswers(testRegId, testHtml))

      verify(1, patchRequestedFor(urlEqualTo(storeNrsApiUrl)).withRequestBody(equalToJson(requestBody.toString)))
      res mustBe StoreNrsPayloadSuccess
    }
    "Throw an internal server exception if the store fails" in {
      stubPatch(storeNrsApiUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException] {
        await(service.storeEncodedUserAnswers(testRegId, testHtml))
      }

      verify(1, patchRequestedFor(urlEqualTo(storeNrsApiUrl)).withRequestBody(equalToJson(requestBody.toString)))
    }
  }

}
