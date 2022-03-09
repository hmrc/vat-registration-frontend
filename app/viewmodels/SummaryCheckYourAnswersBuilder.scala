/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels

import featureswitch.core.config.FeatureSwitching
import models.api._
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.{Accordion, Section}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap

@Singleton
class SummaryCheckYourAnswersBuilder @Inject()(govukSummaryList: GovukSummaryList,
                                               eligibilitySummaryBuilder: EligibilitySummaryBuilder,
                                               grsSummaryBuilder: GrsSummaryBuilder,
                                               transactorDetailsSummaryBuilder: TransactorDetailsSummaryBuilder,
                                               applicantDetailsSummaryBuilder: ApplicantDetailsSummaryBuilder,
                                               aboutTheBusinessSummaryBuilder: AboutTheBusinessSummaryBuilder,
                                               registrationDetailsSummaryBuilder: RegistrationDetailsSummaryBuilder) extends FeatureSwitching {

  def generateSummaryAccordion(vatScheme: VatScheme, eligibilityJson: JsValue)(implicit messages: Messages): Accordion = {
    val isTransactor = vatScheme.eligibilitySubmissionData.exists(_.isTransactor)

    val summaryMap = ListMap(
      messages(s"cya.heading.eligibility") -> eligibilitySummaryBuilder.build(eligibilityJson, vatScheme.id),
      messages(s"cya.heading.transactor") -> transactorDetailsSummaryBuilder.build(vatScheme),
      messages(s"cya.heading.verifyBusiness") -> grsSummaryBuilder.build(vatScheme),
      (if (isTransactor) messages(s"cya.heading.applicant.transactor") else messages(s"cya.heading.applicant.self")) -> applicantDetailsSummaryBuilder.build(vatScheme),
      messages(s"cya.heading.aboutBusiness") -> aboutTheBusinessSummaryBuilder.build(vatScheme),
      messages(s"cya.heading.vatRegDetails") -> registrationDetailsSummaryBuilder.build(vatScheme)
    ).filter { case (_, summaryList) => summaryList.rows.nonEmpty }

    Accordion(items = summaryMap.map { case (heading, summarySection) =>
      Section(
        headingContent = Text(heading),
        content = HtmlContent(govukSummaryList(summarySection))
      )
    }.toSeq)
  }
}
