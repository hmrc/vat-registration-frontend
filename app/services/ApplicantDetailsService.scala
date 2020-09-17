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

import config.Logging
import connectors.VatRegistrationConnector
import javax.inject.{Inject, Singleton}
import models.external.Name
import models.view.{ApplicantDetails, _}
import models.{CurrentProfile, S4LKey}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class ApplicantDetailsService @Inject()(val vatRegistrationConnector: VatRegistrationConnector,
                                      val s4LService: S4LService) extends Logging {

  def getApplicantDetails(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    s4LService.fetchAndGetNoAux[ApplicantDetails](S4LKey[ApplicantDetails]) flatMap {
      case Some(applicant) => Future.successful(applicant)
      case _ => vatRegistrationConnector.getApplicantDetails(cp.registrationId) flatMap { json =>
        val applicantDetails = json.fold(ApplicantDetails(None, None, None, None, None))(ApplicantDetails.fromApi)
        s4LService.saveNoAux[ApplicantDetails](applicantDetails, S4LKey[ApplicantDetails]) map (_ => applicantDetails)
      }
    }
  }

  private def updateApplicantDetails(data: ApplicantDetails)
                                  (implicit cp: CurrentProfile, headerCarrier: HeaderCarrier): Future[ApplicantDetails] = {
    for {
      _ <- vatRegistrationConnector.patchApplicantDetails(data)
      _ <- s4LService.clear
    } yield data
  }

  private def isModelComplete(applicantDetails: ApplicantDetails): Completion[ApplicantDetails] = applicantDetails match {
    case ApplicantDetails(None, None, None, None, None) =>
      Complete(applicantDetails)
    case ApplicantDetails(Some(_), Some(_), Some(fName), fNameDate, Some(_)) if fName.yesNo && fNameDate.isDefined =>
      Complete(applicantDetails)
    case ApplicantDetails(Some(_), Some(_), Some(fName), _, Some(_)) if !fName.yesNo =>
      Complete(applicantDetails)
    case _ =>
      Incomplete(applicantDetails)
  }

  def saveApplicantDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[ApplicantDetails] = {
    def updateModel(data: T, before: ApplicantDetails): ApplicantDetails = {
      data match {
        case currAddr: HomeAddressView => before.copy(homeAddress = Some(currAddr))
        case contact: ContactDetailsView => before.copy(contactDetails = Some(contact))
        case fName: FormerNameView => before.copy(formerName = Some(fName))
        case fNameDate: FormerNameDateView => before.copy(formerNameDate = Some(fNameDate))
        case prevAddr: PreviousAddressView => before.copy(previousAddress = Some(prevAddr))
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
