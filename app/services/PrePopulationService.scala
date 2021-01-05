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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import connectors.logResponse
import javax.inject.{Inject, Singleton}
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration}
import models._
import uk.gov.hmrc.http.HeaderCarrier
import utils.SystemDate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrePopulationService @Inject()(val save4later: S4LService,
                                     val businessContactService: BusinessContactService,
                                     val vatRegService: VatRegistrationService
                                    )(implicit ec: ExecutionContext){

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val seqAllowedCountries = Seq("United Kingdom", "UK", "GB") map normalised

  def getCTActiveDate(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[LocalDate]] =
    for {
      ctReg <- getCompanyRegistrationDetails
      optDate = ctReg.flatMap(_.accountingDetails).flatMap(_.activeDate)
    } yield optDate.map(dateString => LocalDate.parse(dateString, formatter))

  private def normalised(str: String) = str.toLowerCase.replace(" ", "")

  private[services] def getCompanyRegistrationDetails(implicit hc: HeaderCarrier,
                                                      profile: CurrentProfile): Future[Option[CorporationTaxRegistration]] = {

    vatRegService.getThreshold(profile.registrationId) map { threshold =>
      if (!threshold.mandatoryRegistration) {
        Some(CorporationTaxRegistration(Some(AccountingDetails("", Some(SystemDate.getSystemDate.toLocalDate.plusDays(7) format formatter)))))
      } else {
        None
      }
    } recover {
      case e => throw logResponse(e, "getCompanyRegistrationDetails")
    }
  }

  private[services] def filterAddressListByCountry(seqAddress: Seq[Address]): Seq[Address] = seqAddress.filter(addr =>
    addr.country.fold(true)(
      country => {
        val containsCode = country.code.fold(false)(code => seqAllowedCountries contains normalised(code))
        val containsName = country.name.fold(false)(name => seqAllowedCountries contains normalised(name))

        containsCode || containsName
      }
    )
  )
}
