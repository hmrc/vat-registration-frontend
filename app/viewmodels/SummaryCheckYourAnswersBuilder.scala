/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import featuretoggle.FeatureToggleSupport
import models.api._
import models.error.MissingAnswerException
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.{Accordion, Section}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import play.api.mvc.Request

@Singleton
class SummaryCheckYourAnswersBuilder @Inject()(eligibilitySummaryBuilder: EligibilitySummaryBuilder,
                                               grsSummaryBuilder: GrsSummaryBuilder,
                                               transactorDetailsSummaryBuilder: TransactorDetailsSummaryBuilder,
                                               applicantDetailsSummaryBuilder: ApplicantDetailsSummaryBuilder,
                                               aboutTheBusinessSummaryBuilder: AboutTheBusinessSummaryBuilder,
                                               otherBusinessInvolvementSummaryBuilder: OtherBusinessInvolvementSummaryBuilder,
                                               registrationDetailsSummaryBuilder: RegistrationDetailsSummaryBuilder) extends FeatureToggleSupport {

  def generateSummaryAccordion(vatScheme: VatScheme)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig, request: Request[_]): Accordion = {
    val isTransactor = vatScheme.eligibilitySubmissionData.exists(_.isTransactor)
    val missingRegReasonSection = "tasklist.eligibilty.regReason"

    val summaryMap = ListMap(

      messages(s"cya.heading.eligibility") -> eligibilitySummaryBuilder.build(vatScheme.eligibilityJson.getOrElse(throw MissingAnswerException(missingRegReasonSection)), vatScheme.registrationId),
      messages(s"cya.heading.transactor") -> transactorDetailsSummaryBuilder.build(vatScheme),
      messages(s"cya.heading.verifyBusiness") -> grsSummaryBuilder.build(vatScheme),
      (if (isTransactor) messages(s"cya.heading.applicant.transactor") else messages(s"cya.heading.applicant.self")) -> applicantDetailsSummaryBuilder.build(vatScheme),
      messages(s"cya.heading.aboutBusiness") -> aboutTheBusinessSummaryBuilder.build(vatScheme)
    ) ++ {
      val html = otherBusinessInvolvementSummaryBuilder.build(vatScheme)
      if (html != HtmlFormat.empty) ListMap(messages(s"cya.heading.otherBusinessInvolvements") -> html) else ListMap()
    } ++ ListMap(
      messages(s"cya.heading.vatRegDetails") -> registrationDetailsSummaryBuilder.build(vatScheme)
    )

    Accordion(items = summaryMap.map { case (heading, summarySection) =>
      Section(
        headingContent = Text(heading),
        content = HtmlContent(summarySection)
      )
    }.toSeq)
  }
}
