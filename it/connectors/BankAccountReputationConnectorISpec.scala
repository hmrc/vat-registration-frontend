
package connectors

import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException
import play.api.mvc.Request

class BankAccountReputationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector = app.injector.instanceOf[BankAccountReputationConnector]

  val stubbedBarsSuccessResponse = Json.obj(
   "accountNumberIsWellFormatted" -> "yes",
   "nonStandardAccountDetailsRequiredForBacs" -> "no"
  )

  "validateBankDetails" when {
    "BARS returns OK" must {
      "return the JSON response" in new Setup {
        given()
          .user.isAuthorised()
          .bankAccountReputation.passes

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(connector.validateBankDetails(testUkBankDetails))

        res mustBe stubbedBarsSuccessResponse
      }
    }
    "BARS returns another status" must {
      "throw an internal server exception" in new Setup {
        given()
          .user.isAuthorised()
          .bankAccountReputation.isDown

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        intercept[InternalServerException] {
          await(connector.validateBankDetails(testUkBankDetails))
        }
      }
    }
  }

}
