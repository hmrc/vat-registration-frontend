/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import connectors.{BusinessRegistrationConnector, logResponse}
import features.businessContact.BusinessContactService
import features.businessContact.models.BusinessContact
import features.officer.models.view.LodgingOfficer
import models._
import models.api._
import models.external.{AccountingDetails, CorporationTaxRegistration, Officer}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.SystemDate

import scala.concurrent.Future

class PrePopulationService @Inject()(val businessRegistrationConnector: BusinessRegistrationConnector,
                                     val incorpInfoService: IncorporationInformationService,
                                     val save4later: S4LService,
                                     val businessContactService: BusinessContactService,
                                     val vatRegService: RegistrationService) extends PrePopService

trait PrePopService {

  val businessRegistrationConnector: BusinessRegistrationConnector
  val incorpInfoService: IncorporationInformationService
  val vatRegService: RegistrationService
  val save4later: S4LService
  val businessContactService: BusinessContactService

  private val formatter             = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val seqAllowedCountries   = Seq("United Kingdom","UK").map(a => a.toLowerCase.replace(" ", ""))

  def getCTActiveDate(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[LocalDate]] =
    for {
      ctReg       <- getCompanyRegistrationDetails
      optDate     = ctReg.flatMap(_.accountingDetails).flatMap(_.activeDate)
    } yield optDate.map(dateString => LocalDate.parse(dateString, formatter))

  def getPpobAddressList(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]] = {
    for {
      roAddress       <- incorpInfoService.getRegisteredOfficeAddress
      ppobAddress     <- businessContactService.getBusinessContact map(_.ppobAddress)
      businessContact <- save4later.fetchAndGet[BusinessContact]
      s4lAddress      =  businessContact.flatMap(_.ppobAddress)
    } yield filterAddressListByCountry(List(roAddress, ppobAddress, s4lAddress).flatten.distinct)
  }

  private[services] def getCompanyRegistrationDetails(implicit hc: HeaderCarrier,
                                            profile: CurrentProfile,
                                            rds: HttpReads[CorporationTaxRegistration]): Future[Option[CorporationTaxRegistration]] = {

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

  private[services] def filterAddressListByCountry(seqAddress:Seq[ScrsAddress]): Seq[ScrsAddress] = seqAddress.filter(addr =>
    addr.country.fold(true)(
      count => seqAllowedCountries.contains(count.toLowerCase.replace(" ","")
      )
    )
  )

  def getOfficerAddressList(officer: LodgingOfficer)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]] = {
    incorpInfoService.getRegisteredOfficeAddress map {
      address => Seq(address, officer.homeAddress.fold(Option.empty[ScrsAddress])(_.address)).flatten.distinct
    }
  }

  def getOfficerList(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]] = {
    incorpInfoService.getOfficerList map (_.distinct)
  }

  def getTradingName(regId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    businessRegistrationConnector.retrieveTradingName(regId)

  def saveTradingName(regId: String, tradingName: String)(implicit hc: HeaderCarrier): Future[String] =
    businessRegistrationConnector.upsertTradingName(regId, tradingName)
}
