/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.ConfigConnector
import javax.inject.{Inject, Singleton}
import models.CurrentProfile
import models.api.VatScheme
import models.view.{Summary, SummaryFromQuestionAnswerJson}
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SummaryService @Inject()(val vrs: VatRegistrationService,
                               val applicantDetailsService: ApplicantDetailsService,
                               val sicAndComplianceService: SicAndComplianceService,
                               val flatRateService: FlatRateService,
                               val configConnector: ConfigConnector,
                               config: ServicesConfig)(implicit ec: ExecutionContext) {

  lazy val vatRegEFEUrl: String = config.getConfString("vat-registration-eligibility-frontend.uri", throw new Exception("vat-registration-eligibility-frontend.uri could not be found"))
  lazy val vatRegEFEQuestionUri: String = config.getConfString("vat-registration-eligibility-frontend.question", throw new Exception("vat-registration-eligibility-frontend.question could not be found"))

  private[services] def eligibilityCall(uri: String): Call = Call("GET", vatRegEFEUrl + vatRegEFEQuestionUri + s"?pageId=$uri")

  def getEligibilityDataSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
    vrs.getEligibilityData.map {
      _.validate[Summary](SummaryFromQuestionAnswerJson.summaryReads(eligibilityCall)).fold(
        errors => throw new Exception(s"[SummaryController][getEligibilitySummary] Json could not be parsed with errors: $errors with regId: ${profile.registrationId}"),
        identity
      )
    }
  }

  def getRegistrationSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
    for {
      applicant <- applicantDetailsService.getApplicantDetails
      sac <- sicAndComplianceService.getSicAndCompliance
      summary <- vrs.getVatScheme.map(scheme => registrationToSummary(scheme.copy(applicantDetails = Some(applicant), sicAndCompliance = Some(sac))))
    } yield summary
  }

  def registrationToSummary(vs: VatScheme): Summary = {
    Summary(Seq(
      viewmodels.SummaryCheckYourAnswersBuilder(vs,
        vs.applicantDetails.getOrElse(throw new IllegalStateException("Missing Applicant Details data to show summary")),
        vs.flatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))),
        vs.flatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
        vs.eligibilitySubmissionData.map(_.estimates), vs.eligibilitySubmissionData.map(_.threshold),
        vs.returns).section
    ))
  }
}