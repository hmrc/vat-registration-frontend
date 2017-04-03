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

package models.view.sicAndCompliance.labour

import models.api.{VatScheme, VatSicAndCompliance}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class Workers(numberOfWorkers: Int)

object Workers {

  implicit val format: OFormat[Workers] = Json.format[Workers]

  implicit val modelTransformer = ApiModelTransformer[Workers] { (vs: VatScheme) =>
    //TODO: Return a proper value once the frontend API model is created for labour compliance
    None
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: Workers, g: VatSicAndCompliance) => {
    //TODO: Return proper logical group once the frontend API model is created for labour compliance
    g
  }
  }

}

