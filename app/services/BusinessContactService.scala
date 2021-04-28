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

import _root_.models.api.Address
import _root_.models.{BusinessContact, CompanyContactDetails, ContactPreference, CurrentProfile}
import connectors.VatRegistrationConnector
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessContactService @Inject()(val registrationConnector: VatRegistrationConnector,
                                       val s4lService: S4LService) {

  def getBusinessContact(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessContact] = {
    s4lService.fetchAndGet[BusinessContact].flatMap {
      case None | Some(BusinessContact(None, None, None)) => registrationConnector.getBusinessContact.map {
        case Some(businessContact) => businessContact
        case None => BusinessContact()
      }
      case Some(businessContact) => Future.successful(businessContact)
    }
  }

  def updateBusinessContact[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    getBusinessContact.flatMap { businessContact =>
      isModelComplete(updateBusinessContactModel[T](data, businessContact)).fold(
        incomplete => s4lService.save[BusinessContact](incomplete).map(_ => data),
        complete => registrationConnector.upsertBusinessContact(complete) flatMap { _ =>
          s4lService.clearKey[BusinessContact].map(_ => data)
        }
      )
    }
  }

  private def updateBusinessContactModel[T](data: T, businessContact: BusinessContact): BusinessContact = {
    data match {
      case address: Address => businessContact.copy(ppobAddress = Some(address))
      case details: CompanyContactDetails => businessContact.copy(companyContactDetails = Some(details))
      case preference: ContactPreference => businessContact.copy(contactPreference = Some(preference))
    }
  }

  private def isModelComplete: BusinessContact => Completion[BusinessContact] = {
    case businessContact@BusinessContact(Some(_), Some(_), Some(_)) => Complete(businessContact)
    case businessContact => Incomplete(businessContact)
  }

}
