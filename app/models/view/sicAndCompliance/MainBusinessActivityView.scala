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

package models.view.sicAndCompliance

import models.api._
import models.{ApiModelTransformer, _}
import play.api.libs.json.Json

case class MainBusinessActivityView(id: String, mainBusinessActivity: Option[SicCode] = None)

object MainBusinessActivityView {

  def apply(cc: SicCode): MainBusinessActivityView = new MainBusinessActivityView(cc.id, Some(cc))

  implicit val format = Json.format[MainBusinessActivityView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.mainBusinessActivity,
    updateF = (c: MainBusinessActivityView, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(mainBusinessActivity = Some(c))
  )

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[MainBusinessActivityView] { vs: VatScheme =>
    vs.vatSicAndCompliance.map(cc =>
      MainBusinessActivityView(cc.mainBusinessActivity.id,
        Some(SicCode(cc.mainBusinessActivity.id, cc.mainBusinessActivity.description, cc.mainBusinessActivity.displayDetails))))
  }

}
