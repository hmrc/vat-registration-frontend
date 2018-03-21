/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import config.WSHttp
import models.external.CompanyRegistrationProfile
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.http.Status._
import utils.{RegistrationWhitelist, VATRegFeatureSwitches}

import scala.concurrent.Future

class CompanyRegistrationConnectorImpl @Inject()(val http: WSHttp,
                                                 config: ServicesConfig,
                                                 val vatFeatureSwitches: VATRegFeatureSwitches) extends CompanyRegistrationConnector {
  val companyRegistrationUrl: String = config.baseUrl("company-registration")
  val companyRegistrationUri: String = config.getConfString("company-registration.uri",
    throw new RuntimeException("[CompanyRegistrationConnector] Could not retrieve config for 'company-registration.uri'"))
  val stubUrl: String = config.baseUrl("incorporation-frontend-stub")
  val stubUri: String = config.getConfString("incorporation-frontend-stub.uri",
    throw new RuntimeException("[CompanyRegistrationConnector] Could not retrieve config for 'incorporation-frontend-stub.uri'"))
}

trait CompanyRegistrationConnector extends RegistrationWhitelist {

  val companyRegistrationUrl: String
  val companyRegistrationUri: String
  val stubUrl: String
  val stubUri: String
  val http: WSHttp
  val vatFeatureSwitches: VATRegFeatureSwitches

  def getTransactionId(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    ifRegIdNotWhitelisted[String](regId) {
      http.GET[JsValue](s"$stubUrl$stubUri/$regId/corporation-tax-registration") map {
        _.\("confirmationReferences").\("transaction-id").as[String]
      } recover {
        case e => throw logResponse(e, "getTransactionID")
      }
    }(returnDefaultTransId)
  }

  def getCompanyProfile(regId: String)(implicit hc: HeaderCarrier): Future[Option[CompanyRegistrationProfile]] = {
    ifRegIdNotWhitelisted(regId) {
      val url = if (useCrStub) stubUrl else companyRegistrationUrl
      val uri = if (useCrStub) stubUri else companyRegistrationUri
      val prefix = if (useCrStub) "" else "/corporation-tax-registration"

      http.GET[HttpResponse](s"$url$uri$prefix/$regId/corporation-tax-registration") map { response =>
        if (response.status == NOT_FOUND) None else {
          val incorpStatus = (response.json \ "status").as[String]
          val ctStatus = (response.json \ "acknowledgementReferences" \ "status").asOpt[String]
          Some(CompanyRegistrationProfile(incorpStatus, ctStatus))
        }
      } recover {
        case e => Logger.warn(s"[CompanyRegistrationConnector][getCompanyProfile] had an exception ${e.getMessage} for regId: $regId")
          None
      }
    }(returnDefaultCompRegProfile)
  }

  private[connectors] def useCrStub = vatFeatureSwitches.useCrStubbed.enabled
}
