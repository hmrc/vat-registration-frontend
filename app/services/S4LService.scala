/*
 * Copyright 2017 HM Revenue & Customs
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

import com.google.inject.ImplementedBy
import connectors.{KeystoreConnector, S4LConnector}
import models.S4LKey
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PersistenceService])
trait S4LService extends CommonService {

  private[services] val s4LConnector: S4LConnector

  def saveForm[T: S4LKey](data: T)(implicit headerCarrier: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    fetchRegistrationId.flatMap(s4LConnector.saveForm[T](_, S4LKey[T].key, data))

  def fetchAndGet[T: S4LKey]()(implicit headerCarrier: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    fetchRegistrationId.flatMap(s4LConnector.fetchAndGet[T](_, S4LKey[T].key))

  def clear()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    fetchRegistrationId.flatMap(s4LConnector.clear)

  def fetchAll()(implicit hc: HeaderCarrier): Future[Option[CacheMap]] =
    fetchRegistrationId.flatMap(s4LConnector.fetchAll)

}

class PersistenceService extends S4LService {
  //$COVERAGE-OFF$
  override val s4LConnector = S4LConnector
  override val keystoreConnector = KeystoreConnector
  //$COVERAGE-ON$
}
