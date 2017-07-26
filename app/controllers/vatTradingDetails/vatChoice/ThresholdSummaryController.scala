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

package controllers.vatTradingDetails.vatChoice

import java.time.LocalDate
import javax.inject.Inject

import controllers.builders._
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.{DateModel, MonthYearModel, S4LTradingDetails}
import models.api._
import models.view._
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ThresholdSummaryController @Inject()(ds: CommonPlayDependencies)
                                          (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  val dateOfIncorporation = LocalDate.now().minusMonths(2) //fixed date until we can get the DOI from II

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      thresholdSummary <- getThresholdSummary()
    } yield Ok(views.html.pages.vatTradingDetails.vatChoice.threshold_summary(
      thresholdSummary,
      MonthYearModel.FORMAT_DD_MMMM_Y.format(dateOfIncorporation))))

  def getThresholdSummary()(implicit hc: HeaderCarrier): Future[Summary] = {
    getVatThresholdPostIncorp.map(thresholdToSummary)
  }

  def thresholdToSummary(vatThresholdPostIncorp: VatThresholdPostIncorp): Summary = {
    Summary(Seq(
      SummaryVatThresholdBuilder(Some(vatThresholdPostIncorp), dateOfIncorporation).section
    ))
  }

  def getVatThresholdPostIncorp()(implicit hc: HeaderCarrier): Future[VatThresholdPostIncorp] = {
    for {
      vatTradingDetails <- s4LService.fetchAndGet[S4LTradingDetails]()
      overThreshold <- vatTradingDetails.flatMap(_.overThreshold).pure
    } yield overThreshold.map(o => VatThresholdPostIncorp(o.selection, o.date)).get
  }

}
