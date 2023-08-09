/*
 * Copyright 2023 HM Revenue & Customs
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
import models._
import models.api.Address
import models.external.{BusinessEntity, Name, PartnershipIdEntity, SoleTraderIdEntity}
import play.api.libs.json.Format
import play.api.mvc.Request
import services.ApplicantDetailsService._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import utils.LoggingUtil

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicantDetailsService @Inject()(val registrationApiConnector: RegistrationApiConnector,
                                        val vatRegistrationService: VatRegistrationService
                                       )(implicit ec: ExecutionContext) extends LoggingUtil {

  def getApplicantDetails(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[ApplicantDetails] = {
    vatRegistrationService.partyType.flatMap { partyType =>
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
      registrationApiConnector.getSection[ApplicantDetails](cp.registrationId).flatMap {
        case Some(applicantDetails) => Future.successful(applicantDetails)
        case None => Future.successful(ApplicantDetails())
      }
    }
  }

  def getDateOfIncorporation(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[Option[LocalDate]] =
    getApplicantDetails.map(_.entity.flatMap(_.dateOfIncorporation))

  def getCompanyName(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[Option[String]] =
    for {
      applicant <- getApplicantDetails
    } yield {
      applicant.entity.flatMap(_.getBusinessName)
    }

  def getApplicantNameForTransactorFlow(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[Option[String]] =
    for {
      isTransactor <- vatRegistrationService.isTransactor
      optName <- if (isTransactor) getApplicantDetails.map(_.personalDetails.map(_.firstName)) else Future.successful(None)
    } yield optName

  def saveApplicantDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[ApplicantDetails] = {
    for {
      applicant <- getApplicantDetails
      updatedApplicant = updateModel(data, applicant)
      partyType <- vatRegistrationService.partyType
      result <- {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        registrationApiConnector.replaceSection[ApplicantDetails](cp.registrationId, updatedApplicant)
      }
    } yield result
  }

  //scalastyle:off
  private def updateModel[T](data: T, before: ApplicantDetails)(implicit request: Request[_]): ApplicantDetails = {
    data match {
      case businessEntity: SoleTraderIdEntity =>
        before.copy(
          entity = Some(businessEntity),
          roleInTheBusiness = Some(OwnerProprietor)
        )
      case businessEntity: PartnershipIdEntity =>
        before.copy(
          entity = Some(businessEntity),
          roleInTheBusiness = Some(Partner)
        )
      case businessEntity: BusinessEntity =>
        before.copy(entity = Some(businessEntity))
      case personalDetails: PersonalDetails =>
        before.copy(personalDetails = Some(personalDetails))
      case CurrentAddress(answer) =>
        before.copy(currentAddress = Some(answer))
      case NoPreviousAddress(answer) =>
        if (answer) {
          before.copy(
            noPreviousAddress = Some(answer),
            previousAddress = None
          )
        } else {
          before.copy(noPreviousAddress = Some(answer))
        }
      case PreviousAddress(answer) =>
        before.copy(previousAddress = Some(answer))
      case EmailAddress(answer) =>
        before.copy(contact = before.contact.copy(email = Some(answer)))
      case EmailVerified(answer) =>
        before.copy(contact = before.contact.copy(emailVerified = Some(answer)))
      case TelephoneNumber(answer) =>
        before.copy(contact = before.contact.copy(tel = Some(answer)))
      case HasFormerName(hasFormerName) =>
        if (hasFormerName) {
          before.copy(changeOfName = before.changeOfName.copy(hasFormerName = Some(hasFormerName)))
        } else {
          before.copy(changeOfName = before.changeOfName.copy(
            hasFormerName = Some(hasFormerName),
            name = None,
            change = None
          ))
        }
      case formerName: Name =>
        before.copy(changeOfName = before.changeOfName.copy(name = Some(formerName)))
      case formerNameDate: LocalDate =>
        before.copy(changeOfName = before.changeOfName.copy(change = Some(formerNameDate)))
      case RoleInTheBusinessAnswer(roleInTheBusiness, optOtherRole) =>
        if (roleInTheBusiness.equals(OtherDeclarationCapacity)) {
          before.copy(
            roleInTheBusiness = Some(roleInTheBusiness),
            otherRoleInTheBusiness = optOtherRole
          )
        } else {
          before.copy(
            roleInTheBusiness = Some(roleInTheBusiness),
            otherRoleInTheBusiness = None
          )
        }
      case _ =>
        errorLog(s"[ApplicantDetailsService][updateModel] Attempting to store unsupported data")
        throw new InternalServerException("[ApplicantDetailsService] Attempting to store unsupported data")
    }
  }
}

object ApplicantDetailsService {
  case class CurrentAddress(currentAddress: Address)

  case class NoPreviousAddress(noPreviousAddress: Boolean)

  case class PreviousAddress(previousAddress: Address)

  case class EmailAddress(email: String)

  case class EmailVerified(emailVerified: Boolean)

  case class TelephoneNumber(telephone: String)

  case class HasFormerName(hasFormerName: Boolean)

  case class RoleInTheBusinessAnswer(role: RoleInTheBusiness, otherRole: Option[String])
}