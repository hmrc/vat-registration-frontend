/*
 * Copyright 2026 HM Revenue & Customs
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
import models._
import models.api.Address
import play.api.mvc.Request
import services.TransactorDetailsService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactorDetailsService @Inject()(registrationsApiConnector: RegistrationApiConnector)
                                        (implicit ec: ExecutionContext) extends Logging {

  def getTransactorDetails(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[TransactorDetails] = {
    registrationsApiConnector.getSection[TransactorDetails](cp.registrationId).map {
      case Some(details) => details
      case None => TransactorDetails()
    }
  }

  def saveTransactorDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[TransactorDetails] = {
    for {
      transactorDetails <- getTransactorDetails
      updatedTransactorDetails = updateModel(data, transactorDetails)
      result <- registrationsApiConnector.replaceSection[TransactorDetails](cp.registrationId, updatedTransactorDetails)
    } yield result
  }

  //scalastyle:off
  private def updateModel[T](data: T, before: TransactorDetails): TransactorDetails = {
    data match {
      case personalDetails: PersonalDetails =>
        if (personalDetails.arn.isDefined) {
          before.copy(personalDetails = Some(personalDetails), isPartOfOrganisation = None, organisationName = None, address = None)
        } else {
          before.copy(personalDetails = Some(personalDetails))
        }
      case PartOfOrganisation(answer) =>
        if (answer) {
          before.copy(isPartOfOrganisation = Some(answer))
        } else {
          before.copy(isPartOfOrganisation = Some(answer), organisationName = None)
        }
      case OrganisationName(answer) =>
        before.copy(organisationName = Some(answer))
      case Telephone(answer) =>
        before.copy(telephone = Some(answer))
      case TransactorEmail(answer) =>
        before.copy(email = Some(answer))
      case TransactorEmailVerified(answer) =>
        before.copy(emailVerified = Some(answer))
      case address: Address =>
        before.copy(address = Some(address))
      case declarationCapacity: DeclarationCapacityAnswer =>
        before.copy(declarationCapacity = Some(declarationCapacity))
    }
  }
  //scalastyle:on
}

object TransactorDetailsService {

  case class PartOfOrganisation(answer: Boolean)

  case class OrganisationName(answer: String)

  case class Telephone(answer: String)

  case class TransactorEmail(answer: String)

  case class TransactorEmailVerified(answer: Boolean)

}