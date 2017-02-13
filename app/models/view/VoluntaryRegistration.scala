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

package models.view

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatChoice, VatScheme}
import models.api.VatChoice.{NECESSITY_VOLUNTARY, NECESSITY_OBLIGATORY}
import models.view.VoluntaryRegistration.REGISTER_YES
import play.api.libs.json.Json

case class VoluntaryRegistration(yesNo: String) extends ViewModelTransformer[VatChoice] {

  // Upserts (selectively converts) a View model object to its API model counterpart
  override def toApi(vatChoice: VatChoice): VatChoice =
    vatChoice.copy(necessity =
      if (REGISTER_YES == yesNo) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY)
}

object VoluntaryRegistration extends ApiModelTransformer[VoluntaryRegistration] {
  val REGISTER_YES = "REGISTER_YES"
  val REGISTER_NO = "REGISTER_NO"

  implicit val format = Json.format[VoluntaryRegistration]

  def empty: VoluntaryRegistration = VoluntaryRegistration("")

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): VoluntaryRegistration =
    vatScheme.vatChoice.necessity match {
      case NECESSITY_VOLUNTARY => VoluntaryRegistration(REGISTER_YES)
      case _ => VoluntaryRegistration.empty
    }
}