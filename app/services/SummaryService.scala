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

package services

import config.FrontendAppConfig
import models.CurrentProfile
import models.view.EligibilityJsonParser
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.SummaryCheckYourAnswersBuilder

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SummaryService @Inject()(vatRegistrationService: VatRegistrationService,
                               summaryCheckYourAnswersBuilder: SummaryCheckYourAnswersBuilder
                              )(implicit ec: ExecutionContext,
                                appConfig: FrontendAppConfig) {

  private[services] def eligibilityCall(uri: String): String = s"${appConfig.eligibilityUrl}${appConfig.eligibilityQuestionUrl}?pageId=$uri"

  def getEligibilityDataSummary(implicit hc: HeaderCarrier, profile: CurrentProfile, messages: Messages): Future[SummaryList] = {
    vatRegistrationService.getEligibilityData.map { json =>
      json.validate[SummaryList](EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, messages("app.common.change"))).fold(
        errors => throw new Exception(s"[SummaryController][getEligibilitySummary] Json could not be parsed with errors: $errors with regId: ${profile.registrationId}"),
        identity
      )
    }
  }

  def getRegistrationSummary(implicit hc: HeaderCarrier, profile: CurrentProfile, messages: Messages): Future[SummaryList] = {
    vatRegistrationService.getVatScheme.map { vatScheme =>
      summaryCheckYourAnswersBuilder.generateSummaryList(vatScheme, messages)
    }
  }

}