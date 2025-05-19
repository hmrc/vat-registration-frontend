
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
