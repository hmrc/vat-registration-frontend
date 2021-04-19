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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SaveAndRetrieveService @Inject()(vatRegistrationService: VatRegistrationService,
                                       val s4LConnector: S4LConnector)
                                      (implicit executionContext: ExecutionContext) {

  val s4lKey = "partialVatScheme"

  def savePartialVatScheme(regId: String)(implicit hc: HeaderCarrier): Future[CacheMap] = {
    for {
      vatSchemeJson <- vatRegistrationService.getVatSchemeJson(regId)
      cacheMap <- s4LConnector.save(regId, s4lKey, vatSchemeJson)
    } yield cacheMap
  }

  def retrievePartialVatScheme(regId: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    s4LConnector.fetchAndGet[JsValue](regId, s4lKey).flatMap {
      case Some(scheme) => vatRegistrationService.storePartialVatScheme(regId, scheme)
      case None => vatRegistrationService.createRegistrationFootprint.map(scheme => Json.toJson(scheme))
    }
  }
}
