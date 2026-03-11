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

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model._
import play.api.libs.json._
import config.FrontendAppConfig
import models.Lock
import models.Lock._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserLockRepository @Inject()(
                                    mongoComponent: MongoComponent,
                                    appConfig: FrontendAppConfig
                                  )(implicit ec: ExecutionContext) extends PlayMongoRepository[Lock](
  collectionName = "user-lock",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[Lock]],
  indexes = Seq(
    IndexModel(
      keys = ascending("lastAttemptedAt"),
      indexOptions = IndexOptions()
        .name("CVEInvalidDataLockExpires")
        .expireAfter(appConfig.ttlLockSeconds, TimeUnit.SECONDS)
    ),
    IndexModel(
      keys = ascending("identifier"),
      indexOptions = IndexOptions()
        .name("IdentifierIdx")
        .sparse(true)
        .unique(true)
    )
  ),
  replaceIndexes = true
) {

  def getFailedAttempts(identifier: String): Future[Int] =
    collection
      .find(Filters.eq("identifier", identifier))
      .headOption()
      .map(_.map(_.failedAttempts).getOrElse(0))

  def isUserLocked(userId: String): Future[Boolean] = {
    collection
      .find(Filters.in("identifier", userId))
      .toFuture()
      .map { _.exists { _.failedAttempts >= appConfig.knownFactsLockAttemptLimit }}
  }

  def updateAttempts(userId: String): Future[Map[String, Int]] = {
    def updateAttemptsForLockWith(identifier: String): Future[Lock] = {
      collection
        .find(Filters.eq("identifier", identifier))
        .headOption()
        .flatMap {
          case Some(existingLock) =>
            val newLock = existingLock.copy(
              failedAttempts = existingLock.failedAttempts + 1,
              lastAttemptedAt = Instant.now()
            )
            collection.replaceOne(
                Filters.and(
                  Filters.eq("identifier", identifier)
                ),
                newLock
              )
              .toFuture()
              .map(_ => newLock)
          case _ =>
            val newLock = Lock(identifier, 1, Instant.now)
            collection.insertOne(newLock)
              .toFuture()
              .map(_ => newLock)
        }
    }
    val updateUserLock = updateAttemptsForLockWith(userId)

    for {
      userLock <- updateUserLock
    } yield {
      Map(
        "user" -> userLock.failedAttempts
      )
    }
  }
}