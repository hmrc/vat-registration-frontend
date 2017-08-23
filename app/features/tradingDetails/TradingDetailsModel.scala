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

package models.api {

  import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
  import play.api.libs.json._

  case class VatTradingDetails(
                                vatChoice: VatChoice,
                                tradingName: TradingName,
                                euTrading: VatEuTrading
                              ) {
    def registeringVoluntarily: Boolean = vatChoice.necessity == NECESSITY_VOLUNTARY
  }

  object VatTradingDetails {
    implicit val format: OFormat[VatTradingDetails] = Json.format[VatTradingDetails]
  }
}

package models {

  import common.ErrorUtil.fail
  import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
  import models.api._
  import models.view.vatTradingDetails.TradingNameView
  import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
  import models.view.vatTradingDetails.vatChoice._
  import models.view.vatTradingDetails.vatChoice.StartDateView.BUSINESS_START_DATE
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_YES
  import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
  import models.view.vatTradingDetails.vatEuTrading.EuGoods.EU_GOODS_YES
  import play.api.libs.json.{Json, OFormat}

  final case class S4LTradingDetails
  (
    taxableTurnover: Option[TaxableTurnover] = None,
    tradingName: Option[TradingNameView] = None,
    startDate: Option[StartDateView] = None,
    voluntaryRegistration: Option[VoluntaryRegistration] = None,
    voluntaryRegistrationReason: Option[VoluntaryRegistrationReason] = None,
    euGoods: Option[EuGoods] = None,
    applyEori: Option[ApplyEori] = None,
    overThreshold: Option[OverThresholdView] = None
  )

  object S4LTradingDetails {
    implicit val format: OFormat[S4LTradingDetails] = Json.format[S4LTradingDetails]
    implicit val tradingDetails: S4LKey[S4LTradingDetails] = S4LKey("VatTradingDetails")

    implicit val modelT = new S4LModelTransformer[S4LTradingDetails] {
      // map VatScheme to VatTradingDetails
      override def toS4LModel(vs: VatScheme): S4LTradingDetails =
        S4LTradingDetails(
          taxableTurnover = ApiModelTransformer[TaxableTurnover].toViewModel(vs),
          tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
          startDate = ApiModelTransformer[StartDateView].toViewModel(vs),
          voluntaryRegistration = ApiModelTransformer[VoluntaryRegistration].toViewModel(vs),
          voluntaryRegistrationReason = ApiModelTransformer[VoluntaryRegistrationReason].toViewModel(vs),
          euGoods = ApiModelTransformer[EuGoods].toViewModel(vs),
          applyEori = ApiModelTransformer[ApplyEori].toViewModel(vs),
          overThreshold = ApiModelTransformer[OverThresholdView].toViewModel(vs)
        )
    }

    def error = throw fail("VatTradingDetails")

    implicit val apiT = new S4LApiTransformer[S4LTradingDetails, VatTradingDetails] {
      // map S4LTradingDetails to VatTradingDetails
      override def toApi(c: S4LTradingDetails): VatTradingDetails =
        VatTradingDetails(
          vatChoice = VatChoice(
            necessity = c.voluntaryRegistration.map(vr =>
              if (vr.yesNo == REGISTER_YES) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY).getOrElse(NECESSITY_OBLIGATORY),
            vatStartDate = c.startDate.map(sd => VatStartDate(
              selection = sd.dateType,
              startDate = if (sd.dateType == BUSINESS_START_DATE) sd.ctActiveDate else sd.date)
            ).getOrElse(error),
            reason = c.voluntaryRegistrationReason.map(_.reason),
            vatThresholdPostIncorp = c.overThreshold.map(vtp =>
              VatThresholdPostIncorp(
                overThresholdSelection = vtp.selection,
                overThresholdDate = vtp.date
              )
            )),

          tradingName = c.tradingName.map(tnv =>
            TradingName(tnv.yesNo == TRADING_NAME_YES, tnv.tradingName)).getOrElse(error),

          euTrading = VatEuTrading(
            selection = c.euGoods.map(_.yesNo == EU_GOODS_YES).getOrElse(error),
            eoriApplication = c.applyEori.map(_.yesNo)
          )
        )
    }
  }

}
