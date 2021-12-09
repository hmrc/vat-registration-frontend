/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import models.api.Partnership
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class PartnershipIDConnectorSpec extends VatRegSpec {

  class Setup {
    implicit val ex = scala.concurrent.ExecutionContext.Implicits.global
    val config = new FrontendAppConfig(mockServicesConfig, runModeConfiguration = Configuration())
    val connector = new PartnershipIdConnector(mockHttpClient, config)(ex)
    val testJourneyId = "1"
    val testJourneyUrl = "/test-journey-url"
    val createJourneyUrl = "/partnership-identification/api/general-partnership/journey"
    val retrieveDetailsUrl = s"/partnership-identification/api/journey/$testJourneyId"

    val testJourneyConfig = PartnershipIdJourneyConfig(
      continueUrl = "/test-url",
      optServiceName = Some("MTD"),
      deskProServiceId = "MTDSUR",
      signOutUrl = "/test-sign-out",
      accessibilityUrl = "/test-accessiblity-url",
      regime = "VATC",
      businessVerificationCheck = true
    )
  }

  "createJourney" when {
    "the API returns CREATED" must {
      "return the journey ID when the response JSON includes the journeyId" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(CREATED, Json.obj("journeyStartUrl" -> testJourneyUrl).toString))

        val res = await(connector.createJourney(testJourneyConfig, Partnership))

        res mustBe testJourneyUrl
      }
    }
    "the API returns Errors" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(INTERNAL_SERVER_ERROR, ""))

        intercept[InternalServerException] {
          await(connector.createJourney(testJourneyConfig, Partnership))
        }
      }
    }
    "the API returns an unexpected status" must {
      "throw an InternalServerException" in new Setup {
        mockHttpPOST(createJourneyUrl, HttpResponse(IM_A_TEAPOT, ""))

        intercept[InternalServerException] {
          await(connector.createJourney(testJourneyConfig, Partnership))
        }
      }
    }
  }

  "getDetails" must {
    "return GeneralPartnership when returns OK" in new Setup {
      val testPIResponse: JsObject = Json.obj(
        "sautr" -> testSautr,
        "postcode" -> testPostcode,
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        ),
        "registration" -> Json.obj(
          "registrationStatus" -> testRegistration,
          "registeredBusinessPartnerId" -> testSafeId
        ),
        "identifiersMatch" -> true
      )

      mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, testPIResponse.toString()))
      val res = await(connector.getDetails(testJourneyId))
      res mustBe testGeneralPartnership
    }
    "throw an InternalServerException when relevant fields are missing OK" in new Setup {
      val invalidGeneralPartnershipJson = {
        Json.toJson(testGeneralPartnership).as[JsObject] - "sautr"
      }
      mockHttpGET(retrieveDetailsUrl, HttpResponse(OK, Some(Json.obj("sautr" -> invalidGeneralPartnershipJson))))

      intercept[InternalServerException] {
        await(connector.getDetails(testJourneyId))
      }
    }
    "throw an InternalServerException for any other status" in new Setup {
      mockHttpGET(retrieveDetailsUrl, HttpResponse(IM_A_TEAPOT, ""))

      intercept[InternalServerException] {
        await(connector.getDetails(testJourneyId))
      }
    }
  }
}