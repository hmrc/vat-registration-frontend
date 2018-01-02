/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import cats.data.OptionT
import connectors._
import models.{CurrentProfile, S4LKey, ViewModelFormat}
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class PersistenceService @Inject()(val s4LConnector: S4LConnect, val keystoreConnector: KeystoreConnect) extends S4LService

trait S4LService extends CommonService {

  val s4LConnector: S4LConnect

  def save[T: S4LKey](data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    s4LConnector.save[T](profile.registrationId, S4LKey[T].key, data)
  }

  def updateViewModel[T, G](data: T, container: Future[G])
                           (implicit hc: HeaderCarrier,
                            profile: CurrentProfile,
                            vmf: ViewModelFormat.Aux[T, G],
                            f: Format[G],
                            k: S4LKey[G]): Future[CacheMap] = for {
      group <- container
      cm <- s4LConnector.save(profile.registrationId, k.key, vmf.update(data, Some(group)))
    } yield cm

  def saveNoAux[T](data: T, s4LKey: S4LKey[T])(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    s4LConnector.save(profile.registrationId, s4LKey.key, data)
  }

  def fetchAndGet[T: S4LKey](implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, S4LKey[T].key)

  def fetchAndGetNoAux[T](key: S4LKey[T])(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, key.key)

  def getViewModel[T, G](container: Future[G])(implicit r: ViewModelFormat.Aux[T, G], f: Format[G], hc: HeaderCarrier): OptionalResponse[T] = {
    OptionT(container.map(r.read))
  }

  def clear(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[HttpResponse] =
    s4LConnector.clear(profile.registrationId)

  def fetchAll(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[CacheMap]] =
    s4LConnector.fetchAll(profile.registrationId)

  def save[T](key: String, data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    s4LConnector.save[T](profile.registrationId, key, data)

  def fetchAndGet[T](key: String)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, key)
}
