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

package models.view.sicAndCompliance.financial

import models.api.{VatScheme, VatSicAndCompliance}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class AdditionalNonSecuritiesWork(yesNo: Boolean)

object AdditionalNonSecuritiesWork {

  implicit val format = Json.format[AdditionalNonSecuritiesWork]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[AdditionalNonSecuritiesWork] { vs: VatScheme =>
    for {
      vsc <- vs.vatSicAndCompliance
      fc <- vsc.financialCompliance
      answ <- fc.additionalNonSecuritiesWork
    } yield AdditionalNonSecuritiesWork(answ)
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AdditionalNonSecuritiesWork, g: VatSicAndCompliance) =>
    g.copy(financialCompliance = g.financialCompliance.map(_.copy(additionalNonSecuritiesWork = Some(c.yesNo))))
  }

}



