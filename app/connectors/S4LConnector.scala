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

package connectors

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class S4LConnector @Inject()(val shortCache: ShortLivedCache)
                            (implicit ec: ExecutionContext) {

  def save[T](Id: String, formId: String, data: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[CacheMap] =
    shortCache.cache[T](Id, formId, data)

  def fetchAndGet[T](Id: String, formId: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[Option[T]] =
    shortCache.fetchAndGetEntry[T](Id, formId)

  def clear(Id: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = shortCache.remove(Id)

  def fetchAll(Id: String)(implicit hc: HeaderCarrier): Future[Option[CacheMap]] = shortCache.fetch(Id)
}
