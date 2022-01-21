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

import common.enums.VatRegStatus
import config.AuthClientConnector
import connectors.S4LConnector
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import services.SaveAndRetrieveService.vatSchemeKey
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, InternalServerException}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SaveAndRetrieveService @Inject()(vatRegistrationService: VatRegistrationService,
                                       val s4LConnector: S4LConnector,
                                       val authConnector: AuthClientConnector)
                                      (implicit executionContext: ExecutionContext) extends AuthorisedFunctions {

  def savePartialVatScheme(regId: String)(implicit hc: HeaderCarrier): Future[CacheMap] = {
    for {
      vatSchemeJson <- vatRegistrationService.getVatSchemeJson(regId)
      cacheMap <- s4LConnector.save(regId, vatSchemeKey, vatSchemeJson)
    } yield cacheMap
  }

}

object SaveAndRetrieveService {
  val vatSchemeKey = "partialVatScheme"
}
