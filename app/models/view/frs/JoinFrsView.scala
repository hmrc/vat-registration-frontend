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
import models.api.VatScheme
import play.api.libs.json.Json

final case class JoinFrsView(selection: Boolean)

object JoinFrsView {
  implicit val format = Json.format[JoinFrsView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (_: S4LFlatRateScheme).joinFrs,
    updateF = (c: JoinFrsView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(joinFrs = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
    vs.vatFlatRateScheme.map(_.joinFrs).map(JoinFrsView(_))
  }

}
