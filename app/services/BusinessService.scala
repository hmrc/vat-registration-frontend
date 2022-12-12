/*
 * Copyright 2022 HM Revenue & Customs
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
import models.api.{Address, SicCode}
import models.{Business, ContactPreference, CurrentProfile, LabourCompliance}
import services.BusinessService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessService @Inject()(val registrationApiConnector: RegistrationApiConnector,
                                val s4lService: S4LService)(implicit ec: ExecutionContext) {

  def getBusiness(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Business] = {
    s4lService.fetchAndGet[Business].flatMap {
      case None | Some(Business(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)) =>
        registrationApiConnector.getSection[Business](cp.registrationId).map {
          case Some(business) => business
          case None => Business()
        }
      case Some(business) => Future.successful(business)
    }
  }

  def updateBusiness[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Business] =
    for {
      business <- getBusiness
      updatedBusiness = updateBusinessModel(data, business)
      _ <- registrationApiConnector.replaceSection[Business](cp.registrationId, updatedBusiness)
      _ <- s4lService.clearKey[Business]
    } yield updatedBusiness

  def submitSicCodes(sicCodes: List[SicCode])(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Business] = {
    getBusiness flatMap { sac =>
      val sacWithCodes = sac.copy(businessActivities = Some(sicCodes))

      val mainBusinessActivity = if (sicCodes.size == 1) Some(sicCodes.head) else None
      val labourCompliance = if (!needComplianceQuestions(sicCodes)) None else sacWithCodes.labourCompliance

      updateBusiness(
        sacWithCodes.copy(mainBusinessActivity = mainBusinessActivity, labourCompliance = labourCompliance)
      )
    }
  }

  // scalastyle:off
  private def updateBusinessModel[T](data: T, business: Business): Business = {
    data match {
      case ConfirmTradingName(answer) =>
        if (answer) {
          business.copy(
            hasTradingName = Some(answer),
            tradingName = None
          )
        } else {
          business.copy(hasTradingName = Some(answer))
        }
      case TradingName(answer) => business.copy(tradingName = Some(answer))
      case ShortOrgNameAnswer(answer) => business.copy(shortOrgName = Some(answer))
      case address: Address => business.copy(ppobAddress = Some(address))
      case VatCorrespondenceInWelsh(answer) => business.copy(welshLanguage = Some(answer))
      case preference: ContactPreference => business.copy(contactPreference = Some(preference))
      case Email(answer) => business.copy(email = Some(answer))
      case TelephoneNumber(answer) => business.copy(telephoneNumber = Some(answer))
      case HasWebsiteAnswer(answer) =>
        val contactWithWebsiteCheck = business.copy(hasWebsite = Some(answer))
        if (!answer) contactWithWebsiteCheck.copy(website = None) else contactWithWebsiteCheck
      case Website(answer) => business.copy(website = Some(answer))
      case BusinessActivityDescription(answer) => business.copy(businessDescription = Some(answer))
      case MainBusinessActivity(sicCode) => business.copy(mainBusinessActivity = Some(sicCode))
      case BusinessActivities(sicCodes) => business.copy(businessActivities = Some(sicCodes))
      case LandAndPropertyAnswer(answer) => business.copy(hasLandAndProperty = Some(answer))
      case OtherBusinessInvolvementAnswer(answer) => business.copy(otherBusinessInvolvement = Some(answer))
      case labour: LabourCompliance => business.copy(labourCompliance = Some(labour))
      case updatedBusiness: Business => updatedBusiness
      case _ => business
    }
  }
  // scalastyle:on

  def isLabourComplianceModelComplete(labourCompliance: LabourCompliance): Boolean = {
    labourCompliance match {
      case LabourCompliance(_, Some(_), Some(false)) => true
      case LabourCompliance(Some(_), _, Some(true)) => true
      case _ => false
    }
  }

  def needComplianceQuestions(sicCodes: List[SicCode]): Boolean = {
    val complianceSicCodes = Set(
      "42110", "42910", "43999", "41201", "43120", "42990",
      "01610", "78200", "80100", "81210", "81221", "81222",
      "81223", "81291", "81299")

    complianceSicCodes.intersect(sicCodes.map(_.code).toSet).nonEmpty
  }
}

object BusinessService {
  case class ConfirmTradingName(answer: Boolean)
  case class TradingName(answer: String)
  case class ShortOrgNameAnswer(answer: String)
  case class Email(answer: String)
  case class TelephoneNumber(answer: String)
  case class HasWebsiteAnswer(answer: Boolean)
  case class Website(answer: String)
  case class LandAndPropertyAnswer(answer: Boolean)
  case class VatCorrespondenceInWelsh(answer: Boolean)
  case class OtherBusinessInvolvementAnswer(answer: Boolean)
  case class BusinessActivityDescription(answer: String)
  case class BusinessActivities(sicCodes: List[SicCode])
  case class MainBusinessActivity(sicCode: SicCode)
}
