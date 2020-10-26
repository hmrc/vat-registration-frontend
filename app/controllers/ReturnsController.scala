/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import forms.{AccountingPeriodForm, ChargeExpectancyForm, MandatoryDateForm, ReturnFrequencyForm, VoluntaryDateForm}
import javax.inject.{Inject, Singleton}
import models.{CurrentProfile, DateSelection, Frequency, MonthYearModel, Returns}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{ApplicantDetailsService, ReturnsService, SessionProfile, TimeService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays.BankHolidaySet
import views.html.vatAccountingPeriod.{accounting_period_view => AccountingPeriodPage, return_frequency_view => ReturnFrequencyPage}
import views.html.{charge_expectancy_view => ChargeExpectancyPage, mandatory_start_date_incorp_view => MandatoryStartDateIncorpPage, start_date_view => VoluntaryStartDatePage}
import java.time.LocalDate
import java.util

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class ReturnsController @Inject()(mcc: MessagesControllerComponents,
                                  val keystoreConnector: KeystoreConnector,
                                  val authConnector: AuthClientConnector,
                                  val returnsService: ReturnsService,
                                  val applicantDetailsService: ApplicantDetailsService,
                                  val timeService: TimeService)
                                 (implicit val appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val chargeExpectancyPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.reclaimVatOnMostReturns match {
            case Some(chargeExpectancy) => Ok(ChargeExpectancyPage(ChargeExpectancyForm.form.fill(chargeExpectancy)))
            case None => Ok(ChargeExpectancyPage(ChargeExpectancyForm.form))
          }
        }
  }

  val submitChargeExpectancy: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ChargeExpectancyForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(ChargeExpectancyPage(errors))),
          success => {
            returnsService.saveReclaimVATOnMostReturns(success) flatMap { _ =>
              correctVatStartDatePage()
            }
          }
        )
  }

  val accountPeriodsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.staggerStart match {
            case Some(stagger) => Ok(AccountingPeriodPage(AccountingPeriodForm.form.fill(stagger)))
            case None => Ok(AccountingPeriodPage(AccountingPeriodForm.form))
          }
        }
  }

  private def correctVatStartDatePage()(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[Result] =
    returnsService.getThreshold map { voluntary =>
      if (voluntary) Redirect(routes.ReturnsController.voluntaryStartPage()) else Redirect(routes.ReturnsController.mandatoryStartPage())
    }

  val submitAccountPeriods: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        AccountingPeriodForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(AccountingPeriodPage(errors))),
          success => returnsService.saveStaggerStart(success) map { _ =>
            Redirect(controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
          }
        )
  }

  val returnsFrequencyPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.frequency match {
            case Some(frequency) => Ok(ReturnFrequencyPage(ReturnFrequencyForm.form.fill(frequency)))
            case None => Ok(ReturnFrequencyPage(ReturnFrequencyForm.form))
          }
        }
  }

  val submitReturnsFrequency: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ReturnFrequencyForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(ReturnFrequencyPage(errors))),
          success => returnsService.saveFrequency(success) map { _ =>
            if (success == Frequency.monthly) {
              Redirect(controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
            } else {
              Redirect(routes.ReturnsController.accountPeriodsPage())
            }
          }
        )
  }

  val voluntaryStartPage: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      returnsService.voluntaryStartPageViewModel() map { viewModel =>
        implicit val bhs: BankHolidaySet = timeService.bankHolidaySet
        val voluntaryDateForm = VoluntaryDateForm.form(timeService.getMinWorkingDayInFuture, timeService.addMonths(3))
        val filledForm = viewModel.form.fold(voluntaryDateForm) {
          case (dateSelection, date) => voluntaryDateForm.fill((dateSelection, date))
        }
        val dynamicDate = timeService.dynamicFutureDateExample()
        Ok(VoluntaryStartDatePage(filledForm, viewModel.ctActive, dynamicDate))
      }
  }

  val submitVoluntaryStart: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        implicit val bhs: BankHolidaySet = timeService.bankHolidaySet

        calculateEarliestStartDate.flatMap { esd =>
          val voluntaryDateForm = VoluntaryDateForm.form(esd, timeService.addMonths(3))
          returnsService.retrieveCTActiveDate flatMap { ctActiveDate =>
            voluntaryDateForm.bindFromRequest.fold(
              errors => {
                val dynamicDate = timeService.dynamicFutureDateExample()
                Future.successful(BadRequest(VoluntaryStartDatePage(errors, ctActiveDate, dynamicDate)))
              },
              success => returnsService.saveVoluntaryStartDate(success._1, success._2, ctActiveDate) map redirectBasedOnReclaim
            )
          }
        }
  }

  val mandatoryStartPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap(incorpDate =>
          returnsService.retrieveMandatoryDates map { dateModel =>
            val form = MandatoryDateForm.form(incorpDate, dateModel.calculatedDate)
            val dynamicDate = timeService.dynamicFutureDateExample()
            Ok(MandatoryStartDateIncorpPage(
              dateModel.selected.fold(form) { selection => form.fill((selection, dateModel.startDate)) },
              dateModel.calculatedDate.format(MonthYearModel.FORMAT_D_MMMM_Y),
              dynamicDate
            ))
          })
  }

  val submitMandatoryStart: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap(incorpDate =>
          returnsService.retrieveCalculatedStartDate flatMap { calcDate =>
            MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest.fold(
              errors => {
                val dynamicDate = timeService.dynamicFutureDateExample()
                Future.successful(BadRequest(MandatoryStartDateIncorpPage(errors, calcDate.format(MonthYearModel.FORMAT_D_MMMM_Y), dynamicDate)))
              },
              success => returnsService.saveVatStartDate(if (success._1 == DateSelection.calculated_date) Some(calcDate) else success._2) map redirectBasedOnReclaim
            )
          }
        )
  }

  private def redirectBasedOnReclaim(returns: Returns): Result = {
    if (returns.reclaimVatOnMostReturns.contains(true)) {
      Redirect(routes.ReturnsController.returnsFrequencyPage())
    } else {
      Redirect(routes.ReturnsController.accountPeriodsPage())
    }
  }

  private def calculateEarliestStartDate()(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[LocalDate] = for {
    dateOfIncorporationOption <- applicantDetailsService.getDateOfIncorporation
  } yield {
    val fourYearsAgo = timeService.minusYears(4)
    val dateOfIncorporation = dateOfIncorporationOption.getOrElse(fourYearsAgo)
    util.Collections.min(util.Arrays.asList(fourYearsAgo, dateOfIncorporation))
  }
}