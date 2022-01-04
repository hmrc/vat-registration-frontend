/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.OptionT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import models.CurrentProfile
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionRepository @Inject()(config: Configuration,
                                  mongo: ReactiveMongoComponent)
  extends ReactiveRepository[DatedCacheMap, BSONObjectID](
    config.get[String]("appName"),
    mongo.mongoConnector.db,
    DatedCacheMap.formats
  ) {

  val fieldName = "lastUpdated"
  val createdIndexName = "userAnswersExpiry"
  val expireAfterSeconds = "expireAfterSeconds"
  val timeToLiveInSeconds: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  def upsert(cm: CacheMap): Future[Boolean] = {
    val selector = BSONDocument("id" -> cm.id)
    val cmDocument = Json.toJson(DatedCacheMap(cm))
    val modifier = BSONDocument("$set" -> cmDocument)

    collection.update(ordered = false).one(selector, modifier, upsert = true).map { lastError =>
      lastError.ok
    }
  }

  def removeDocument(id: String): Future[Boolean] = {
    collection.delete().one(BSONDocument("id" -> id)).map(lastError =>
      lastError.ok
    )
  }

  def get(id: String): Future[Option[CacheMap]] =
    collection.find(Json.obj("id" -> id)).one[CacheMap]

  def addRejectionFlag(id: String): Future[Boolean] = {
    val selector = BSONDocument("data.CurrentProfile.transactionID" -> id)
    val modifier = BSONDocument("$set" -> BSONDocument("data.CurrentProfile.incorpRejected" -> true))

    collection.update(ordered = false).one(selector, modifier).map { lastError => lastError.ok }
  }

  def getRegistrationID(transactionID: String): Future[Option[String]] = {
    OptionT[Future, CacheMap](
      collection.find(Json.obj("data.CurrentProfile.transactionID" -> transactionID)).one[CacheMap]
    ).map(
      _.getEntry[CurrentProfile]("CurrentProfile").map(_.registrationId)
    ).value map (_.flatten)
  }
}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC))

object DatedCacheMap {
  implicit val dateFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
  implicit val formats: OFormat[DatedCacheMap] = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}
