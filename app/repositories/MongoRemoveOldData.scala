/*
 * Copyright 2025 HM Revenue & Customs
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

import org.bson.BsonType
import org.mongodb.scala._
import org.mongodb.scala.model.{Filters, Updates}
import utils.LoggingUtil

import java.time.Instant
import java.util.Date
import scala.concurrent.ExecutionContext

object MongoRemoveOldData extends App with LoggingUtil {
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017/vat-registration-frontend")
  val database: MongoDatabase = mongoClient.getDatabase("vat-registration-frontend")
  val collection: MongoCollection[Document] = database.getCollection("vat-registration-frontend")

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val filter = Filters.`type`("lastUpdated", BsonType.STRING)

  collection
    .deleteMany(filter)
    .toFuture()
    .recover {
      case e: Throwable =>
        logger.error(
          s"[MongoRemoveOldData] Failed to delete data with invalid 'lastUpdated' index.\n[MongoRemoveOldData] Error: $e"
        )
    }
    .onComplete(_ => mongoClient.close())
}
