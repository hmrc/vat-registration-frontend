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

import connectors.RegistrationApiConnector
import models.Entity
import models.api.{PartyType, ScotPartnership}
import models.external.{BusinessEntity, PartnershipIdEntity}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntityService @Inject()(val s4LService: S4LService,
                              val sessionService: SessionService,
                              val registrationApiConnector: RegistrationApiConnector
                               )(implicit ec: ExecutionContext) {

  def getAllEntities(regId: String)(implicit hc: HeaderCarrier): Future[List[Entity]] = {
    registrationApiConnector.getListSection[Entity](regId)
  }

  def getEntity(regId: String, idx: Int)(implicit hc: HeaderCarrier): Future[Entity] =
    registrationApiConnector.getSection[Entity](regId, Some(idx)).map(_.getOrElse(
      throw new InternalServerException(s"Missing entity for at index '$idx'")
    ))

  def upsertEntity[T](regId: String, index: Int, data: T)(implicit hc: HeaderCarrier): Future[Entity] = {
    for {
      entity <- getEntity(regId, index).recoverWith {
        case ex => data match {
          case partyType: PartyType => Future.successful(Entity(None, partyType, Some(true), None))
          case _ => throw ex
        }
      }
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
      case scottishPartnershipName: String => entity.copy(optScottishPartnershipName = Some(scottishPartnershipName))
    }
  }

  def deleteEntity(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Boolean] =
    registrationApiConnector.deleteSection[Entity](regId, Some(index))
}