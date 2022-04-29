/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.mocks.MockUpscanConnector
import models.api.PrimaryIdentityEvidence
import models.external.upscan.{InProgress, UpscanDetails, UpscanResponse}
import play.api.http.Status._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

import scala.concurrent.Future

class UpscanServiceSpec extends VatRegSpec with MockUpscanConnector {

  object TestService extends UpscanService(mockUpscanConnector)

  val testReference = "testReference"
  val testHref = "testHref"
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map())
  val testUpscanDetails: UpscanDetails = UpscanDetails(reference = testReference, fileStatus = InProgress, attachmentType = PrimaryIdentityEvidence)

  "initiateUpscan" must {
    "return an UpscanResponse" in {
      mockUpscanInitiate(Future.successful(testUpscanResponse))
      mockStoreUpscanReference(testRegId, testReference, PrimaryIdentityEvidence)(Future.successful(HttpResponse(OK, "{}")))

      val response = await(TestService.initiateUpscan(testRegId, PrimaryIdentityEvidence))

      response mustBe testUpscanResponse
    }

    "throw an exception if initiate fails" in {
      mockUpscanInitiate(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(TestService.initiateUpscan(testRegId, PrimaryIdentityEvidence)))
    }

    "throw an exception if store fails" in {
      mockUpscanInitiate(Future.successful(testUpscanResponse))
      mockStoreUpscanReference(testRegId, testReference, PrimaryIdentityEvidence)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(TestService.initiateUpscan(testRegId, PrimaryIdentityEvidence)))
    }
  }

  "fetchUpscanFileDetails" must {
    "return an UpscanDetails" in {
      mockFetchUpscanFileDetails(testRegId, testReference)(Future.successful(testUpscanDetails))

      val response = await(TestService.fetchUpscanFileDetails(testRegId, testReference))

      response mustBe testUpscanDetails
    }

    "throw an exception if fetch fails" in {
      mockFetchUpscanFileDetails(testRegId, testReference)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(TestService.fetchUpscanFileDetails(testRegId, testReference)))
    }
  }

  "deleteUpscanDetails" must {
    "delete and return true" in {
      mockDeleteUpscanDetails(testRegId, testReference)(Future.successful(true))

      val response = await(TestService.deleteUpscanDetails(testRegId, testReference))

      response mustBe true
    }

    "throw an exception if delete fails" in {
      mockDeleteUpscanDetails(testRegId, testReference)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(TestService.deleteUpscanDetails(testRegId, testReference)))
    }
  }

  "deleteAllUpscanDetails" must {
    "delete and return true" in {
      mockDeleteAllUpscanDetails(testRegId)(Future.successful(true))

      val response = await(TestService.deleteAllUpscanDetails(testRegId))

      response mustBe true
    }

    "throw an exception if delete fails" in {
      mockDeleteAllUpscanDetails(testRegId)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(TestService.deleteAllUpscanDetails(testRegId)))
    }
  }

  "fetchAllUpscanDetails" must {
    "return list of uploaded files" in {
      mockFetchAllUpscanDetails(testRegId)(Future.successful(List(testUpscanDetails)))

      val response = await(TestService.fetchAllUpscanDetails(testRegId))
      response mustBe List(testUpscanDetails)
    }

    "throw an exception if fetch fails" in {
      mockFetchAllUpscanDetails(testRegId)(Future.failed(new InternalServerException("")))
      intercept[InternalServerException](await(TestService.fetchAllUpscanDetails(testRegId)))
    }
  }

  "getUpscanFileStatus" must {
    "return a file status" in {
      mockFetchUpscanFileDetails(testRegId, testReference)(Future.successful(testUpscanDetails))

      val response = await(TestService.getUpscanFileStatus(testRegId, testReference))

      response mustBe InProgress
    }
  }
}
