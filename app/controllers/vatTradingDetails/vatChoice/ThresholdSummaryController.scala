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

import javax.inject.Inject

import controllers.builders._
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.api._
import models.view._
import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration}
import models.{MonthYearModel, S4LTradingDetails}
import play.api.mvc._
import services.{CommonService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ThresholdSummaryController @Inject()(ds: CommonPlayDependencies)
                                          (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      thresholdSummary <- getThresholdSummary()
      dateOfIncorporation <- fetchDateOfIncorporation()
    } yield Ok(views.html.pages.vatTradingDetails.vatChoice.threshold_summary(
      thresholdSummary,
      MonthYearModel.FORMAT_DD_MMMM_Y.format(dateOfIncorporation))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    getVatThresholdPostIncorp().map(vatThresholdPostIncorp => vatThresholdPostIncorp match {
      case VatThresholdPostIncorp(true, _) =>
        save(VoluntaryRegistration(REGISTER_NO))
        save(StartDateView(COMPANY_REGISTRATION_DATE))
        Redirect(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
      case _ => Redirect(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show())
    })
  })

  def getThresholdSummary()(implicit hc: HeaderCarrier): Future[Summary] = {
    for {
      vatThresholdPostIncorp <- getVatThresholdPostIncorp()
    } yield thresholdToSummary(vatThresholdPostIncorp)
  }

  def thresholdToSummary(vatThresholdPostIncorp: VatThresholdPostIncorp): Summary = {
    Summary(Seq(
      SummaryVatThresholdBuilder(Some(vatThresholdPostIncorp)).section
    ))
  }

  def getVatThresholdPostIncorp()(implicit hc: HeaderCarrier): Future[VatThresholdPostIncorp] = {
    for {
      vatTradingDetails <- s4LService.fetchAndGet[S4LTradingDetails]()
      overThreshold <- vatTradingDetails.flatMap(_.overThreshold).pure
    } yield overThreshold.map(o => VatThresholdPostIncorp(o.selection, o.date)).get
  }

}
