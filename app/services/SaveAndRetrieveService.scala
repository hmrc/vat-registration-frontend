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

import connectors.S4LConnector
import models.CurrentProfile
import models.api.VatScheme
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SaveAndRetrieveService @Inject()(vatRegistrationService: VatRegistrationService,
                                       val s4LConnector: S4LConnector)
                                      (implicit executionContext: ExecutionContext) {

  val s4lKey = "partialVatScheme"

  def savePartialVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[CacheMap] = {
    for {
      vatScheme <- vatRegistrationService.getVatScheme
      cacheMap <- {
        implicit val format: Format[VatScheme] = VatScheme.s4lFormat
        s4LConnector.save[VatScheme](profile.registrationId, s4lKey, vatScheme)
      }
    } yield cacheMap
  }

}
