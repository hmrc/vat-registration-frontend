/*
 * Copyright 2017 HM Revenue & Customs
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

import common.enums.VatRegStatus
import config.FrontendAppConfig
import itutil.IntegrationSpecBase
import models.CurrentProfile._
import org.mockito.Mockito.when
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonBoolean, BsonDateTime, BsonDocument, BsonString}
import org.mongodb.scala.{MongoCollection, result}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsBoolean, JsObject, Json, OWrites}
import play.api.test.Helpers._
import services.SessionService
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class SessionRepositoryISpec extends IntegrationSpecBase with AppAndStubs {

  val sId = UUID.randomUUID().toString

  override implicit val hc = HeaderCarrier(sessionId = Some(SessionId(sId)))

  class Setup {
    val repository = app.injector.instanceOf[SessionRepository]

    val connector = app.injector.instanceOf[SessionService]
    await(repository.collection.drop().head())
    await(repository.ensureIndexes)

    implicit val jsObjWts: OWrites[JsObject] = OWrites(identity)

    def count = await(repository.collection.countDocuments().head())
  }

  "SessionRepository" should {
    "cache" when {
      "given a new currentProfile" in new Setup() {
        count mustBe 0
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1
      }
      "given an existing currentProfile" in new Setup() {
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1
        await(connector.cache("CurrentProfile", models.CurrentProfile("newregId", VatRegStatus.draft)))
        count mustBe 1
      }
    }
    "fetch" when {
      "given a currentProfile exists" in new Setup() {
        val currentProfileData: models.CurrentProfile = models.CurrentProfile("regId2", VatRegStatus.draft)
        val key: String                               = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[CacheMap] = await(connector.fetch)
        res.isDefined mustBe true
        res.get.data mustBe Map(key -> Json.toJson(currentProfileData))
      }
    }
    "fetchAndGet" when {
      "given a currentProfile and key" in new Setup() {
        val currentProfileData: models.CurrentProfile = models.CurrentProfile("regId3", VatRegStatus.draft)
        val key: String                               = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[models.CurrentProfile] = await(connector.fetchAndGet(key)(hc, models.CurrentProfile.format))
        res.isDefined mustBe true
        res.get mustBe currentProfileData
      }
      "given no current profile" in new Setup() {
        val key: String = "CurrentProfile"

        val res: Option[models.CurrentProfile] = await(connector.fetchAndGet(key)(hc, models.CurrentProfile.format))
        res.isDefined mustBe false
      }
    }
    "remove" when {
      "there is a current profile to remove" in new Setup() {
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1

        val res: Boolean = await(connector.remove)
        res mustBe true
        count mustBe 0
      }
      "there is no current profile to remove" in new Setup() {
        val res: Boolean = await(connector.remove)

        res mustBe false
        count mustBe 0
      }
      "there are two current profiles" in new Setup() {
        val hc1 = hc.copy(sessionId = Some(SessionId("id1")))

        await(connector.cache("CurrentProfile", currentProfile)(hc1, models.CurrentProfile.format))
        await(connector.cache("CurrentProfile", currentProfile)(hc.copy(sessionId = Some(SessionId("id2"))), models.CurrentProfile.format))
        count mustBe 2

        val res: Boolean = await(connector.remove(hc1))
        res mustBe true
        count mustBe 1
      }
    }
  }

  private val mockAppConfig: FrontendAppConfig   = mock[FrontendAppConfig]
  private val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)

  trait DeleteDataSetup {
    val testSessionRepository: SessionRepository        = app.injector.instanceOf[SessionRepository]
    val mongoCollection: MongoCollection[DatedCacheMap] = testSessionRepository.collection

    val mongoComponent: MongoComponent                = app.injector.instanceOf[MongoComponent]
    val rawMongoCollection: MongoCollection[Document] = mongoComponent.database.getCollection[Document]("vat-registration-frontend")

    def fillDatabaseWithData(rawDataToAdd: Seq[Document]): result.InsertManyResult = {
      await(rawMongoCollection.drop().head())
      await(rawMongoCollection.insertMany(rawDataToAdd).toFuture())
    }
  }

  private val validData1 = DatedCacheMap(
    id = "valid-id-1",
    data = Map("voluntaryRegistration" -> JsBoolean(true)),
    lastUpdated = Instant.now()
  )
  private val validData2 = DatedCacheMap(
    id = "valid-id-2",
    data = Map("voluntaryRegistration" -> JsBoolean(false)),
    lastUpdated = Instant.now().minusSeconds(3600)
  )
  private val invalidData = Document(
    "id"          -> BsonString("invalid-1"),
    "data"        -> BsonDocument("voluntaryRegistration" -> BsonBoolean(true)),
    "lastUpdated" -> BsonString("2020-01-01T00:00:00Z"), // This is the invalid data type, is String not BsonDateTime
    "expiry"      -> BsonDateTime(Instant.now.plus(3600, ChronoUnit.SECONDS).toEpochMilli)
  )
  private val validDataSetDocs: Seq[Document] = convertDatedCacheMapsToDocuments(Seq(validData1, validData2))

  private def convertDatedCacheMapsToDocuments(datedCacheMaps: Seq[DatedCacheMap]): Seq[Document] =
    datedCacheMaps.map(dcm =>
      Document(
        "id" -> BsonString(dcm.id),
        "data" -> BsonDocument(dcm.data.map { case (key, jsValue: JsBoolean) =>
          key -> BsonBoolean(jsValue.value)
        }),
        "lastUpdated" -> BsonDateTime(dcm.lastUpdated.toEpochMilli), // This is the valid data type
        "expiry"      -> BsonDateTime(Instant.now.plus(3600, ChronoUnit.SECONDS).toEpochMilli)
      ))

  "deleteAllDataWithLastUpdatedStringType" must {
    "delete any data with a 'lastUpdated' index of type String" when {
      "database is empty" in new DeleteDataSetup {
        await(mongoCollection.drop().head())
        await(mongoCollection.countDocuments().head()) mustBe 0

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has only valid data" in new DeleteDataSetup {
        fillDatabaseWithData(validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 2

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }

      "database has only invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData))
        await(mongoCollection.countDocuments().head()) mustBe 1

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has a mix of valid and invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData) ++ validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 3

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }
    }
  }

  "deleteNDataWithLastUpdatedStringType" must {
    "delete data - up to config limit - with a 'lastUpdated' index of type String" when {
      "database is empty" when {
        "delete limit is zero" in new DeleteDataSetup {
          val limit = 0
          await(mongoCollection.drop().head())
          await(mongoCollection.countDocuments().head()) mustBe 0

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
        "delete limit exceeds document count" in new DeleteDataSetup {
          val limit = 2
          await(mongoCollection.drop().head())
          await(mongoCollection.countDocuments().head()) mustBe 0

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
      }

      "database has only valid data, no data should be deleted" in new DeleteDataSetup {
        val limit = 2
        fillDatabaseWithData(validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 2

        await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }

      "database has only invalid data" when {
        "limit is zero" in new DeleteDataSetup {
          val limit = 0
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 2
        }
        "limit is greater than invalid doc count" in new DeleteDataSetup {
          val limit = 5
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 2
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
        "limit is less than invalid doc count" in new DeleteDataSetup {
          val limit = 1
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 1
          await(testSessionRepository.collection.countDocuments().head()) mustBe 1
        }
      }

      "database has a mix of valid and invalid data" when {
        "limit is zero" in new DeleteDataSetup {
          val limit = 0
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 4
        }
        "limit is greater than invalid doc count" in new DeleteDataSetup {
          val limit = 5
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 2
          await(testSessionRepository.collection.countDocuments().head()) mustBe 2
        }
        "limit is less than invalid doc count" in new DeleteDataSetup {
          val limit = 1
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 1
          await(testSessionRepository.collection.countDocuments().head()) mustBe 3
        }
      }
    }
  }

}
