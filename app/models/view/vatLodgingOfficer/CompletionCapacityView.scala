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

package models.view.vatLodgingOfficer

import models.api.{CompletionCapacity, VatScheme}
import models.{ApiModelTransformer, _}
import play.api.libs.json.Json

case class CompletionCapacityView(id: String, completionCapacity: Option[CompletionCapacity] = None)

object CompletionCapacityView {

  def apply(cc: CompletionCapacity): CompletionCapacityView = new CompletionCapacityView(cc.name.id, Some(cc))

  implicit val format = Json.format[CompletionCapacityView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatLodgingOfficer) => group.completionCapacity,
    updateF = (c: CompletionCapacityView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(completionCapacity = Some(c))
  )

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[CompletionCapacityView] { vs: VatScheme =>
    vs.lodgingOfficer.map(cc => CompletionCapacityView(cc.name.id, Some(CompletionCapacity(cc.name, cc.role))))
  }

}
