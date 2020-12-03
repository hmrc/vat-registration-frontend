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

import connectors.TrafficManagementConnector
import javax.inject.{Inject, Singleton}
import models.api.trafficmanagement.{Draft, RegistrationInformation, VatReg}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrafficManagementService @Inject()(trafficManagementConnector: TrafficManagementConnector
                                        )(implicit executionContext: ExecutionContext) {

  def passedTrafficManagement(regId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    trafficManagementConnector.getRegistrationInformation.map {
      case Some(RegistrationInformation(_, registrationId, Draft, Some(date), VatReg)) if regId == registrationId =>
        true
      case Some(RegistrationInformation(_, registrationId, Draft, Some(date), VatReg)) =>
        throw new InternalServerException("[TrafficManagementService][passedTrafficManagement] passed traffic management but there is a registrationId mismatch")
      case _ =>
        false
    }

}
