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

import config.Logging
import connectors.RegistrationApiConnector
import models.{CurrentProfile, OtherBusinessInvolvement}
import play.api.mvc.Request
import services.OtherBusinessInvolvementsService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherBusinessInvolvementsService @Inject()(registrationApiConnector: RegistrationApiConnector)
                                                (implicit ec: ExecutionContext) extends Logging {

  def getOtherBusinessInvolvement(index: Int)(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[OtherBusinessInvolvement]] =
    registrationApiConnector.getSection[OtherBusinessInvolvement](profile.registrationId, Some(index))

  def getOtherBusinessInvolvements(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[List[OtherBusinessInvolvement]] =
    registrationApiConnector.getListSection[OtherBusinessInvolvement](profile.registrationId)

  def getHighestValidIndex(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Int] =
    getOtherBusinessInvolvements.map(_.length + 1)

  def updateOtherBusinessInvolvement[T](index: Int, data: T)(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier, request: Request[_]): Future[OtherBusinessInvolvement] = {
    for {
      otherBusinessInvolvement <- getOtherBusinessInvolvement(index)
      updatedOtherBusinessInvolvement = updateModel(otherBusinessInvolvement.getOrElse(OtherBusinessInvolvement()), data)
      result <- registrationApiConnector.replaceSection(profile.registrationId, updatedOtherBusinessInvolvement, Some(index))
    } yield result
  }

  def upsertObiList(data: List[OtherBusinessInvolvement])(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[List[OtherBusinessInvolvement]] =
    registrationApiConnector.replaceListSection(profile.registrationId, data)

  def deleteOtherBusinessInvolvement(index: Int)(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Boolean] = {
    registrationApiConnector.deleteSection[OtherBusinessInvolvement](regId = profile.registrationId, idx = Some(index))
  }

  def deleteOtherBusinessInvolvements(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Boolean] = {
    registrationApiConnector.deleteSection[OtherBusinessInvolvement](regId = profile.registrationId)
  }

  private[services] def updateModel[T](otherBusinessInvolvement: OtherBusinessInvolvement, data: T): OtherBusinessInvolvement =
    data match {
      case BusinessNameAnswer(answer) => otherBusinessInvolvement.copy(businessName = Some(answer))
      case HasVrnAnswer(true) => otherBusinessInvolvement.copy(hasVrn = Some(true), hasUtr = None, utr = None)
      case HasVrnAnswer(false) => otherBusinessInvolvement.copy(hasVrn = Some(false), vrn = None)
      case VrnAnswer(answer) => otherBusinessInvolvement.copy(vrn = Some(answer))
      case HasUtrAnswer(true) => otherBusinessInvolvement.copy(hasUtr = Some(true))
      case HasUtrAnswer(false) => otherBusinessInvolvement.copy(hasUtr = Some(false), utr = None)
      case UtrAnswer(answer) => otherBusinessInvolvement.copy(utr = Some(answer))
      case StillTradingAnswer(answer) => otherBusinessInvolvement.copy(stillTrading = Some(answer))
    }

}

object OtherBusinessInvolvementsService {
  case class BusinessNameAnswer(answer: String)

  case class HasVrnAnswer(answer: Boolean)

  case class VrnAnswer(answer: String)

  case class HasUtrAnswer(answer: Boolean)

  case class UtrAnswer(answer: String)

  case class StillTradingAnswer(answer: Boolean)
}