/*
 * Copyright 2020 HM Revenue & Customs
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

import features.sicAndCompliance.models.SicAndCompliance
import models.view.{SummaryRow, SummarySection}

case class SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance: Option[SicAndCompliance] = None) extends SummarySectionBuilder {

  override val sectionId: String = "businessActivities"

  val sicAndComp = vatSicAndCompliance.fold(SicAndCompliance())(a => a)

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    s"$sectionId.businessDescription",
    sicAndComp.description.fold("app.common.no")(desc =>
      if(desc.description.isEmpty) "app.common.no" else desc.description),
    Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showBusinessActivityDescription())
  )

  val companyMainBusinessActivityRow: SummaryRow = SummaryRow(
    s"$sectionId.mainBusinessActivity",
      sicAndComp.mainBusinessActivity.fold("app.common.no")(main =>
        main.mainBusinessActivity collect {
          case sicCode if sicCode.description.nonEmpty => sicCode.code + " - " + sicCode.description
        } getOrElse "app.common.no"
      ),
    Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showMainBusinessActivity())
  )

  val confirmIndustryClassificationCodesRow: SummaryRow = SummaryRow(
    s"$sectionId.otherBusinessActivities",
    sicAndComp.otherBusinessActivities.fold(Seq("app.common.no"))(codes => codes.sicCodes.map(
      sicCode => sicCode.code + " - " + sicCode.description
    )),
    Some(features.sicAndCompliance.controllers.routes.SicAndComplianceController.returnToICL())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (companyBusinessDescriptionRow, true),
      (confirmIndustryClassificationCodesRow, true),
      (companyMainBusinessActivityRow, true)
    )
  )
}
