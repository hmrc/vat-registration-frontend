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

package forms.vatapplication

import forms.FormValidation._
import models.DateSelection
import models.DateSelection.specific_date
import models.api.vatapplication._
import play.api.data.Forms.{single, tuple, _}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AccountingPeriodForm {

  private val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  private val ACCOUNTING_PERIOD = "value"

  val janStaggerKey = "jan"
  val febStaggerKey = "feb"
  val marStaggerKey = "mar"

  implicit def formatter: Formatter[QuarterlyStagger] = new Formatter[QuarterlyStagger] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QuarterlyStagger] = {
      data.get(key) match {
        case Some(`janStaggerKey`) => Right(JanuaryStagger)
        case Some(`febStaggerKey`) => Right(FebruaryStagger)
        case Some(`marStaggerKey`) => Right(MarchStagger)
        case _ => Left(Seq(FormError(key, accountingPeriodInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: QuarterlyStagger) =
      Map(key -> {
        value match {
          case JanuaryStagger => janStaggerKey
          case FebruaryStagger => febStaggerKey
          case MarchStagger => marStaggerKey
        }
      })
  }

  val form: Form[QuarterlyStagger] = Form(
    single(ACCOUNTING_PERIOD -> Forms.of[QuarterlyStagger])
  )
}