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
import connectors.BankHolidaysConnector
import play.api.http.Status.OK
import play.api.libs.json._
import repositories.BankHolidayRepository
import utils.LoggingUtil
import utils.workingdays.{BankHoliday, BankHolidaySet}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class BankHolidaysService @Inject()(val bankHolidaysConnector: BankHolidaysConnector,
                                    val bankHolidayRepository: BankHolidayRepository)(implicit val ec: ExecutionContext) extends LoggingUtil {

  import services.BankHolidaysService._

  private val emptyBankHolidaySet = BankHolidaySet("england-and-wales", Nil)
  private val pagerDutyAlertKeyForNoBankHolidays = "NO_BANK_HOLIDAYS"

  def fetchBankHolidaySet: Future[BankHolidaySet] =
    bankHolidayRepository.getBankHolidaysFromCache.flatMap {

      case Some(cached) =>
        logger.info("[BankHolidaysService][fetchBankHolidaySet] Retrieved bank holidays from Mongo cache.")
        Future.successful(cached)

      case None =>
        logger.info("[BankHolidaysService][fetchBankHolidaySet] Bank holiday cache empty. Fetching from API.")

        getBankHolidaySetFromApi.flatMap {

          case Right(bankHolidaySet) =>
            logger.info("[BankHolidaysService][fetchBankHolidaySet] Successfully retrieved bank holidays from API.")

            bankHolidayRepository.saveBankHolidaysDataOnCache(bankHolidaySet)
              .map(_ => bankHolidaySet)

          case Left(error) =>
            logger.error(s"[BankHolidaysService][fetchBankHolidaySet] - ${error.message}")
            // pagerduty alert NO_BANK_HOLIDAYS
            logger.error(s"$pagerDutyAlertKeyForNoBankHolidays - Failed to retrieve bank holidays from API and no cached data exists.")
            Future.successful(emptyBankHolidaySet)
        }
    }


  private def getBankHolidaySetFromApi: Future[Either[BankHolidayError, BankHolidaySet]] = {
    bankHolidaysConnector
      .getBankHolidaysFromApi
      .map { response =>
        response.status match {

          case OK =>
            response.json.validate[GDSBankHolidays] match {

              case JsSuccess(result, _) =>
                logger.info("[BankHolidaysService][getBankHolidaySetFromApi] Successfully retrieved bank holidays from the API.")
                Right(toEnglandAndWalesBankHolidays(result))

              case e: JsError =>
                logger.error( s"[BankHolidaysService][getBankHolidaySetFromApi] Could not parse bank holiday response: $e")
                Left(JsonParseError(e))
            }

          case status =>
            logger.error(s"[BankHolidaysService][getBankHolidaySetFromApi] Got http status ${status.toString} when calling the bank holiday API")
            Left(ApiError(status))
        }
      }
      .recover {
        case ex =>
          logger.error(s"[BankHolidaysService][getBankHolidaySetFromApi] UnexpectedError when calling the bank holiday API - $ex")
          Left(UnexpectedError(ex))
      }
  }


  private def toEnglandAndWalesBankHolidays(gdsBankHolidays: GDSBankHolidays): BankHolidaySet = {
    BankHolidaySet("england-and-wales",
      gdsBankHolidays.`england-and-wales`.events.map(event => BankHoliday(event.title,event.date)).toList)
  }

}

sealed trait BankHolidayError {
  def message: String
}

case class ApiError(status: Int) extends BankHolidayError {
  override val message =
    s"Bank holiday API returned HTTP $status"
}

case class JsonParseError(errors: JsError) extends BankHolidayError {
  override val message =
    s"Failed to parse bank holiday response: $errors"
}

case class UnexpectedError(ex: Throwable) extends BankHolidayError {
  override val message =
    s"Unexpected error calling bank holiday API: ${ex.getMessage}"
}

object BankHolidaysService extends LoggingUtil {

  final case class Event(title: String, date: LocalDate)

  final case class RegionalResult(events: Seq[Event])

  final case class GDSBankHolidays(
                                    `england-and-wales`: RegionalResult,
                                    scotland: RegionalResult,
                                    `northern-ireland`: RegionalResult
                                  )

  implicit val localDateFormat: Format[LocalDate] =
    Format(
      Reads.localDateReads(DateTimeFormatter.ISO_DATE),
      Writes.temporalWrites[LocalDate, DateTimeFormatter](DateTimeFormatter.ISO_DATE)
    )

  implicit val eventReads: Reads[Event] = {
    Json.reads[Event]
  }

  implicit val regionalResultReads: Reads[RegionalResult] = Json.reads[RegionalResult]

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val gdsBankHolidaysReads: Reads[GDSBankHolidays] = Json.reads[GDSBankHolidays]

  import play.api.libs.json._
  implicit val bankHolidayFormat: OFormat[BankHoliday] =
    Json.format[BankHoliday]

  implicit val bankHolidaySetFormat: OFormat[BankHolidaySet] =
    Json.format[BankHolidaySet]
}