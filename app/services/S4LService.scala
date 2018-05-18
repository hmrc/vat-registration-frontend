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

import connectors._
import models.{CurrentProfile, S4LKey}
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class S4LServiceImpl @Inject()(val s4LConnector: S4LConnect) extends S4LService

trait S4LService {
  val s4LConnector: S4LConnect

  def save[T: S4LKey](data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    s4LConnector.save[T](profile.registrationId, S4LKey[T].key, data)
  }

  def saveNoAux[T](data: T, s4LKey: S4LKey[T])(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    s4LConnector.save(profile.registrationId, s4LKey.key, data)
  }

  def fetchAndGet[T: S4LKey](implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, S4LKey[T].key)

  def fetchAndGetNoAux[T](key: S4LKey[T])(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, key.key)

  def clear(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[HttpResponse] =
    s4LConnector.clear(profile.registrationId)

  def save[T](key: String, data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    s4LConnector.save[T](profile.registrationId, key, data)

  def fetchAndGet[T](key: String)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, key)
}
