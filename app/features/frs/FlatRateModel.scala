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

package models.api {

  import java.time.LocalDate

  import play.api.libs.json._

  case class VatFlatRateScheme(joinFrs: Boolean = false,
                               annualCostsInclusive: Option[String] = None,
                               annualCostsLimited: Option[String] = None,
                               doYouWantToUseThisRate: Option[Boolean] = None, //mandatory if joinFrs yes
                               whenDoYouWantToJoinFrs: Option[String] = None,
                               startDate: Option[LocalDate] = None, // if doYouWantToUseThisRate is true this is mandatory
                               categoryOfBusiness: Option[String] = None,
                               percentage: Option[BigDecimal] = None)


  object VatFlatRateScheme {
    implicit val format: OFormat[VatFlatRateScheme] = Json.format[VatFlatRateScheme]
  }
}

package models {

  import models.api.{VatFlatRateScheme, VatScheme}
  import play.api.libs.json.{Json, OFormat}

  final case class S4LFlatRateScheme
  (
    joinFrs: Option[JoinFrsView] = None,
    annualCostsInclusive: Option[AnnualCostsInclusiveView] = None,
    annualCostsLimited: Option[AnnualCostsLimitedView] = None,
    registerForFrs: Option[RegisterForFrsView] = None,
    frsStartDate: Option[FrsStartDateView] = None,
    categoryOfBusiness: Option[BusinessSectorView] = None
  )

  object S4LFlatRateScheme {
    implicit val format: OFormat[S4LFlatRateScheme] = Json.format[S4LFlatRateScheme]
    implicit val vatFlatRateScheme: S4LKey[S4LFlatRateScheme] = S4LKey("VatFlatRateScheme")

    implicit val modelT = new S4LModelTransformer[S4LFlatRateScheme] {
      override def toS4LModel(vs: VatScheme): S4LFlatRateScheme =
        S4LFlatRateScheme(
          joinFrs = ApiModelTransformer[JoinFrsView].toViewModel(vs),
          annualCostsInclusive = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
          annualCostsLimited = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
          registerForFrs = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
          frsStartDate = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
          categoryOfBusiness = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
        )
    }

    implicit val apiT = new S4LApiTransformer[S4LFlatRateScheme, VatFlatRateScheme] {
      override def toApi(c: S4LFlatRateScheme): VatFlatRateScheme =
        VatFlatRateScheme(
          joinFrs = c.joinFrs.exists(_.selection),
          annualCostsInclusive = c.annualCostsInclusive.map(_.selection),
          annualCostsLimited = c.annualCostsLimited.map(_.selection),
          doYouWantToUseThisRate = c.registerForFrs.map(_.selection),
          whenDoYouWantToJoinFrs = c.frsStartDate.map(_.dateType),
          startDate = c.frsStartDate.flatMap(_.date),
          categoryOfBusiness = c.categoryOfBusiness.map(_.businessSector),
          percentage = c.categoryOfBusiness.map(_.flatRatePercentage)
        )
    }
  }
}
