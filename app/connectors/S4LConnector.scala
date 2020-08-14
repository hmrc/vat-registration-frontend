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

package connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class S4LConnector @Inject()(val shortCache: ShortLivedCache) {

  def save[T](Id: String, formId: String, data: T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    shortCache.cache[T](Id, formId, data)

  def fetchAndGet[T](Id: String, formId: String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    shortCache.fetchAndGetEntry[T](Id, formId)

  def clear(Id: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = shortCache.remove(Id)

  def fetchAll(Id: String)(implicit hc: HeaderCarrier): Future[Option[CacheMap]] = shortCache.fetch(Id)
}
