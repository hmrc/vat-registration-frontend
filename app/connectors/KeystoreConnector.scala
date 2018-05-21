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
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.CascadeUpsert

import scala.concurrent.{ExecutionContext, Future}

class KeystoreConnectorImpl @Inject()(val sessionCache: SessionCache, val sessionRepository: SessionRepository, val cascadeUpsert: CascadeUpsert) extends KeystoreConnector

trait KeystoreConnector {
  val sessionCache: SessionCache
  val sessionRepository: SessionRepository
  val cascadeUpsert: CascadeUpsert

  def sessionID(implicit hc:HeaderCarrier): String = hc.sessionId.getOrElse(throw new RuntimeException("Active User had no Session ID")).value

  def cache[T](formId: String, body : T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    sessionRepository().get(sessionID).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(formId, body, optionalCacheMap.getOrElse(new CacheMap(sessionID, Map())))
      sessionRepository().upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def fetch(implicit hc : HeaderCarrier) : Future[Option[CacheMap]] = {
    sessionRepository().get(sessionID)
  }

  def fetchAndGet[T](key : String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] = {
    fetch.map { optionalCacheMap =>
      optionalCacheMap.flatMap { cacheMap => cacheMap.getEntry(key)}
    }
  }

  def remove(implicit hc : HeaderCarrier) : Future[Boolean] = {
    sessionRepository().get(sessionID).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(Future(false)) { _ =>
        sessionRepository().removeDocument(sessionID)
      }
    }
  }

  def addRejectionFlag(txId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    for {
      reject <- sessionRepository().addRejectionFlag(txId)
      regId <- sessionRepository().getRegistrationID(txId)
    } yield regId
  }
}
