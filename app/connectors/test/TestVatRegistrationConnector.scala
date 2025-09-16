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

package connectors.test

import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestVatRegistrationConnector @Inject()(val http: HttpClient, config: ServicesConfig)
                                            (implicit ec: ExecutionContext) {

  val vatRegUrl: String = config.baseUrl("vat-registration")
  private val vatStubUrl: String = config.baseUrl("vat-registration-stub")

  def retrieveVatSubmission(regId: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET(s"$vatRegUrl/vatreg/test-only/submissions/$regId/submission-payload") map (_.json)
  }

  def hitVatStub(userId: String, regId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET(s"$vatStubUrl/vat-registration-stub/setup-data/$userId/$regId")
  }

  def deleteAllRegistrations(implicit hc: HeaderCarrier): Future[Boolean] =
    http.DELETE(s"$vatRegUrl/vatreg/test-only/registrations").map { response =>
      response.status match {
        case 204 => true
        case _ => false
      }
    }.recoverWith {
      case _: Exception =>
        Future.successful(false)
    }

}
