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
import com.mongodb.client.result.DeleteResult
import org.bson.BsonType
import org.mongodb.scala.model
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.model.{Filters, IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.LoggingUtil

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (config: Configuration, mongo: MongoComponent)(implicit ec: ExecutionContext)
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
        )
      )
    )
    with LoggingUtil {

  def upsert(cm: CacheMap): Future[Boolean] =
    collection
      .replaceOne(
        equal("id", cm.id),
        DatedCacheMap(cm),
        ReplaceOptions().upsert(true)
      )
      .map(_.wasAcknowledged())
      .head()

  def removeDocument(id: String): Future[Boolean] =
    collection.deleteOne(equal("id", id)).map(_.wasAcknowledged()).head()

  def get(id: String): Future[Option[CacheMap]] =
    collection.find(equal("id", id)).map(_.asCacheMap).headOption()

  private val filter = Filters.`type`("lastUpdated", BsonType.STRING)

  private def errorMessage(e: Throwable, allOrN: String) =
    s"[MongoRemoveInvalidDataOnStartUp][delete${allOrN}DataWithLastUpdatedStringType] Deletion of data failed with invalid 'lastUpdated' index." +
      s"\n[MongoRemoveInvalidDataOnStartUp][delete${allOrN}DataWithLastUpdatedStringType] Error: $e"

  def deleteNDataWithLastUpdatedStringType(nLimitForDeletion: Int): Future[DeleteResult] =
    if (nLimitForDeletion > 0) { // This seems unnecessary but if limit is zero => no limit
      collection
        .withDocumentClass() // Has to be Document instead of DatedCacheMap to get '_id'
        .find(filter)
        .projection(include("_id"))
        .limit(nLimitForDeletion)
        .toFuture()
        .flatMap { documents =>
          val ids = documents.map(_.getObjectId("_id"))
          if (ids.nonEmpty) {
            val idFilter = Filters.in("_id", ids: _*)
            collection
              .deleteMany(idFilter)
              .toFuture()
              .map { result =>
                logger.warn(s"[MongoRemoveInvalidDataOnStartUp][deleteNDataWithLastUpdatedStringType] Number of DELETED" +
                  s" invalid documents: ${result.getDeletedCount}, limit: $nLimitForDeletion")
                result
              }
          } else {
            Future.successful(DeleteResult.acknowledged(0))
          }
        }
        .recover { case e: Throwable =>
          logger.error(errorMessage(e, "N"))
          DeleteResult.acknowledged(0)
        }
    } else {
      Future.successful(DeleteResult.acknowledged(0))
    }

  def deleteAllDataWithLastUpdatedStringType(): Future[DeleteResult] =
    collection
      .deleteMany(filter)
      .toFuture()
      .map { result =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp][deleteAllDataWithLastUpdatedStringType] Number of DELETED invalid documents: ${result.getDeletedCount}")
        result
      }
      .recover { case e: Throwable =>
        logger.error(errorMessage(e, "All"))
        DeleteResult.acknowledged(0)
      }

}

case class DatedCacheMap(id: String, data: Map[String, JsValue], lastUpdated: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)) {
  def asCacheMap: CacheMap = CacheMap(id, data)
}

object DatedCacheMap {
  implicit val instantDateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val formats: OFormat[DatedCacheMap]    = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}
