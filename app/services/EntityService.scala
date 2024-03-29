/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{CurrentProfile, Entity}
import models.Entity.leadEntityIndex
import models.api._
import models.external._
import play.api.mvc.Request
import services.EntityService._
import uk.gov.hmrc.http._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntityService @Inject()(val sessionService: SessionService,
                              val registrationApiConnector: RegistrationApiConnector
                               )(implicit ec: ExecutionContext) {

  def getAllEntities(regId: String)(implicit hc: HeaderCarrier): Future[List[Entity]] = {
    registrationApiConnector.getListSection[Entity](regId)
  }

  def getEntity(regId: String, idx: Int)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Entity]] =
    registrationApiConnector.getSection[Entity](regId, Some(idx))

  def upsertEntityList(regId: String, data: List[Entity])(implicit hc: HeaderCarrier): Future[List[Entity]] =
    registrationApiConnector.replaceListSection(regId, data)

  def upsertEntity[T](regId: String, index: Int, data: T)(implicit hc: HeaderCarrier, request: Request[_]): Future[Entity] = {
    for {
      entity <- getEntity(regId, index).map(_.getOrElse {
        data match {
          case partyType: PartyType if index == leadEntityIndex => Entity(None, partyType, Some(true), None, None, None, None)
          case partyType: PartyType => Entity(None, partyType, Some(false), None, None, None, None)
          case _ => throw new InternalServerException(s"[EntityService] Attempted to update an entity without a stored partyType, index: $index")
        }
      })
      result <- registrationApiConnector.replaceSection(regId, updateEntityModel(data, entity), Some(index))
    } yield result
  }

  private def updateEntityModel[T](data: T, entity: Entity): Entity = {
    data match {
      case details: PartnershipIdEntity =>
        val updatedPartnerDetails = if (entity.partyType.equals(ScotPartnership)) {
          details.copy(companyName = entity.optScottishPartnershipName)
        } else {
          details
        }
        entity.copy(details = Some(updatedPartnerDetails))
      case details: BusinessEntity => entity.copy(details = Some(details))
      case partyType: PartyType =>
        if (entity.partyType != partyType) {
          entity.copy(details= None, partyType = partyType, optScottishPartnershipName = None)
        } else {
          entity
        }
      case scottishPartnershipName: ScottishPartnershipName =>
        entity.copy(optScottishPartnershipName = Some(scottishPartnershipName.answer))
      case telephone: Telephone =>
        entity.copy(telephoneNumber = Some(telephone.answer))
      case address: Address => entity.copy(address = Some(address))
      case Email(answer) => entity.copy(email = Some(answer))
    }
  }

  def deleteEntity(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Boolean] =
    registrationApiConnector.deleteSection[Entity](regId, Some(index))

  def getNextValidIndex(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Int] =
    getAllEntities(profile.registrationId).map(_.size + 1)
}

object EntityService {
  case class ScottishPartnershipName(answer: String)
  case class Telephone(answer: String)
  case class Email(answer: String)
}