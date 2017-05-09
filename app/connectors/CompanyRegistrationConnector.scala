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

import cats.data.OptionT
import com.google.inject.ImplementedBy
import config.WSHttp
import models.external.CoHoCompanyProfile
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CompanyRegistrationConnector extends CompanyRegistrationConnect with ServicesConfig {
  //$COVERAGE-OFF$
  val companyRegistrationUrl: String = baseUrl("company-registration")
  val companyRegistrationUri: String = getConfString("company-registration.uri","")
  val http:WSHttp = WSHttp
  //$COVERAGE-ON$
}

@ImplementedBy(classOf[CompanyRegistrationConnector])
trait CompanyRegistrationConnect {
  self =>

  val companyRegistrationUrl : String
  val companyRegistrationUri : String
  val http : WSHttp

  val className = self.getClass.getSimpleName

  def getCompanyRegistrationDetails(regId: String)(implicit hc : HeaderCarrier) : OptionalResponse[CoHoCompanyProfile] = {
    OptionT {
      http.GET[CoHoCompanyProfile](s"$companyRegistrationUrl$companyRegistrationUri/$regId").map(Some(_)) recover {
        case e: Exception => logResponse(e, className, "getCompanyRegistrationDetails")
          Option.empty[CoHoCompanyProfile]
      }
    }
  }
}

