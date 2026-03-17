/*
 * Copyright 2026 HM Revenue & Customs
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

import models.Lock
import models.Lock._
import org.mongodb.scala.model._
import org.mongodb.scala.model.Indexes.ascending
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsLockRepository @Inject() (
    mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Lock](
      collectionName = "bars-lock",
      mongoComponent = mongoComponent,
      domainFormat = implicitly[Format[Lock]],
      indexes = Seq(
        IndexModel(
          keys = ascending("lastAttemptedAt"),
          indexOptions = IndexOptions()
            .name("BarsLockExpires")
            .expireAfter(24, TimeUnit.HOURS)
        ),
        IndexModel(
          keys = ascending("identifier"),
          indexOptions = IndexOptions()
            .name("BarsIdentifierIdx")
            .sparse(true)
            .unique(true)
        )
      ),
      replaceIndexes = true
    ) {

  private val attemptLimit = 3

  def getAttemptsUsed(registrationId: String): Future[Int] =
    collection
      .find(Filters.eq("identifier", registrationId))
      .headOption()
      .map(_.map(_.failedAttempts).getOrElse(0))

  def isLocked(registrationId: String): Future[Boolean] =
    getAttemptsUsed(registrationId).map(_ >= attemptLimit)

  def recordFailedAttempt(registrationId: String): Future[Int] = {
    val update = Updates.combine(
      Updates.inc("failedAttempts", 1),
      Updates.set("lastAttemptedAt", Instant.now()),
      Updates.setOnInsert("identifier", registrationId)
    )
    val options = FindOneAndUpdateOptions()
      .upsert(true)
      .returnDocument(ReturnDocument.AFTER)

    collection
      .findOneAndUpdate(
        Filters.eq("identifier", registrationId),
        update,
        options
      )
      .toFutureOption()
      .map(_.map(_.failedAttempts).getOrElse(1))
  }
}
