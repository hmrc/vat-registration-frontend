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

package services

import connectors.ConfigConnector
import controllers.builders._
import features.frs.services.FlatRateService
import features.officer.services.LodgingOfficerService
import features.sicAndCompliance.services.SicAndComplianceService
import javax.inject.Inject

import models.CurrentProfile
import models.api.VatScheme
import models.view.{Summary, SummaryFromQuestionAnswerJson}
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class SummaryServiceImpl @Inject()(val vrs: RegistrationService,
                                   val lodgingOfficerService: LodgingOfficerService,
                                   val sicAndComplianceService: SicAndComplianceService,
                                   val flatRateService: FlatRateService,
                                   val configConnector: ConfigConnector,
                                   config: ServicesConfig
                                  ) extends SummaryService {

  lazy val vatRegEFEUrl         = config.getConfString("vat-registration-eligibility-frontend.uri", throw new Exception("vat-registration-eligibility-frontend.uri could not be found"))
  lazy val vatRegEFEQuestionUri = config.getConfString("vat-registration-eligibility-frontend.question", throw new Exception("vat-registration-eligibility-frontend.question could not be found"))
}

trait SummaryService {
  val vrs: RegistrationService
  val lodgingOfficerService: LodgingOfficerService
  val sicAndComplianceService: SicAndComplianceService
  val flatRateService: FlatRateService
  val configConnector: ConfigConnector
  val vatRegEFEUrl: String
  val vatRegEFEQuestionUri: String

  private[services] def eligibilityCall(uri: String): Call = Call("GET", vatRegEFEUrl + vatRegEFEQuestionUri + s"?pageId=$uri")

  def getEligibilityDataSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] ={

    vrs.getEligibilityData.map{_.validate[Summary](SummaryFromQuestionAnswerJson.summaryReads(eligibilityCall)).fold(
      errors => throw new Exception(s"[SummaryController][getEligibilitySummary] Json could not be parsed with errors: $errors with regId: ${profile.registrationId}"),
      identity
    )}
  }

  def getRegistrationSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
    for {
      officer <- lodgingOfficerService.getLodgingOfficer
      sac     <- sicAndComplianceService.getSicAndCompliance
      summary <- vrs.getVatScheme.map(scheme => registrationToSummary(scheme.copy(lodgingOfficer = Some(officer), sicAndCompliance = Some(sac))))
    } yield summary
  }

  def registrationToSummary(vs: VatScheme)(implicit profile : CurrentProfile): Summary = {
    Summary(Seq(
      SummaryVatDetailsSectionBuilder(
        vs.tradingDetails,
        vs.threshold,
        vs.returns,
        profile.incorporationDate
      ).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryBusinessActivitiesSectionBuilder(vs.sicAndCompliance).section,
      SummaryComplianceSectionBuilder(vs.sicAndCompliance).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.businessContact).section,
      SummaryBusinessBankDetailsSectionBuilder(vs.bankAccount).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.returns).section,
      SummaryFrsSectionBuilder(
        vs.flatRateScheme,
        vs.flatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))),
        vs.flatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
        vs.turnOverEstimates
      ).section
    ))
  }
}