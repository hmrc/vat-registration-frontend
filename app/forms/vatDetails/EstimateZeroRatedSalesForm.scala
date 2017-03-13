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

package forms.vatDetails

import forms.validation.FormValidation._
import models.view.EstimateZeroRatedSales
import play.api.data.Form
import play.api.data.Forms._

object EstimateZeroRatedSalesForm {
  val ZERO_RATED_SALES_ESTIMATE: String = "zeroRatedSalesEstimate"

  val form = Form(
    mapping(
      ZERO_RATED_SALES_ESTIMATE -> text.verifying(mandatoryNumericText("estimate.zero.rated.sales")).
                                        transform(taxEstimateTextToLong, longToText).verifying(boundedLong("estimate.zero.rated.sales"))
    )(EstimateZeroRatedSales.apply)(EstimateZeroRatedSales.unapply)
  )

}
