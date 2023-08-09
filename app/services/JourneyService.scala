/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.CurrentProfile
import play.api.libs.json.Format
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject()(val vatRegistrationService: VatRegistrationService,
                               val sessionService: SessionService,
                               val config: FrontendAppConfig
                              )(implicit ec: ExecutionContext) {
  
  def buildCurrentProfile(regId: String)(implicit hc: HeaderCarrier, profileFormat: Format[CurrentProfile], request: Request[_]): Future[CurrentProfile] =
    for {
      status <- vatRegistrationService.getStatus(regId)
      profile = CurrentProfile(
        registrationId = regId,
        vatRegistrationStatus = status
      )
      _ <- sessionService.cache[CurrentProfile]("CurrentProfile", profile)
    } yield profile

}
