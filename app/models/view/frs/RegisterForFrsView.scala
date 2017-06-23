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

package models.view.frs

import models.api.{FlatRateScheme, VatScheme}
import models.{ApiModelTransformer, S4LFlatRateScheme, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

final case class RegisterForFrsView(selection: Boolean)


object RegisterForFrsView {
  implicit val format = Json.format[RegisterForFrsView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateScheme) => group.registerForFrs,
    updateF = (c: RegisterForFrsView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(registerForFrs = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[RegisterForFrsView] { (vs: VatScheme) =>
    vs.flatRateScheme.map(frs => RegisterForFrsView(frs.registerForFrs))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: RegisterForFrsView, g: FlatRateScheme) =>
    g.copy(registerForFrs = c.selection)
  }
}
