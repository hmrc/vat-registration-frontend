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

package services

import config.FrontendAppConfig
import models.CurrentProfile
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.Accordion
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.SummaryCheckYourAnswersBuilder

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Request

@Singleton
class SummaryService @Inject()(vatRegistrationService: VatRegistrationService,
                               summaryCheckYourAnswersBuilder: SummaryCheckYourAnswersBuilder
                              )(implicit ec: ExecutionContext) {

  def getSummaryData(implicit hc: HeaderCarrier, profile: CurrentProfile, messages: Messages, frontendAppConfig: FrontendAppConfig, request: Request[_]): Future[Accordion] = {
    for {
      vatScheme <- vatRegistrationService.getVatScheme
      accordion = summaryCheckYourAnswersBuilder.generateSummaryAccordion(vatScheme)(messages, frontendAppConfig, request)
    } yield accordion
  }

}