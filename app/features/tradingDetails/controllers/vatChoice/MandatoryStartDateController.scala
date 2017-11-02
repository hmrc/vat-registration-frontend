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

import connectors.KeystoreConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc._
import services.{S4LService, SessionProfile, VatRegistrationService}
import features.tradingDetails.views.html.vatChoice.{mandatory_start_date_confirmation, mandatory_start_date_incorp}
import forms.vatTradingDetails.vatChoice.MandatoryStartDateForm
import models.{CurrentProfile, MonthYearModel}
import models.api.{VatExpectedThresholdPostIncorp, VatThresholdPostIncorp}
import models.view.vatTradingDetails.vatChoice.StartDateView
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class MandatoryStartDateController @Inject()(ds: CommonPlayDependencies)
                                            (implicit vrs: VatRegistrationService, s4LService: S4LService)
  extends VatRegistrationController(ds) with SessionProfile {

  val keystoreConnector: KeystoreConnector = KeystoreConnector

  def calculateMandatoryStartDate(threshold : Option[VatThresholdPostIncorp], expectedThreshold : Option[VatExpectedThresholdPostIncorp]): Option[LocalDate] = {
    def calculatedCrossedThresholdDate(thresholdDate : LocalDate) = thresholdDate.withDayOfMonth(1).plusMonths(2)

    (threshold.flatMap(_.overThresholdDate), expectedThreshold.flatMap(_.expectedOverThresholdDate)) match {
      case (Some(td), Some(ed)) =>
        val calculatedThresholdDate = calculatedCrossedThresholdDate(td)
        Some(if (calculatedThresholdDate.isBefore(ed)) calculatedThresholdDate else ed)
      case (Some(td), None) => Some(calculatedCrossedThresholdDate(td))
      case (None, Some(ed)) => Some(ed)
      case _ => None
    }
  }

  def mandatoryStartDate(implicit profile : CurrentProfile, hc : HeaderCarrier) : Future[Option[LocalDate]] = {
    vrs.getVatScheme().map(
      _.vatServiceEligibility.flatMap(
        _.vatEligibilityChoice match {
          case Some(vec) => calculateMandatoryStartDate(vec.vatThresholdPostIncorp, vec.vatExpectedThresholdPostIncorp)
          case None => None
        }
      )
    )
  }

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          profile.incorporationDate.fold(Future.successful(Ok(mandatory_start_date_confirmation()))) {date =>
            mandatoryStartDate.flatMap(calculatedDate =>
              viewModel[StartDateView]().getOrElse(StartDateView()).map {vm =>
                val viewModel = (calculatedDate, vm.date) match {
                  case (Some(calcDate), Some(dt)) if calcDate != dt => vm.copy(dateType = StartDateView.SPECIFIC_DATE)
                  case _ => vm
                }
                val calculatedDateValue = calculatedDate.getOrElse(throw new RuntimeException(""))
                Ok(mandatory_start_date_incorp(
                  MandatoryStartDateForm.form(date, calculatedDateValue).fill(viewModel),
                  calculatedDateValue.format(MonthYearModel.FORMAT_D_MMMM_Y)
                ))
              }
            )
          }
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          profile.incorporationDate match {
            case Some(incorpDate) =>
              mandatoryStartDate.flatMap {calculatedDate =>
                val calculatedDateValue = calculatedDate.getOrElse(throw new RuntimeException(""))
                MandatoryStartDateForm.form(incorpDate, calculatedDateValue).bindFromRequest().fold(
                  badForm => BadRequest(mandatory_start_date_incorp(
                    badForm,
                    calculatedDateValue.format(MonthYearModel.FORMAT_D_MMMM_Y)
                  )).pure,
                  goodForm => save(goodForm).flatMap { _ =>
                    vrs.submitTradingDetails().map(_ =>
                      Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
                    )
                  }
                )
              }
            case None => vrs.submitTradingDetails().map { _ =>
              Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
            }
          }
        }
  }
}
