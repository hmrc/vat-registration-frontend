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

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatComplianceFinancial, VatScheme, VatSicAndCompliance}
import play.api.libs.json.Json

case class ActAsIntermediary(yesNo: Boolean)

object ActAsIntermediary {

  implicit val format = Json.format[ActAsIntermediary]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[ActAsIntermediary] { vs: VatScheme =>
    vs.vatSicAndCompliance.flatMap(_.financialCompliance).map { financialCompliance =>
      ActAsIntermediary(financialCompliance.actAsIntermediary)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: ActAsIntermediary, g: VatSicAndCompliance) =>
    /*TODO: This works as the user will always see question 1, but ideally we don't want to be defaulting
      the 1st question to false here in case they somehow manage to avoid it, may need looking into*/

    g.copy(financialCompliance = Some(VatComplianceFinancial(
                                      g.financialCompliance.map(_.adviceOrConsultancyOnly).getOrElse(false),
                                      actAsIntermediary = c.yesNo)))
  }

}


