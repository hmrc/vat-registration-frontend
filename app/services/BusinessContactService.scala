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

import _root_.models.api.Address
import _root_.models.{BusinessContact, CompanyContactDetails, ContactPreference, CurrentProfile}
import connectors.RegistrationApiConnector
import play.api.libs.json.Format
import services.BusinessContactService.{Email, HasWebsiteAnswer, TelephoneNumber, Website}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessContactService @Inject()(val registrationApiConnector: RegistrationApiConnector,
                                       val s4lService: S4LService) {

  def getBusinessContact(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessContact] = {
    s4lService.fetchAndGet[BusinessContact].flatMap {
      case None | Some(BusinessContact(None, None, None, None, None, None, None, None)) =>
        implicit val format: Format[BusinessContact] = BusinessContact.apiFormat

        registrationApiConnector.getSection[BusinessContact](cp.registrationId).map {
          case Some(businessContact) => businessContact
          case None => BusinessContact()
        }
      case Some(businessContact) => Future.successful(businessContact)
    }
  }

  def updateBusinessContact[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    getBusinessContact.flatMap { businessContact =>
      isModelComplete(updateBusinessContactModel[T](data, businessContact)).fold(
        incomplete => {
          s4lService.save[BusinessContact](incomplete).map(_ => data)
        },
        complete => {
          for {
            _ <- {
              implicit val format: Format[BusinessContact] = BusinessContact.apiFormat
              registrationApiConnector.replaceSection[BusinessContact](cp.registrationId, complete)
            }
            _ <- s4lService.clearKey[BusinessContact]
          } yield data
        }
      )
    }
  }

  private def updateBusinessContactModel[T](data: T, businessContact: BusinessContact): BusinessContact = {
    data match {
      case address: Address => businessContact.copy(ppobAddress = Some(address))
      case details: CompanyContactDetails => businessContact.copy(companyContactDetails = Some(details))
      case preference: ContactPreference => businessContact.copy(contactPreference = Some(preference))
      case Email(answer) => businessContact.copy(email = Some(answer))
      case TelephoneNumber(answer) => businessContact.copy(telephoneNumber = Some(answer))
      case HasWebsiteAnswer(answer) =>
        val contactWithWebsiteCheck = businessContact.copy(hasWebsite = Some(answer))
        if (!answer) contactWithWebsiteCheck.copy(website = None) else contactWithWebsiteCheck
      case Website(answer) => businessContact.copy(website = Some(answer))
    }
  }

  private def isModelComplete: BusinessContact => Completion[BusinessContact] = {
    case businessContact@BusinessContact(Some(_), Some(_), _, _, _, _, _, Some(_)) => Complete(businessContact)
    case businessContact@BusinessContact(Some(_), _, Some(_), Some(_), _, Some(_), _, Some(_)) => Complete(businessContact)
    case businessContact => Incomplete(businessContact)
  }

}

object BusinessContactService {
  case class Email(answer: String)
  case class TelephoneNumber(answer: String)
  case class HasWebsiteAnswer(answer: Boolean)
  case class Website(answer: String)
}
