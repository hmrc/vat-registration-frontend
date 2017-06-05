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
import models.{ApiModelTransformer, S4LVatLodgingOfficer, VMReads, ViewModelTransformer}
import play.api.libs.json.Json

case class PreviousAddressView(yesNo: Boolean, address: Option[ScrsAddress] = None)

object PreviousAddressView {

  implicit val format = Json.format[PreviousAddressView]

  implicit val vmReads = VMReads(
    readF = (group: S4LVatLodgingOfficer) => group.previousAddress,
    updateF = (c: PreviousAddressView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(previousAddress = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[PreviousAddressView] { vs: VatScheme =>
    vs.lodgingOfficer.map(_.currentOrPreviousAddress).map(p => PreviousAddressView(p.currentAddressThreeYears, p.previousAddress))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: PreviousAddressView, g: VatLodgingOfficer) =>
    g.copy(currentOrPreviousAddress = g.currentOrPreviousAddress.copy(currentAddressThreeYears = c.yesNo, previousAddress = c.address)
    )
  }

}


