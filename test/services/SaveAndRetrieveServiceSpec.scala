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

package services

import common.enums.VatRegStatus
import connectors.mocks.MockS4lConnector
import play.api.libs.json.{JsValue, Json}
import services.mocks.MockVatRegistrationService
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{BadRequestException, InternalServerException}

import scala.concurrent.Future

class SaveAndRetrieveServiceSpec extends VatRegSpec with MockS4lConnector with MockVatRegistrationService {

  val s4lKey = "partialVatScheme"
  val testInternalId = "testInternalId"
  val emptyVatSchemeJson: JsValue = Json.obj(
    "registrationId" -> Json.toJson[String](testRegId),
    "status" -> Json.toJson(VatRegStatus.draft),
    "internalId" -> Json.toJson[String](testInternalId)
  )
  val validVatSchemeJson: JsValue = Json.toJson(validVatScheme)
  val testCacheMap: CacheMap = CacheMap(s4lKey, data = Map())

  object Service extends SaveAndRetrieveService(vatRegistrationServiceMock, mockS4LConnector, mockAuthClientConnector)

  "savePartialVatScheme" must {
    "store an empty VatScheme from VAT reg in S4L" in {
      mockGetVatSchemeJson(testRegId)(Future.successful(emptyVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, emptyVatSchemeJson)(Future.successful(testCacheMap))

      val res = await(Service.savePartialVatScheme(testRegId))

      res mustBe testCacheMap
    }
    "store a full VatScheme from VAT reg in S4L" in {
      mockGetVatSchemeJson(testRegId)(Future.successful(validVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, validVatSchemeJson)(Future.successful(testCacheMap))

      val res = await(Service.savePartialVatScheme(testRegId))

      res mustBe testCacheMap
    }
    "throw an internal server exception if we fail to get the Vat Scheme from the backend" in {
      mockGetVatSchemeJson(testRegId)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.savePartialVatScheme(testRegId))
      }
    }
    "throw an internal server exception if we fail to save the Vat Scheme in S4L" in {
      mockGetVatSchemeJson(testRegId)(Future.successful(validVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, validVatSchemeJson)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.savePartialVatScheme(testRegId))
      }
    }
  }

  "retrievePartialVatScheme" must {
    "retrieve no VatScheme from S4L" in {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, None)
      mockAuthenticatedInternalId(Some(testInternalId))

      mockSaveVatScheme(testRegId, emptyVatSchemeJson)(Future.successful(emptyVatSchemeJson))

      val res = await(Service.retrievePartialVatScheme(testRegId))

      res mustBe emptyVatSchemeJson
    }

    "retrieve a full VatScheme from S4L" in {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(validVatSchemeJson))

      mockSaveVatScheme(testRegId, validVatSchemeJson)(Future.successful(validVatSchemeJson))

      val res = await(Service.retrievePartialVatScheme(testRegId))

      res mustBe validVatSchemeJson
    }

    "store an empty vat scheme if the backend returns a bad request for the full vat scheme store" in {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(validVatSchemeJson))
      mockAuthenticatedInternalId(Some(testInternalId))

      mockSaveVatScheme(testRegId, validVatSchemeJson)(Future.failed(new BadRequestException("")))
      mockSaveVatScheme(testRegId, emptyVatSchemeJson)(Future.successful(emptyVatSchemeJson))

      val res = await(Service.retrievePartialVatScheme(testRegId))

      res mustBe emptyVatSchemeJson
    }

    "throw an internal server exception if we fail to retrieve the Vat Scheme from s4l" in {
      mockSessionFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(emptyVatSchemeJson))

      mockSaveVatScheme(testRegId, emptyVatSchemeJson)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.retrievePartialVatScheme(testRegId))
      }
    }
  }

}
