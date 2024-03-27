/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.mocks.MockRegistrationApiConnector
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import testHelpers.VatRegSpec
import utils.MockBase64Util

class NonRepudiationServiceSpec extends VatRegSpec
  with MockBase64Util
  with MockRegistrationApiConnector {

  val testHtml: Html = Html("<html></html>")
  val testBase64 = "PGh0bWw+PC9odG1sPg=="

  implicit val request: Request[_] = FakeRequest()
  object Service extends NonRepudiationService(mockBase64Util, mockRegistrationApiConnector)

  "storeEncodedUserAnswers" must {
    "Return the stored string" in {
      mockEncodeBase64(testHtml.toString())(testBase64)
      mockReplaceSection(testRegId, testBase64)

      val res = await(Service.storeEncodedUserAnswers(testRegId, testHtml))

      res mustBe testBase64
    }
  }

}