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

package controllers.userJourney

import javax.inject.Inject

import controllers.utils.{SummaryCompanyDetailsSectionBuilder, SummaryVatDetailsSectionBuilder}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.api._
import models.view._
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryController @Inject()(implicit ds: CommonPlayDependencies, s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      for {
        _ <- vrs.submitVatScheme()
        summary <- getRegistrationSummary()
        _ <- s4LService.clear()
      } yield Ok(views.html.pages.summary(summary))
  }

  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    vrs.getVatScheme().map(registrationToSummary)

  def registrationToSummary(vatScheme: VatScheme): Summary = {
    val vatChoice = vatScheme.vatChoice.getOrElse(VatChoice())
    val vatTradingDetails = vatScheme.tradingDetails.getOrElse(VatTradingDetails())
    val vatFinancials = vatScheme.financials.getOrElse(VatFinancials.empty)

    val vatDetailsSectionBuilder = new SummaryVatDetailsSectionBuilder(vatChoice)
    val companyDetailsSectionBuilder = new SummaryCompanyDetailsSectionBuilder(vatTradingDetails, vatFinancials)

    Summary(Seq(
        vatDetailsSectionBuilder.section,
        companyDetailsSectionBuilder.section
      )
    )
  }
}
