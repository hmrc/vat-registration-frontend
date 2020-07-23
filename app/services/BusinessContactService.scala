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

import _root_.models.api.ScrsAddress
import _root_.models.{BusinessContact, CompanyContactDetails, CurrentProfile}
import connectors.RegistrationConnector
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BusinessContactServiceImpl @Inject()(val registrationConnector: RegistrationConnector,
                                           val s4lService: S4LService) extends BusinessContactService

trait BusinessContactService {
  val registrationConnector: RegistrationConnector
  val s4lService: S4LService

  def getBusinessContact(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessContact] = {
    def getFromVatRegistration: Future[Option[BusinessContact]] = registrationConnector.getBusinessContact map {
      _.fold[Option[BusinessContact]](None)(x => Some(BusinessContact.fromApi(x)))
    }

    s4lService.fetchAndGet[BusinessContact] flatMap {
      case Some(bc) => Future.successful(bc)
      case _        => getFromVatRegistration flatMap { optBC =>
        val businessContact = optBC.getOrElse(BusinessContact())
        s4lService.save[BusinessContact](businessContact) map {
          _ => businessContact
        }
      }
    }
  }

  def updateBusinessContact[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    getBusinessContact flatMap { businessContact =>
      isModelComplete(updateBusinessContactModel[T](data, businessContact)).fold(
        incomplete => s4lService.save[BusinessContact](incomplete) map(_ => data),
        complete   => registrationConnector.upsertBusinessContact(BusinessContact.toApi(complete)) flatMap { _ =>
          s4lService.clear map(_ => data)
        }
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
