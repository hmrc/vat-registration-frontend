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

import connectors.RegistrationApiConnector
import connectors.RegistrationApiConnector.nrsSubmissionPayloadKey
import models.ApiKey
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.Base64Util

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class NonRepudiationService @Inject()(base64Util: Base64Util,
                                      registrationApiConnector: RegistrationApiConnector) {

  def storeEncodedUserAnswers(regId: String, html: Html)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    implicit val key: ApiKey[String] = nrsSubmissionPayloadKey
    val encodedHtml = base64Util.encodeString(html.toString)
    registrationApiConnector.replaceSection(regId, encodedHtml)
  }

}