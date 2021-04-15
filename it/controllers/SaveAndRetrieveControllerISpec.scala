
package controllers

import common.enums.VatRegStatus
import controllers.registration.applicant.routes
import itutil.ControllerISpec
import models.api.VatScheme
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

class SaveAndRetrieveControllerISpec extends ControllerISpec {

  val s4lApiUrl = s"/save4later/vat-registration-frontend/${currentProfile.registrationId}/data/partialVatScheme"
  val url = s"/schemes/save-for-later"

  val fullVatSchemeJson = Json.toJson(fullVatScheme)

  val s4lKey = "partialVatScheme"
  val s4lCacheMap = Map(s4lKey -> Json.toJson(fullVatScheme)(VatScheme.s4lFormat))

  "GET /schemes/save-for-later" when {
    "the VAT scheme is retrieved successfully from the back end" when {
      "the VAT scheme is stored successfully in Save 4 Later" must {
        "return SEE_OTHER" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(fullVatScheme)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubPut(s4lApiUrl, CREATED, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationProgressSavedController.show().url)
        }
      }

      "the VAT scheme is empty" must {
        "return SEE_OTHER" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(VatScheme(currentProfile.registrationId, status = VatRegStatus.draft))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubPut(s4lApiUrl, CREATED, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationProgressSavedController.show().url)
        }
      }

      "the VAT scheme is cannot be stored in Save 4 Later" must {
        "return INTERNAL_SERVER_ERROR" in new Setup {
          given
            .user.isAuthorised
            .vatScheme.contains(fullVatScheme)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          stubPut(s4lApiUrl, INTERNAL_SERVER_ERROR, Json.stringify(Json.toJson(CacheMap(currentProfile.registrationId, s4lCacheMap))))

          val res = await(buildClient(url).get)

          res.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

}
