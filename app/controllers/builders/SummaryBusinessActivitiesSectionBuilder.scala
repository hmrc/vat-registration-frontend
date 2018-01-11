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

package controllers.builders

import models.api._
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryBusinessActivitiesSectionBuilder(vatSicAndCompliance: Option[VatSicAndCompliance] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "businessActivities"

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    s"$sectionId.businessDescription",
    vatSicAndCompliance.collect {
      case VatSicAndCompliance(description, _, _) if StringUtils.isNotBlank(description) => description
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
  )

  val companyMainBusinessActivityRow: SummaryRow = SummaryRow(
    s"$sectionId.mainBusinessActivity",
    vatSicAndCompliance.collect {
      case VatSicAndCompliance(_, _, mainBusinessActivity)
        if StringUtils.isNotBlank(mainBusinessActivity.description) => mainBusinessActivity.description
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.routes.MainBusinessActivityController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (companyBusinessDescriptionRow, true),
      (companyMainBusinessActivityRow, true)
    )
  )
}
