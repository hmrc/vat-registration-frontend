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

package forms

import forms.FormValidation.{nonEmptyDate, validDate, withinRange}
import play.api.data.Form
import play.api.data.Forms._
import services.TimeService
import uk.gov.hmrc.play.mappers.StopOnFirstFail

import java.time.LocalDate
import javax.inject.Inject

class VoluntaryStartDateNoChoiceForm @Inject()(timeService: TimeService) extends StartDateForm {

  lazy val date4YearsAgo = timeService.today.minusYears(4)
  lazy val now3MonthsLater = timeService.today.plusMonths(3)

  def apply(): Form[LocalDate] = Form(
    single(
      START_DATE -> tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(
            minDate = date4YearsAgo,
            maxDate = now3MonthsLater,
            beforeMinErr = dateRange,
            afterMaxErr = dateRange,
            args = List(date4YearsAgo.format(dateFormat), now3MonthsLater.format(dateFormat))
          )
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt, date._2.toInt, date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )

}
