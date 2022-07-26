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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaxRepPage
import forms._
import models._
import models.api.vatapplication.{Annual, Monthly, QuarterlyStagger}
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.vatapplication.{AccountingPeriodView, mandatory_start_date_incorp_view, return_frequency_view, start_date_incorp_view}

import java.time.LocalDate
import java.util
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsController @Inject()(val sessionService: SessionService,
                                  val authConnector: AuthClientConnector,
                                  val vatApplicationService: VatApplicationService,
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
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.staggerStart match {
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
          success => vatApplicationService.saveVatApplication(success) flatMap { _ =>
            if (isEnabled(TaxRepPage)) {
              Future.successful(Redirect(controllers.vatapplication.routes.TaxRepController.show))
            } else {
              Future.successful(Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show))
            }
          }
        )
  }

  val returnsFrequencyPage: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          vatApplication <- vatApplicationService.getVatApplication
          showAAS <- vatApplicationService.isEligibleForAAS
          showMonthly = vatApplication.claimVatRefunds.contains(true)
        } yield {
          if (showAAS || showMonthly) {
            vatApplication.returnsFrequency match {
              case Some(frequency) => Ok(returnFrequencyPage(ReturnFrequencyForm.form.fill(frequency), showAAS, showMonthly))
              case None => Ok(returnFrequencyPage(ReturnFrequencyForm.form, showAAS, showMonthly))
            }
          } else {
            Redirect(routes.ReturnsController.accountPeriodsPage)
          }
        }
  }

  val submitReturnsFrequency: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ReturnFrequencyForm.form.bindFromRequest.fold(
          errors => for {
            vatApplication <- vatApplicationService.getVatApplication
            showAAS <- vatApplicationService.isEligibleForAAS
            showMonthly = vatApplication.claimVatRefunds.contains(true)
          } yield {
            BadRequest(returnFrequencyPage(errors, showAAS, showMonthly))
          },
          success => vatApplicationService.saveVatApplication(success) map { _ =>
            success match {
              case Monthly => if (isEnabled(TaxRepPage)) {
                Redirect(controllers.vatapplication.routes.TaxRepController.show)
              } else {
                Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show)
              }
              case Annual => Redirect(routes.LastMonthOfAccountingYearController.show)
              case _ => Redirect(routes.ReturnsController.accountPeriodsPage)
            }
          }
        )
  }

  val voluntaryStartPage: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      vatApplicationService.getVatApplication.flatMap { vatApplication =>
        calculateEarliestStartDate.map { incorpDate =>
          val exampleVatStartDate = timeService.dynamicFutureDateExample()

          val voluntaryDateForm = VoluntaryDateForm
            .form(timeService.getMinWorkingDayInFuture, timeService.addMonths(3))
          val filledForm = vatApplication.startDate match {
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
            success => vatApplicationService.saveVoluntaryStartDate(success._1, success._2, incorpDate).map(_ =>
              Redirect(routes.ReturnsController.returnsFrequencyPage)
            )
          )
        }
  }

  val mandatoryStartPage: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        calculateEarliestStartDate.flatMap(incorpDate =>
          vatApplicationService.retrieveMandatoryDates map { dateModel =>
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
          vatApplicationService.retrieveCalculatedStartDate.flatMap { calcDate =>
            MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest.fold(
              errors => {
                Future.successful(BadRequest(mandatoryStartDateIncorpPage(errors, calcDate.format(MonthYearModel.FORMAT_D_MMMM_Y))))
              },
              {
                case (DateSelection.specific_date, Some(startDate)) =>
                  vatApplicationService.saveVatApplication(startDate).map(_ =>
                    Redirect(routes.ReturnsController.returnsFrequencyPage)
                  )
                case (DateSelection.calculated_date, _) =>
                  vatApplicationService.saveVatApplication(calcDate).map(_ =>
                    Redirect(routes.ReturnsController.returnsFrequencyPage)
                  )
              }
            )
          }
        )
  }

  private def calculateEarliestStartDate()(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[LocalDate] = for {
    isGroupRegistration <- vatRegistrationService.getEligibilitySubmissionData.map(_.registrationReason.equals(GroupRegistration))
    dateOfIncorporationOption <-
      if (isGroupRegistration) {
        Future.successful(None)
      } else {
        applicantDetailsService.getDateOfIncorporation
      }
  } yield {
    val fourYearsAgo = timeService.minusYears(4)
    val dateOfIncorporation = dateOfIncorporationOption.getOrElse(fourYearsAgo)
    util.Collections.max(util.Arrays.asList(fourYearsAgo, dateOfIncorporation))
  }
}