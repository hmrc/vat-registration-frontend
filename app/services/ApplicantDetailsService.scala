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
import config.Logging
import connectors.VatRegistrationConnector
import javax.inject.{Inject, Singleton}
import models.external.incorporatedentityid.IncorporationDetails
import models.external.{EmailAddress, EmailVerified}
import models.view.{ApplicantDetails, _}
import models.{CurrentProfile, S4LKey, TelephoneNumber, TransactorDetails}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class ApplicantDetailsService @Inject()(val vatRegistrationConnector: VatRegistrationConnector,
                                        val s4LService: S4LService) extends Logging {

  val director = "03"

  def getApplicantDetails(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    s4LService.fetchAndGetNoAux[ApplicantDetails](S4LKey[ApplicantDetails]) flatMap {
      case Some(applicant) => Future.successful(applicant)
      case _ => vatRegistrationConnector.getApplicantDetails(cp.registrationId) flatMap { json =>
        val applicantDetails = json.fold(ApplicantDetails())(ApplicantDetails.apiReads.reads(_) match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw new Exception(errors.toString())
        })
        s4LService.saveNoAux[ApplicantDetails](applicantDetails, S4LKey[ApplicantDetails]) map (_ => applicantDetails)
      }
    }
  }

  def getDateOfIncorporation(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[LocalDate]] =
    getApplicantDetails.map(_.incorporationDetails.map(_.dateOfIncorporation))

  private def updateApplicantDetails(data: ApplicantDetails)
                                    (implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[ApplicantDetails] = {
    for {
      _ <- vatRegistrationConnector.patchApplicantDetails(data)
      _ <- s4LService.clear
    } yield data
  }

  private def isModelComplete(applicantDetails: ApplicantDetails): Completion[ApplicantDetails] = applicantDetails match {
    case ApplicantDetails(None, None, None, None, None, None, None, None, None) =>
      Incomplete(applicantDetails)
    case ApplicantDetails(Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), Some(fName), fNameDate, Some(_)) if fName.yesNo && fNameDate.isDefined =>
      Complete(applicantDetails)
    case ApplicantDetails(Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), Some(fName), _, Some(_)) if !fName.yesNo =>
      Complete(applicantDetails)
    case _ =>
      Incomplete(applicantDetails)
  }

  def saveApplicantDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    def updateModel(data: T, before: ApplicantDetails): ApplicantDetails = {
      data match {
        case incorporationDetails: IncorporationDetails =>
          before.copy(incorporationDetails = Some(incorporationDetails))
        case transactorDetails: TransactorDetails =>
          before.copy(transactorDetails = Some(transactorDetails.copy(role = Some(director))))
        case currAddr: HomeAddressView =>
          before.copy(homeAddress = Some(currAddr))
        case emailAddress: EmailAddress =>
          before.copy(emailAddress = Some(emailAddress))
        case emailVerified: EmailVerified =>
          before.copy(emailVerified = Some(emailVerified))
        case telephoneNumber: TelephoneNumber =>
          before.copy(telephoneNumber = Some(telephoneNumber))
        case fName: FormerNameView =>
          before.copy(formerName = Some(fName), formerNameDate = None)
        case fNameDate: FormerNameDateView =>
          before.copy(formerNameDate = Some(fNameDate))
        case prevAddr: PreviousAddressView =>
          before.copy(previousAddress = Some(prevAddr))
      }
    }

    getApplicantDetails flatMap { applicantDetails =>
      isModelComplete(updateModel(data, applicantDetails)).fold(
        incomplete => s4LService.saveNoAux[ApplicantDetails](incomplete, S4LKey[ApplicantDetails]) map (_ => incomplete),
        complete => updateApplicantDetails(complete)
      )
    }
  }
}