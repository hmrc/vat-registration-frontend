
package controllers

import common.enums.VatRegStatus
import itutil.ControllerISpec
import models.api.VatScheme
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

class SaveAndRetrieveControllerISpec extends ControllerISpec  {

  val vatSchemeApiUrl = s"/vatreg/${currentProfile.registrationId}/get-scheme"
  val s4lApiUrl = s"/save4later/vat-registration-frontend/${currentProfile.registrationId}/data/partialVatScheme"
  val url = "/come-back-later"

  val fullVatSchemeJson = Json.toJson(fullVatScheme)

  val s4lKey = "partialVatScheme"
  val s4lCacheMap = Map(s4lKey -> Json.toJson(fullVatScheme)(VatScheme.s4lFormat))

  "GET /come-back-later" when {
    "the VAT scheme is retrieved successfully from the back end" when {
      "the VAT scheme is stored successfully in Save 4 Later" must {
        "return NOT_IMPLEMENTED" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(fullVatScheme)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubGet(vatSchemeApiUrl, OK, Json.stringify(fullVatSchemeJson))
          stubPut(s4lApiUrl, CREATED, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe NOT_IMPLEMENTED
        }
      }
      "the VAT scheme is empty" must {
        "return NOT_IMPLEMENTED" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(VatScheme(currentProfile.registrationId, status = VatRegStatus.draft))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubGet(vatSchemeApiUrl, OK, Json.stringify(fullVatSchemeJson))
          stubPut(s4lApiUrl, CREATED, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe NOT_IMPLEMENTED
        }
      }
      "the VAT scheme is cannot be stored in Save 4 Later" must {
        "return INTERNAL_SERVER_ERROR" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(fullVatScheme)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubGet(vatSchemeApiUrl, OK, Json.stringify(fullVatSchemeJson))
          stubPut(s4lApiUrl, INTERNAL_SERVER_ERROR, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
    "the VAT scheme cannot be retrieved from the back end" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        given
          .user.isAuthorised
          .vatScheme.contains(fullVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubGet(vatSchemeApiUrl, INTERNAL_SERVER_ERROR, "")

        val res = await(buildClient(url).get)

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
