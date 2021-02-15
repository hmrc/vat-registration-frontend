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

import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import mocks.MockNonRepuidiationConnector
import play.twirl.api.Html
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException
import utils.MockBase64Util

import scala.concurrent.Future

class NonRepudiationServiceSpec extends VatRegSpec
  with MockBase64Util
  with MockNonRepuidiationConnector {

  val testHtml  = Html("<html></html>")
  val testBase64 = "PGh0bWw+PC9odG1sPg=="

  object Service extends NonRepudiationService(mockBase64Util, mockNonRepuidiationConnector)

  "storeEncodedUserAnswers" must {
    "Return StoreNrsPayloadSuccess" in {
      mockEncodeBase64(testHtml.toString())(testBase64)
      mockStoreEncodedUserAnswers(testRegId, testBase64)(Future.successful(StoreNrsPayloadSuccess))

      val res = await(Service.storeEncodedUserAnswers(testRegId, testHtml))

      res mustBe StoreNrsPayloadSuccess
    }
    "throw an exception if the store fails" in {
      mockEncodeBase64(testHtml.toString())(testBase64)
      mockStoreEncodedUserAnswers(testRegId, testBase64)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.storeEncodedUserAnswers(testRegId, testHtml))
      }
    }
  }

}
