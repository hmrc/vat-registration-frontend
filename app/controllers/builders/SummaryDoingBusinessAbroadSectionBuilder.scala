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

package controllers.builders

import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryDoingBusinessAbroadSectionBuilder
(
  vatTradingDetails: Option[VatTradingDetails] = None
)
  extends SummarySectionBuilder {

  val euGoodsRow: SummaryRow = SummaryRow(
    "doingBusinessAbroad.eori.euGoods",
    vatTradingDetails.map(_.euTrading.selection).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())
  )

  val applyEoriRow: SummaryRow = SummaryRow(
    "doingBusinessAbroad.eori",
    vatTradingDetails.flatMap(_.euTrading.eoriApplication).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show())
  )

  val section: SummarySection = SummarySection(
    id = "doingBusinessAbroad",
    Seq(
      (euGoodsRow, true),
      (applyEoriRow, vatTradingDetails.exists(_.euTrading.selection))
    )
  )
}
