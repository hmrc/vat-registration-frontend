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

package forms.vatTradingDetails.vatChoice

import java.time.LocalDate
import javax.inject.Inject

import common.Now
import forms.FormValidation.Dates.{nonEmptyPartialDateModel, validPartialDateModel}
import forms.FormValidation.{missingBooleanFieldMapping, onOrAfter}
import models.MonthYearModel
import models.view.vatTradingDetails.vatChoice.OverThresholdView
import play.api.data.Form
import play.api.data.Forms._
import services.DateService
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

class OverThresholdFormFactory @Inject()(dateService: DateService, today: Now[LocalDate]) {

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val RADIO_YES_NO = "overThresholdRadio"

  def form(): Form[OverThresholdView] = {

    val minDate: LocalDate = (dateService.addWorkingDays(today(), 2))

    implicit val specificErrorCode: String = "overThreshold.date"

    Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("overThreshold.selection"),
        "overThreshold" -> mandatoryIf(
          isEqual(RADIO_YES_NO, "true"),
          mapping(
            "month" -> text,
            "year" -> text
          )(MonthYearModel.apply)(MonthYearModel.unapply).verifying(
            nonEmptyPartialDateModel(validPartialDateModel(onOrAfter(minDate))))
        )
      )(OverThresholdView.bind)(OverThresholdView.unbind)
    )
  }

}