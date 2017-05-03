/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import config.WSHttp
import models.external.CoHoCompanyProfile
import play.api.Logger
import play.api.libs.json._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{BadRequestException, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CompanyRegistrationConnector extends CompanyRegistrationConnect with ServicesConfig {
  lazy val companyRegistrationUrl: String = baseUrl("company-registration")
  lazy val companyRegistrationUri: String = getConfString("company-registration.uri","")
  val http = WSHttp
}

@ImplementedBy(classOf[CompanyRegistrationConnector])
trait CompanyRegistrationConnect {

  val companyRegistrationUrl : String
  val companyRegistrationUri : String
  val http : WSHttp

  def getCompanyRegistrationDetails(regId: String)(implicit hc : HeaderCarrier) : Future[CoHoCompanyProfile] = {
    http.GET[JsObject](s"$companyRegistrationUrl$companyRegistrationUri/$regId") map {
      response =>
        val status = (response \ "status").as[String]
        val txId = (response \ "confirmationReferences" \ "transaction-id").as[String]
        CoHoCompanyProfile(status, txId)
    } recover {
      case badRequestErr: BadRequestException =>
        Logger.error("[CompanyRegistrationConnect] [getCompanyRegistrationDetails] - Received a BadRequest status code when expecting a Company Registration document")
        throw badRequestErr
      case ex: Exception =>
        Logger.error(s"[CompanyRegistrationConnect] [getCompanyRegistrationDetails] - Received an error response when expecting a Company Registration document - error: ${ex.getMessage}")
        throw ex
    }
  }
}
