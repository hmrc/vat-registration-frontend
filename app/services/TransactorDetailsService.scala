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

import config.Logging
import models._
import models.api.Address
import services.TransactorDetailsService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactorDetailsService @Inject()(val s4LService: S4LService
                                        )(implicit ec: ExecutionContext) extends Logging {

  def getTransactorDetails(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[TransactorDetails] = {
    s4LService.fetchAndGet[TransactorDetails].flatMap {
      case None | Some(TransactorDetails(None, None, None, None, None, None, None)) =>
        Future.successful(TransactorDetails()) //TODO integrate with new registration api on BE to get transactor details if they're not on S4L
      case Some(transactorDetails) => Future.successful(transactorDetails)
    }
  }

  private def isModelComplete(transactorDetails: TransactorDetails): Completion[TransactorDetails] = {
    transactorDetails match {
      case TransactorDetails(Some(_), Some(false), _, Some(_), Some(_), Some(_), Some(_)) =>
        Complete(transactorDetails.copy(organisationName = None))
      case TransactorDetails(Some(_), Some(true), Some(_), Some(_), Some(_), Some(_), Some(_)) =>
        Complete(transactorDetails)
      case _ =>
        Incomplete(transactorDetails)

    }
  }

  def saveTransactorDetails[T](data: T)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[TransactorDetails] = {
    getTransactorDetails.flatMap { transactorDetails =>
      isModelComplete(updateModel(data, transactorDetails)).fold(
        incomplete => s4LService.save[TransactorDetails](incomplete).map(_ => incomplete),
        complete => for {
          // _ <- TODO store to backend using new api
          _ <- s4LService.clearKey[TransactorDetails]
        } yield complete
      )
    }
  }

  private def updateModel[T](data: T, before: TransactorDetails): TransactorDetails = {
    data match {
      case personalDetails: PersonalDetails =>
        before.copy(personalDetails = Some(personalDetails))
      case partOfOrganisation: PartOfOrganisation =>
        before.copy(isPartOfOrganisation = Some(partOfOrganisation.answer))
      case organisationName: OrganisationName =>
        before.copy(organisationName = Some(organisationName.answer))
      case telephone: Telephone =>
        before.copy(telephone = Some(telephone.answer))
      case email: TransactorEmail =>
        before.copy(email = Some(email.answer))
      case address: Address =>
        before.copy(address = Some(address))
      case declarationCapacity: DeclarationCapacityAnswer =>
        before.copy(declarationCapacity = Some(declarationCapacity))
    }
  }
}

object TransactorDetailsService {

  case class PartOfOrganisation(answer: Boolean)

  case class OrganisationName(answer: String)

  case class Telephone(answer: String)

  case class TransactorEmail(answer: String)

}