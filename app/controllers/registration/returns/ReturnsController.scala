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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms._
import models._
import models.api.NETP
import models.api.returns.{Annual, Monthly, QuarterlyStagger}
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays.BankHolidaySet
import views.html.returns.{mandatory_start_date_incorp_view, return_frequency_view, start_date_incorp_view, AccountingPeriodView}

import java.time.LocalDate
import java.util
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsController @Inject()(val keystoreConnector: KeystoreConnector,
                                  val authConnector: AuthClientConnector,
                                  val returnsService: ReturnsService,
                                  val applicantDetailsService: ApplicantDetailsService,
                                  val timeService: TimeService,
                                  val vatRegistrationService: VatRegistrationService,
                                  mandatoryStartDateIncorpPage: mandatory_start_date_incorp_view,
                                  returnFrequencyPage: return_frequency_view,
                                  voluntaryStartDateIncorpPage: start_date_incorp_view,
                                  accountingPeriodPage: AccountingPeriodView
                                 )(implicit appConfig: FrontendAppConfig,
                                   val executionContext: ExecutionContext,
                                   baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val accountPeriodsPage: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.staggerStart match {
            case Some(stagger: QuarterlyStagger) => Ok(accountingPeriodPage(AccountingPeriodForm.form.fill(stagger)))
            case _ => Ok(accountingPeriodPage(AccountingPeriodForm.form))
          }
        }
  }

  val submitAccountPeriods: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        AccountingPeriodForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(accountingPeriodPage(errors))),
          success => returnsService.saveStaggerStart(success) flatMap { _ =>
            vatRegistrationService.partyType.map {
              case NETP => Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show()) //TODO Remove this page skip when etmp are ready to handle overseas
              case _ => Redirect(controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
            }
          }
        )
  }

  val returnsFrequencyPage: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          returns <- returnsService.getReturns
          showAAS <- returnsService.isEligibleForAAS
          showMonthly = returns.reclaimVatOnMostReturns.contains(true)
        } yield {
          if (showAAS || showMonthly) {
            returns.returnsFrequency match {
              case Some(frequency) => Ok(returnFrequencyPage(ReturnFrequencyForm.form.fill(frequency), showAAS, showMonthly))
              case None => Ok(returnFrequencyPage(ReturnFrequencyForm.form, showAAS, showMonthly))
            }
          } else {
            Redirect(routes.ReturnsController.accountPeriodsPage())
          }
        }
  }

  val submitReturnsFrequency: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ReturnFrequencyForm.form.bindFromRequest.fold(
          errors => for {
            returns <- returnsService.getReturns
            showAAS <- returnsService.isEligibleForAAS
            showMonthly = returns.reclaimVatOnMostReturns.contains(true)
          } yield {
            BadRequest(returnFrequencyPage(errors, showAAS, showMonthly))
          },
          success => returnsService.saveFrequency(success) map { _ =>
            success match {
              case Monthly => Redirect(controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
              case Annual => Redirect(routes.LastMonthOfAccountingYearController.show())
              case _ => Redirect(routes.ReturnsController.accountPeriodsPage())
            }
          }
        )
  }

  val voluntaryStartPage: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      implicit val bhs: BankHolidaySet = timeService.bankHolidaySet
      returnsService.getReturns.flatMap { returns =>
        calculateEarliestStartDate.map { incorpDate =>
          val exampleVatStartDate = timeService.dynamicFutureDateExample()

          val voluntaryDateForm = VoluntaryDateForm
            .form(timeService.getMinWorkingDayInFuture, timeService.addMonths(3))
          val filledForm = returns.startDate match {
            case Some(startDate) if incorpDate == startDate =>
              voluntaryDateForm.fill((DateSelection.company_registration_date, Some(startDate)))
            case Some(startDate) =>
              voluntaryDateForm.fill((DateSelection.specific_date, Some(startDate)))
            case _ =>
              voluntaryDateForm
          }

          val incorpDateAfter = incorpDate.isAfter(timeService.minusYears(4))

          Ok(voluntaryStartDateIncorpPage(filledForm, incorpDate.format(VoluntaryDateForm.dateFormat), incorpDateAfter, exampleVatStartDate))
        }
      }
  }

  val submitVoluntaryStart: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap { incorpDate =>
          val voluntaryDateForm = VoluntaryDateForm.form(incorpDate, timeService.addMonths(3))
          voluntaryDateForm.bindFromRequest.fold(
            errors => {
              val dynamicDate = timeService.dynamicFutureDateExample()
              val incorpDateAfter = incorpDate.isAfter(timeService.minusYears(4))

              Future.successful(BadRequest(voluntaryStartDateIncorpPage(errors, incorpDate.format(VoluntaryDateForm.dateFormat), incorpDateAfter, dynamicDate)))
            },
            success => returnsService.saveVoluntaryStartDate(success._1, success._2, incorpDate).map(_ =>
              Redirect(routes.ReturnsController.returnsFrequencyPage())
            )
          )
        }
  }

  val mandatoryStartPage: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap(incorpDate =>
          returnsService.retrieveMandatoryDates map { dateModel =>
            val form = MandatoryDateForm.form(incorpDate, dateModel.calculatedDate)
            Ok(mandatoryStartDateIncorpPage(
              dateModel.selected.fold(form) { selection => form.fill((selection, dateModel.startDate)) },
              dateModel.calculatedDate.format(MonthYearModel.FORMAT_D_MMMM_Y)
            ))
          })
  }

  val submitMandatoryStart: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap(incorpDate =>
          returnsService.retrieveCalculatedStartDate.flatMap { calcDate =>
            MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest.fold(
              errors => {
                Future.successful(BadRequest(mandatoryStartDateIncorpPage(errors, calcDate.format(MonthYearModel.FORMAT_D_MMMM_Y))))
              },
              {
                case (DateSelection.specific_date, Some(startDate)) =>
                  returnsService.saveVatStartDate(Some(startDate)).map(_ =>
                    Redirect(routes.ReturnsController.returnsFrequencyPage())
                  )
                case _ =>
                  returnsService.saveVatStartDate(None).map(_ =>
                    Redirect(routes.ReturnsController.returnsFrequencyPage())
                  )
              }
            )
          }
        )
  }

  private def calculateEarliestStartDate()(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[LocalDate] = for {
    dateOfIncorporationOption <- applicantDetailsService.getDateOfIncorporation
  } yield {
    val fourYearsAgo = timeService.minusYears(4)
    val dateOfIncorporation = dateOfIncorporationOption.getOrElse(fourYearsAgo)
    util.Collections.max(util.Arrays.asList(fourYearsAgo, dateOfIncorporation))
  }
}
