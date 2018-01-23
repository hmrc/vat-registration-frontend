/*
 * Copyright 2018 HM Revenue & Customs
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

package features.businessContact

import javax.inject.Inject

import _root_.models.CurrentProfile
import _root_.models.api.ScrsAddress
import connectors.{RegistrationConnector, S4LConnect}
import features.businessContact.models.{BusinessContact, CompanyContactDetails}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BusinessContactServiceImpl @Inject()(val registrationConnector: RegistrationConnector,
                                           val s4lConnector: S4LConnect) extends BusinessContactService

trait BusinessContactService {
  val registrationConnector: RegistrationConnector
  val s4lConnector: S4LConnect

  private val BUSINESS_CONTACT = "business-contact"

  type Completion[T] = Either[T, T]
  val Incomplete     = scala.util.Left
  val Complete       = scala.util.Right

  def getBusinessContact(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessContact] = {
    def getFromVatRegistration: Future[Option[BusinessContact]] = registrationConnector.getBusinessContact map {
      _.fold[Option[BusinessContact]](None)(x => Some(BusinessContact.fromApi(x)))
    }

    s4lConnector.fetchAndGet[BusinessContact](cp.registrationId, BUSINESS_CONTACT) flatMap {
      case Some(bc) => Future.successful(bc)
      case _        => getFromVatRegistration flatMap { optBC =>
        val businessContact = optBC.getOrElse(BusinessContact())
        s4lConnector.save[BusinessContact](cp.registrationId, BUSINESS_CONTACT, businessContact) map {
          _ => businessContact
        }
      }
    }
  }

  def updateBusinessContact[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    getBusinessContact flatMap { businessContact =>
      isModelComplete(updateBusinessContactModel[T](data, businessContact)).fold(
        incomplete => s4lConnector.save[BusinessContact](cp.registrationId,BUSINESS_CONTACT, incomplete) map(_ => data),
        complete   => registrationConnector.upsertBusinessContact(BusinessContact.toApi(complete)) map(_ => data)
      )
    }
  }

  private def updateBusinessContactModel[T](data: T, businessContact: BusinessContact): BusinessContact = {
    data match {
      case address: ScrsAddress                   => businessContact.copy(ppobAddress = Some(address))
      case contactDetails: CompanyContactDetails  => businessContact.copy(companyContactDetails = Some(contactDetails))
    }
  }

  private val isModelComplete: BusinessContact => Completion[BusinessContact] = businessContact => {
    if(businessContact.ppobAddress.isDefined && businessContact.companyContactDetails.isDefined) {
      Complete(businessContact)
    } else {
      Incomplete(businessContact)
    }
  }
}
