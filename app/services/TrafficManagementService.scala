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

import connectors.TrafficManagementConnector
import models.api.trafficmanagement.{ClearTrafficManagementResponse, Draft, OTRS, RegistrationInformation, VatReg}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrafficManagementService @Inject()(trafficManagementConnector: TrafficManagementConnector
                                        )(implicit executionContext: ExecutionContext) {

  def passedTrafficManagement(regId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    trafficManagementConnector.getRegistrationInformation(regId).map {
      case Some(RegistrationInformation(_, registrationId, Draft, Some(_), VatReg)) if regId == registrationId =>
        true
      case Some(RegistrationInformation(_, _, Draft, Some(_), VatReg)) =>
        throw new InternalServerException("[TrafficManagementService][passedTrafficManagement] passed traffic management but there is a registrationId mismatch")
      case _ =>
        false
    }



  def checkTrafficManagement(regId: String)(implicit hc: HeaderCarrier): Future[TrafficManagementResponse] =
    trafficManagementConnector.getRegistrationInformation(regId).map {
      case Some(RegistrationInformation(_, registrationId, Draft, Some(_), VatReg)) =>
        PassedVatReg(registrationId)
      case Some(RegistrationInformation(_, registrationId, Draft, Some(_), OTRS)) =>
        PassedOTRS
      case _ =>
        Failed
    }

  def clearTrafficManagement(implicit hc: HeaderCarrier): Future[ClearTrafficManagementResponse] =
    trafficManagementConnector.clearTrafficManagement
}

sealed trait TrafficManagementResponse

case class PassedVatReg(regId: String) extends TrafficManagementResponse

case object PassedOTRS extends TrafficManagementResponse

case object Failed extends TrafficManagementResponse

case object Cleared
