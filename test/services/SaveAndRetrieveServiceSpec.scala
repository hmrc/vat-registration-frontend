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

import connectors.mocks.MockS4lConnector
import play.api.libs.json.Json
import services.mocks.MockVatRegistrationService
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SaveAndRetrieveServiceSpec extends VatRegSpec with MockS4lConnector with MockVatRegistrationService {

  val s4lKey = "partialVatScheme"
  val emptyVatSchemeJson = Json.toJson(emptyVatScheme)
  val validVatSchemeJson = Json.toJson(validVatScheme)
  val testCacheMap = CacheMap(s4lKey, data = Map())

  object Service extends SaveAndRetrieveService(vatRegistrationServiceMock, mockS4LConnector)

  "savePartialVatScheme" must {
    "store an empty VatScheme from VAT reg in S4L" in {
      mockGetVatScheme(testRegId)(Future.successful(emptyVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, emptyVatSchemeJson)(Future.successful(testCacheMap))

      val res = await(Service.savePartialVatScheme(testRegId))

      res mustBe testCacheMap
    }
    "store a full VatScheme from VAT reg in S4L" in {
      mockGetVatScheme(testRegId)(Future.successful(validVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, validVatSchemeJson)(Future.successful(testCacheMap))

      val res = await(Service.savePartialVatScheme(testRegId))

      res mustBe testCacheMap
    }
    "throw an internal server exception if we fail to get the Vat Scheme from the backend" in {
      mockGetVatScheme(testRegId)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.savePartialVatScheme(testRegId))
      }
    }
    "throw an internal server exception if we fail to save the Vat Scheme in S4L" in {
      mockGetVatScheme(testRegId)(Future.successful(validVatSchemeJson))
      mockS4LSave(testRegId, s4lKey, validVatSchemeJson)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.savePartialVatScheme(testRegId))
      }
    }
  }

  "retrievePartialVatScheme" must {
    "retrieve an empty VatScheme from S4L" in {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(emptyVatSchemeJson))

      mockSaveVatScheme(testRegId, emptyVatSchemeJson)(Future.successful(emptyVatSchemeJson))

      val res = await(Service.retrievePartialVatScheme(testRegId))

      res mustBe emptyVatSchemeJson
    }
    "retrieve a full VatScheme from S4L" in {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(validVatSchemeJson))

      mockSaveVatScheme(testRegId, validVatSchemeJson)(Future.successful(validVatSchemeJson))

      val res = await(Service.retrievePartialVatScheme(testRegId))

      res mustBe validVatSchemeJson
    }
    "throw an internal server exception if we fail to retrieve the Vat Scheme from s4l" in {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(s4lKey, Some(emptyVatSchemeJson))

      mockSaveVatScheme(testRegId, emptyVatSchemeJson)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.retrievePartialVatScheme(testRegId))
      }
    }
  }

}
