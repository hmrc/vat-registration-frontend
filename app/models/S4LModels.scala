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
import models.api.VatEligibilityChoice._
import models.api._
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration._
import models.view.vatTradingDetails.vatChoice.{OverThresholdView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import play.api.libs.json._


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


case class S4LVatEligibilityChoice(taxableTurnover: Option[TaxableTurnover] = None,
                                   voluntaryRegistration: Option[VoluntaryRegistration] = None,
                                   voluntaryRegistrationReason: Option[VoluntaryRegistrationReason] = None,
                                   overThreshold: Option[OverThresholdView] = None)

object S4LVatEligibilityChoice {
  implicit val format: OFormat[S4LVatEligibilityChoice] = Json.format[S4LVatEligibilityChoice]
  implicit val tradingDetails: S4LKey[S4LVatEligibilityChoice] = S4LKey("VatChoice")

  implicit val modelT = new S4LModelTransformer[S4LVatEligibilityChoice] {
    // map VatScheme to VatTradingDetails
    override def toS4LModel(vs: VatScheme): S4LVatEligibilityChoice =
      S4LVatEligibilityChoice(
        taxableTurnover = ApiModelTransformer[TaxableTurnover].toViewModel(vs),
        voluntaryRegistration = ApiModelTransformer[VoluntaryRegistration].toViewModel(vs),
        overThreshold = ApiModelTransformer[OverThresholdView].toViewModel(vs),
        voluntaryRegistrationReason = ApiModelTransformer[VoluntaryRegistrationReason].toViewModel(vs)
      )
  }

  def error = throw fail("VatChoice")

  implicit val apiT = new S4LApiTransformer[S4LVatEligibilityChoice, VatEligibilityChoice] {
    // map S4LTradingDetails to VatTradingDetails
    override def toApi(c: S4LVatEligibilityChoice): VatEligibilityChoice =
      VatEligibilityChoice(
        necessity = c.voluntaryRegistration.map(vr =>
          if (vr.yesNo == REGISTER_YES) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY).getOrElse(NECESSITY_OBLIGATORY),
        reason = c.voluntaryRegistrationReason.map(_.reason),
        vatThresholdPostIncorp = c.overThreshold.map(vtp =>
          VatThresholdPostIncorp(
            overThresholdSelection = vtp.selection,
            overThresholdDate = vtp.date
          )
        )
      )
  }
}

final case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility] = None

)


object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]

  implicit val modelT = new S4LModelTransformer[S4LVatEligibility] {
    override def toS4LModel(vs: VatScheme): S4LVatEligibility =
      S4LVatEligibility(vatEligibility = ApiModelTransformer[VatServiceEligibility].toViewModel(vs))
  }

  def error = throw fail("VatServiceEligibility")

  implicit val apiT = new S4LApiTransformer[S4LVatEligibility, VatServiceEligibility] {
    override def toApi(c: S4LVatEligibility): VatServiceEligibility = {
      c.vatEligibility.map(ve => VatServiceEligibility(
        haveNino = ve.haveNino,
        doingBusinessAbroad = ve.doingBusinessAbroad,
        doAnyApplyToYou = ve.doAnyApplyToYou,
        applyingForAnyOf = ve.applyingForAnyOf,
        applyingForVatExemption = ve.applyingForVatExemption,
        companyWillDoAnyOf = ve.companyWillDoAnyOf,
        vatEligibilityChoice = ve.vatEligibilityChoice
        )
      ).getOrElse(error)
    }
  }
}
