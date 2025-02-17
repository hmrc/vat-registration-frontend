/*
 * Copyright 2024 HM Revenue & Customs
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

package repositories

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDateTime}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject()(config: Configuration,
                                  mongo: MongoComponent)
                                 (implicit ec: ExecutionContext)
  extends PlayMongoRepository[DatedCacheMap](
    collectionName = config.get[String]("appName"),
    mongoComponent = mongo,
    domainFormat = DatedCacheMap.formats,
    indexes = Seq(
      model.IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .name("userAnswersExpiry")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
      ),
      model.IndexModel(
        ascending("id"),
        IndexOptions()
          .name("id_Index")
          .unique(true)
      )
    )
  ) {

  val fieldName = "lastUpdated"
  val createdIndexName = "userAnswersExpiry"
  val expireAfterSeconds = "expireAfterSeconds"
  val timeToLiveInSeconds: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  def upsert(cm: CacheMap): Future[Boolean] = {
    collection.replaceOne(
      equal("id", cm.id),
      DatedCacheMap(cm),
      ReplaceOptions().upsert(true)
    ).map(_.wasAcknowledged()).head()
  }

  def removeDocument(id: String): Future[Boolean] = {
    collection.deleteOne(equal("id", id)).map(_.wasAcknowledged()).head()
  }

  def get(id: String): Future[Option[CacheMap]] = {
    collection.find(equal("id", id)).map(_.asCacheMap).headOption()
  }
}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: LocalDateTime = LocalDateTime.now()) {
  def asCacheMap: CacheMap = CacheMap(id, data)
}

object DatedCacheMap {
  implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val formats: OFormat[DatedCacheMap] = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}
