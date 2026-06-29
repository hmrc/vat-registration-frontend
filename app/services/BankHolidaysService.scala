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

package services

import com.google.inject.{Inject, Singleton}
import connectors.BankHolidaysConnectorV2
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json._
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.mongo.cache.CacheItem
import utils.LoggingUtil
import utils.workingdays.{BankHoliday, BankHolidaySet}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class BankHolidaysService @Inject()(val bankHolidaysConnector: BankHolidaysConnectorV2)(implicit val ec: ExecutionContext) extends LoggingUtil {

  import services.BankHolidaysService._

  def fetchBankHolidaySet: Future[BankHolidaySet] =
    getBankHolidaysFromCache.flatMap {
      case Some(bankHolidaySet) =>
        logger.info("[BankHolidaysService][fetchBankHolidaySet] Retrieved bank holidays from Mongo cache.")
        Future.successful(bankHolidaySet)

      case None =>
        logger.info("[BankHolidaysService][fetchBankHolidaySet] Bank holiday cache empty. Fetching from API.")

        getBankHolidaySetFromApi
          .flatMap { bankHolidaySet =>
            saveBankHolidaysDataOnCache(bankHolidaySet)
              .map(_ => bankHolidaySet)
          }
          .recoverWith { case ex =>
            logger.error(
              "[BankHolidaysService][fetchBankHolidaySet] Failed to retrieve bank holidays from API and no cached data exists.",
              ex
            )
            Future.failed(ex)
          }
    }

  private def getBankHolidaySetFromApi: Future[BankHolidaySet] = {
    bankHolidaysConnector.getBankHolidaysFromApi.map { httpResponse =>
      if (httpResponse.status != OK) {

        logger.warn(s"[BankHolidaysService][getBankHolidaySetFromApi] Got http status ${httpResponse.status.toString} when calling the bank holiday API")
        throw UpstreamErrorResponse(
          message = "Could not retrieve bank holidays",
          statusCode = httpResponse.status,
          reportAs = INTERNAL_SERVER_ERROR
        )
      }
      httpResponse.json.validate[GDSBankHolidays] match {
        case JsSuccess(result, _) =>
          logger.info("[BankHolidaysService][getBankHolidaySetFromApi] Successfully retrieved bank holidays from the API.")
          toEnglandAndWalesBankHolidays(result)

        case JsError(errors) =>
          throw new IllegalStateException(
            s"[BankHolidaysService][getBankHolidaySetFromApi] Could not parse bank holiday response: $errors"
          )
      }
    }
  }


  private def toEnglandAndWalesBankHolidays(gdsBankHolidays: GDSBankHolidays): BankHolidaySet = {
    BankHolidaySet("england-and-wales",
      gdsBankHolidays.`england-and-wales`.events.map(event => BankHoliday(event.title,event.date)).toList)
  }

  private def saveBankHolidaysDataOnCache(data: BankHolidaySet): Future[CacheItem] =
    bankHolidaysConnector.saveBankHolidaysDataOnCache(data)(bankHolidaySetFormat)

  private def getBankHolidaysFromCache: Future[Option[BankHolidaySet]] =
    bankHolidaysConnector.getBankHolidaysFromCache()(bankHolidaySetFormat)

}


object BankHolidaysService extends LoggingUtil {

  final case class Event(title: String, date: LocalDate)

  final case class RegionalResult(events: Seq[Event])

  final case class GDSBankHolidays(
                                    `england-and-wales`: RegionalResult,
                                    scotland: RegionalResult,
                                    `northern-ireland`: RegionalResult
                                  )

  implicit val eventReads: Reads[Event] = {
    implicit val eventDateReads: Reads[LocalDate] = Reads.localDateReads(DateTimeFormatter.ISO_DATE)
    Json.reads[Event]
  }

  implicit val regionalResultReads: Reads[RegionalResult] = Json.reads[RegionalResult]

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val gdsBankHolidaysReads: Reads[GDSBankHolidays] = Json.reads[GDSBankHolidays]

  import play.api.libs.json._

  import java.time.LocalDate
  import java.time.format.DateTimeFormatter

  implicit val localDateFormat: Format[LocalDate] =
    Format(
      Reads.localDateReads(DateTimeFormatter.ISO_DATE),
      Writes.temporalWrites[LocalDate, DateTimeFormatter](DateTimeFormatter.ISO_DATE)
    )

  implicit val bankHolidayFormat: OFormat[BankHoliday] =
    Json.format[BankHoliday]

  implicit val bankHolidaySetFormat: OFormat[BankHolidaySet] =
    Json.format[BankHolidaySet]
}