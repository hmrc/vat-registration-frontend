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

import models.api.EligibilityQuestion._
import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryServiceEligibilitySectionBuilder
(
  vatServiceEligibility: Option[VatServiceEligibility] = None
)
  extends SummarySectionBuilder {

  override val sectionId: String = "serviceCriteria"

  val haveNinoRow: SummaryRow = yesNoRow(
    "nino",
    vatServiceEligibility.flatMap(_.haveNino),
    controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(HaveNinoQuestion.name)
  )

  val doingBusinessAbroadRow: SummaryRow = yesNoRow(
    "businessAbroad",
    vatServiceEligibility.flatMap(_.doingBusinessAbroad),
    controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name)
  )

  val doAnyApplyToYouRow: SummaryRow = yesNoRow(
    "doAnyApplyToYou",
    vatServiceEligibility.flatMap(_.doAnyApplyToYou),
    controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoAnyApplyToYouQuestion.name)
  )

  val applyingForAnyOfRow: SummaryRow = yesNoRow(
    "applyingForAnyOf",
    vatServiceEligibility.flatMap(_.applyingForAnyOf),
    controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(ApplyingForAnyOfQuestion.name)
  )

  val companyWillDoAnyOfRow: SummaryRow = yesNoRow(
    "companyWillDoAnyOf",
    vatServiceEligibility.flatMap(_.companyWillDoAnyOf),
    controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(CompanyWillDoAnyOfQuestion.name)
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (haveNinoRow, vatServiceEligibility.exists(_.haveNino.isDefined)),
      (doingBusinessAbroadRow, vatServiceEligibility.exists(_.doingBusinessAbroad.isDefined)),
      (doAnyApplyToYouRow, vatServiceEligibility.exists(_.doAnyApplyToYou.isDefined)),
      (applyingForAnyOfRow, vatServiceEligibility.exists(_.applyingForAnyOf.isDefined)),
      (companyWillDoAnyOfRow, vatServiceEligibility.exists(_.companyWillDoAnyOf.isDefined))
    ),
    vatServiceEligibility.isDefined
  )
}
