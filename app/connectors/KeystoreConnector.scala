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

package connectors

import javax.inject.Inject
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import repositories.SessionRepository
import utils.CascadeUpsert

import scala.concurrent.Future

class KeystoreConnector @Inject()(val sessionCache: SessionCache, val sessionRepository: SessionRepository, val cascadeUpsert: CascadeUpsert) extends KeystoreConnect

trait KeystoreConnect {
  val sessionCache: SessionCache
  val sessionRepository: SessionRepository
  val cascadeUpsert: CascadeUpsert

  def CHANGE_ME()(implicit hc:HeaderCarrier): String = hc.sessionId.get.value //todo: refactor hc.sessionId.get.value later

  def cache[T](formId: String, body : T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    sessionRepository().get(CHANGE_ME).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(formId, body, optionalCacheMap.getOrElse(new CacheMap(CHANGE_ME, Map())))
      sessionRepository().upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def fetch(implicit hc : HeaderCarrier) : Future[Option[CacheMap]] = {
    sessionRepository().get(CHANGE_ME)
  }

  def fetchAndGet[T](key : String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] = {
    fetch.map { optionalCacheMap =>
      optionalCacheMap.flatMap { cacheMap => cacheMap.getEntry(key)}
    }
  }

  def remove(key: String)(implicit hc : HeaderCarrier) : Future[Boolean] = {
    sessionRepository().get(CHANGE_ME()).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(Future(false)) { cacheMap =>
        sessionRepository().removeDocument(CHANGE_ME())
      }
    }
  }
//  def cache[T](formId: String, body : T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = sessionCache.cache[T](formId, body)
//  def fetch(implicit hc : HeaderCarrier) : Future[Option[CacheMap]]                                       = sessionCache.fetch()
//  def fetchAndGet[T](key : String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]]      = sessionCache.fetchAndGetEntry(key)
//  def remove(implicit hc : HeaderCarrier) : Future[HttpResponse]                                          = sessionCache.remove()
}
