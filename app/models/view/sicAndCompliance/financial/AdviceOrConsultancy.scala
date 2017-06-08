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

import models.api._
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class AdviceOrConsultancy(yesNo: Boolean)

object AdviceOrConsultancy {
  
  implicit val format = Json.format[AdviceOrConsultancy]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.adviceOrConsultancy,
    updateF = (c: AdviceOrConsultancy, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(adviceOrConsultancy = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[AdviceOrConsultancy] { vs: VatScheme =>
    vs.vatSicAndCompliance.flatMap(_.financialCompliance).map { financialCompliance =>
      AdviceOrConsultancy(financialCompliance.adviceOrConsultancyOnly)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AdviceOrConsultancy, g: VatSicAndCompliance) =>
    /*TODO: This works as the user will always see question 2, but ideally we don't want to be defaulting
      the 2nd question to false here in case they somehow manage to avoid it, may need looking into*/

    g.copy(financialCompliance = Some(VatComplianceFinancial(
                                      adviceOrConsultancyOnly = c.yesNo,
                                      g.financialCompliance.map(_.actAsIntermediary).getOrElse(false))))
  }

}

