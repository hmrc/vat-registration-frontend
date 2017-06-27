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

import models._
import models.api.{VatFlatRateScheme, VatScheme}
import play.api.libs.json.Json

final case class JoinFrsView(selection: Boolean)

object JoinFrsView {
  implicit val format = Json.format[JoinFrsView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateSchemeAnswers) => group.joinFrs,
    updateF = (c: JoinFrsView, g: Option[S4LFlatRateSchemeAnswers]) =>
      g.getOrElse(S4LFlatRateSchemeAnswers()).copy(joinFrs = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
    vs.vatFlatRateSchemeAnswers.flatMap(_.joinFrs).map(JoinFrsView(_))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: JoinFrsView, g: VatFlatRateScheme) =>
    g.copy(joinFrs = Some(c.selection))
  }
}
