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
import models.external.{AccountingDetails, CorporationTaxRegistration}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global


@ImplementedBy(classOf[CompanyRegistrationConnector])
trait PPConnector {

  val companyRegUrl: String
  val http: WSHttp

  def getCompanyRegistrationDetails
  (regId: String)
  (implicit hc: HeaderCarrier, rds: HttpReads[CorporationTaxRegistration]): OptionalResponse[CorporationTaxRegistration]

}


@Singleton
class CompanyRegistrationConnector extends PPConnector with ServicesConfig {

  //$COVERAGE-OFF$
  val className = this.getClass.getSimpleName
  val companyRegUrl = baseUrl("company-registration")
  val http: WSHttp = WSHttp

  import cats.instances.future._

  override def getCompanyRegistrationDetails(regId: String)
                                            (implicit hc: HeaderCarrier, rds: HttpReads[CorporationTaxRegistration])
  : OptionalResponse[CorporationTaxRegistration] =
    OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some("2017-05-20")))))

  //    OptionT(
  //      http.GET[CorporationTaxRegistration](s"$companyRegUrl/company-registration/corporation-tax-registration/$regId/corporation-tax-registration")
  //        .map(Option.apply)
  //        .recover {
  //          case ex =>
  //            logResponse(ex, className, "getCompanyRegistrationDetails")
  //            Option.empty[CorporationTaxRegistration]
  //        }
  //    )

  //$COVERAGE-ON$

}