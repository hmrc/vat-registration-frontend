/*
 * Copyright 2018 HM Revenue & Customs
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

package models

import common.ErrorUtil.fail
import models.api._
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import play.api.libs.json.{Json, OFormat}


trait S4LModelTransformer[C] {
  // Returns an S4L container for a logical group given a VatScheme
  def toS4LModel(vatScheme: VatScheme): C
}

trait S4LApiTransformer[C, API] {
  // Returns logical group API model given an S4L container
  def toApi(container: C): API
}

final case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails] = None,
  ppob: Option[PpobView] = None
)

object S4LVatContact {
  implicit val format: OFormat[S4LVatContact] = Json.format[S4LVatContact]

  implicit val modelT = new S4LModelTransformer[S4LVatContact] {
    override def toS4LModel(vs: VatScheme): S4LVatContact =
      S4LVatContact(
        businessContactDetails = ApiModelTransformer[BusinessContactDetails].toViewModel(vs),
        ppob = ApiModelTransformer[PpobView].toViewModel(vs)
      )
  }

  def error = throw fail("VatContact")

  implicit val apiT = new S4LApiTransformer[S4LVatContact, VatContact] {
    override def toApi(c: S4LVatContact): VatContact =
      VatContact(
        digitalContact = VatDigitalContact(
                          email = c.businessContactDetails.map(_.email).getOrElse(error),
                          tel = c.businessContactDetails.flatMap(_.daytimePhone),
                          mobile = c.businessContactDetails.flatMap(_.mobile)),
        website = c.businessContactDetails.flatMap(_.website),
        ppob = c.ppob.flatMap(_.address).getOrElse(error)
      )
  }
}
