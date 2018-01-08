/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.forms

import java.time.LocalDate

import features.officer.models.view.{FormerNameDateView, FormerNameView, SecurityQuestionsView}
import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.FormValidation.{ErrorCode, inRange, missingBooleanFieldMapping, nonEmptyValidText, textMapping}
import models.DateModel
import play.api.data.Form
import play.api.data.Forms.{mapping, single, text}
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object CompletionCapacityForm {
  val NAME_ID: String = "completionCapacityRadio"

  val form = Form(
    single(
      NAME_ID -> textMapping()("completionCapacity")
    )
  )
}

object SecurityQuestionsForm {
  val NINO_REGEX = """^[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\d{2} ?\d{2} ?\d{2} ?[A-D]{1}$""".r

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val minDate: LocalDate = LocalDate.of(1900, 1, 1)
  val maxDate: LocalDate = LocalDate.now()

  val form = Form(
    mapping(
      "dob" -> {
        implicit val errorCodeDob: ErrorCode = "security.questions.dob"
        mapping(
          "day" -> text,
          "month" -> text,
          "year" -> text
        )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
      },
      "nino" -> {
        implicit val errorCodeNino: ErrorCode = "security.questions.nino"
        text.verifying(nonEmptyValidText(NINO_REGEX))
      }
    )(SecurityQuestionsView.bind)(SecurityQuestionsView.unbind)
  )
}

object FormerNameForm {
  val RADIO_YES_NO: String = "formerNameRadio"
  val INPUT_FORMER_NAME: String = "formerName"

  val FORMER_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$""".r

  implicit val errorCode: ErrorCode = INPUT_FORMER_NAME

  val form = Form(
    mapping(
      RADIO_YES_NO -> missingBooleanFieldMapping()("formerName"),
      INPUT_FORMER_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, "true"),
        text.verifying(nonEmptyValidText(FORMER_NAME_REGEX)("formerName.selected")))
    )(FormerNameView.apply)(FormerNameView.unapply)
  )
}

object FormerNameDateForm {
  implicit val errorCode: ErrorCode = "formerNameDate"

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val minDate: LocalDate = LocalDate.of(1900, 1, 1)
  val maxDate: LocalDate = LocalDate.now()

  val form = Form(
    mapping(
      "formerNameDate" -> mapping(
        "day" -> text,
        "month" -> text,
        "year" -> text
      )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
    )(FormerNameDateView.bind)(FormerNameDateView.unbind)
  )
}