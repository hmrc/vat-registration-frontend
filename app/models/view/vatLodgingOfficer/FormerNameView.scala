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

import models.api._
import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class FormerNameView(
                           yesNo: Boolean,
                           formerName: Option[String] = None
                         )

object FormerNameView {

  def apply(changeOfName: ChangeOfName): FormerNameView = new FormerNameView(changeOfName.nameHasChanged, changeOfName.formerName.map(_.formerName))

  implicit val format = Json.format[FormerNameView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatLodgingOfficer) => group.formerName,
    updateF = (c: FormerNameView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(formerName = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[FormerNameView] { vs: VatScheme =>
    vs.lodgingOfficer.map(_.changeOfName).map(changeOfName => FormerNameView(changeOfName.nameHasChanged, changeOfName.formerName.map(_.formerName)))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: FormerNameView, g: VatLodgingOfficer) =>
    g.copy(changeOfName =
      ChangeOfName(c.yesNo,
        if (c.yesNo) Some(FormerName(c.formerName.getOrElse(""))) else None))
  }

}


