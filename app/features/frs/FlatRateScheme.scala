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

package models

import java.text.DecimalFormat
import java.time.LocalDate

import models.api.{VatFlatRateScheme, VatScheme}
import play.api.libs.json.Json

import scala.util.Try

case class AnnualCostsInclusiveView(selection: String)

object AnnualCostsInclusiveView {

  val YES = "yes"
  val YES_WITHIN_12_MONTHS = "yesWithin12months"
  val NO = "no"

  val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

  implicit val format = Json.format[AnnualCostsInclusiveView]

  implicit val modelTransformer = ApiModelTransformer[AnnualCostsInclusiveView] { vs: VatScheme =>
    vs.vatFlatRateScheme.flatMap(_.annualCostsInclusive).collect {
      case choice@(YES | YES_WITHIN_12_MONTHS | NO) => AnnualCostsInclusiveView(choice)
    }
  }
}

case class AnnualCostsLimitedView(selection: String)

object AnnualCostsLimitedView {

  val YES = "yes"
  val YES_WITHIN_12_MONTHS = "yesWithin12months"
  val NO = "no"

  val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

  implicit val format = Json.format[AnnualCostsLimitedView]

  implicit val modelTransformer = ApiModelTransformer[AnnualCostsLimitedView] { vs: VatScheme =>
    vs.vatFlatRateScheme.flatMap(_.annualCostsLimited).collect {
      case YES => AnnualCostsLimitedView(YES)
      case YES_WITHIN_12_MONTHS => AnnualCostsLimitedView(YES_WITHIN_12_MONTHS)
      case NO => AnnualCostsLimitedView(NO)
    }
  }
}

final case class BusinessSectorView(businessSector: String, flatRatePercentage: BigDecimal) {
  val flatRatePercentageFormatted = BusinessSectorView.decimalFormat.format(flatRatePercentage)
}

object BusinessSectorView {

  val decimalFormat = new DecimalFormat("#0.##")

  implicit val format = Json.format[BusinessSectorView]

  implicit val modelTransformer = ApiModelTransformer[BusinessSectorView] { (vs: VatScheme) =>
    for {
      frs <- vs.vatFlatRateScheme
      sector <- frs.categoryOfBusiness
      percentage <- frs.percentage
    } yield BusinessSectorView(sector, percentage)
  }
}

case class FrsStartDateView(dateType: String = "", date: Option[LocalDate] = None)

object FrsStartDateView {

  def bind(dateType: String, dateModel: Option[DateModel]): FrsStartDateView =
    FrsStartDateView(dateType, dateModel.flatMap(_.toLocalDate))

  def unbind(frsStartDate: FrsStartDateView): Option[(String, Option[DateModel])] =
    Try {
      frsStartDate.date.fold((frsStartDate.dateType, Option.empty[DateModel])) {
        d => (frsStartDate.dateType, Some(DateModel.fromLocalDate(d)))
      }
    }.toOption

  val VAT_REGISTRATION_DATE = "VAT_REGISTRATION_DATE"
  val DIFFERENT_DATE = "DIFFERENT_DATE"

  val validSelection: String => Boolean = Seq(VAT_REGISTRATION_DATE, DIFFERENT_DATE).contains

  implicit val format = Json.format[FrsStartDateView]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[FrsStartDateView] { vs: VatScheme =>
    vs.vatFlatRateScheme.collect {
      case VatFlatRateScheme(_, _, _, _, Some(dateType), date, _, _) => FrsStartDateView(dateType, date) //TODO review if such collect necessary
    }
  }
}

case class JoinFrsView(selection: Boolean)

object JoinFrsView {
  implicit val format = Json.format[JoinFrsView]

  implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
    vs.vatFlatRateScheme.map(_.joinFrs).map(JoinFrsView(_))
  }
}

final case class RegisterForFrsView(selection: Boolean)

object RegisterForFrsView {
  implicit val format = Json.format[RegisterForFrsView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateScheme) => group.registerForFrs,
    updateF = (c: RegisterForFrsView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(registerForFrs = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[RegisterForFrsView] { (vs: VatScheme) =>
    vs.vatFlatRateScheme.flatMap(answers => answers.doYouWantToUseThisRate.map(RegisterForFrsView.apply))
  }
}
