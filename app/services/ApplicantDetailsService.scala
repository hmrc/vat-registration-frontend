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

import config.Logging
import connectors.RegistrationApiConnector
import models.external._
import models.view._
import models._
import play.api.libs.json.{Format, Reads, Writes}
import services.ApplicantDetailsService.HasFormerName
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicantDetailsService @Inject()(val registrationApiConnector: RegistrationApiConnector,
                                        val vatRegistrationService: VatRegistrationService,
                                        val s4LService: S4LService
                                       )(implicit ec: ExecutionContext) extends Logging {

  def getApplicantDetails(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    vatRegistrationService.partyType.flatMap { partyType =>
      implicit val reads: Reads[ApplicantDetails] = ApplicantDetails.s4LReads(partyType)
      s4LService.fetchAndGet[ApplicantDetails].flatMap {
        case None | Some(ApplicantDetails(None, None, None, None, None, None, None, None, None, None, None)) =>
          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
          registrationApiConnector.getSection[ApplicantDetails](cp.registrationId).flatMap {
            case Some(applicantDetails) => Future.successful(applicantDetails)
            case None => Future.successful(ApplicantDetails())
          }
        case Some(applicantDetails) => Future.successful(applicantDetails)
      }
    }
  }

  def getDateOfIncorporation(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[LocalDate]] =
    getApplicantDetails.map(_.entity.flatMap(_.dateOfIncorporation))

  def getCompanyName(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[String]] =
    for {
      applicant <- getApplicantDetails
    } yield {
      applicant.entity.flatMap {
        case incorporatedEntity: IncorporatedEntity => incorporatedEntity.companyName
        case minorEntity: MinorEntity => minorEntity.companyName
        case partnershipIdEntity: PartnershipIdEntity => partnershipIdEntity.companyName
        case _ => throw new InternalServerException("Attempted to get company name for a partyType without one")
      }
    }

  def getTransactorApplicantName(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[String]] =
    for {
      applicant <- getApplicantDetails
      personalDetails = applicant.personalDetails
      isTransactor <- vatRegistrationService.isTransactor
    } yield if (isTransactor) personalDetails.map(_.firstName) else None

  private def isModelComplete(applicantDetails: ApplicantDetails): Completion[ApplicantDetails] = {
    applicantDetails match {
      case ApplicantDetails(None, None, None, None, None, None, None, _, None, None, None) =>
        Incomplete(applicantDetails)
      case ApplicantDetails(Some(SoleTraderIdEntity(_, _, _, _, _, _, _, _, _, _, _)), Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), _, _, Some(_), _) =>
        Complete(applicantDetails.copy(roleInTheBusiness = Some(OwnerProprietor)))
      case ApplicantDetails(Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), _, _, Some(_), Some(_)) =>
        Complete(applicantDetails)
      case _ =>
        Incomplete(applicantDetails)
    }
  }

  def saveApplicantDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    getApplicantDetails.flatMap { applicantDetails =>
      isModelComplete(updateModel(data, applicantDetails)).fold(
        incomplete => {
          implicit val writes: Writes[ApplicantDetails] = ApplicantDetails.s4LWrites
          s4LService.save[ApplicantDetails](incomplete).map(_ => incomplete)
        },
        complete => for {
          partyType <- vatRegistrationService.partyType
          _ <- {
            implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
            registrationApiConnector.replaceSection[ApplicantDetails](cp.registrationId, complete)
          }
          _ <- s4LService.clearKey[ApplicantDetails]
        } yield complete
      )
    }
  }

  //scalastyle:off
  private def updateModel[T](data: T, before: ApplicantDetails): ApplicantDetails = {
    data match {
      case businessEntity: BusinessEntity =>
        before.copy(entity = Some(businessEntity))
      case personalDetails: PersonalDetails =>
        before.copy(personalDetails = Some(personalDetails))
      case currAddr: HomeAddressView =>
        before.copy(homeAddress = Some(currAddr))
      case emailAddress: EmailAddress =>
        before.copy(emailAddress = Some(emailAddress))
      case emailVerified: EmailVerified =>
        before.copy(emailVerified = Some(emailVerified))
      case telephoneNumber: TelephoneNumber =>
        before.copy(telephoneNumber = Some(telephoneNumber))
      case fName: Name =>
        before.copy(formerName = Some(fName))
      case fNameDate: FormerNameDateView =>
        before.copy(formerNameDate = Some(fNameDate))
      case prevAddr: PreviousAddressView =>
        before.copy(previousAddress = Some(prevAddr))
      case roleInTheBusiness: RoleInTheBusiness =>
        before.copy(roleInTheBusiness = Some(roleInTheBusiness))
      case HasFormerName(hasFormerName) if !hasFormerName =>
        before.copy(hasFormerName = Some(hasFormerName), formerName = None, formerNameDate = None)
      case HasFormerName(hasFormerName) =>
        before.copy(hasFormerName = Some(hasFormerName))
    }
  }
}

object ApplicantDetailsService {
  case class HasFormerName(hasFormerName: Boolean)
}