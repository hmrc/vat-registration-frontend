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

package features.returns

import javax.inject.Inject

import cats.instances.FutureInstances
import cats.syntax.{ApplicativeSyntax, FlatMapSyntax}
import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.returns.routes.ReturnsController
import features.returns.views.html.vatAccountingPeriod.{accounting_period_view => AccountingPeriodPage, return_frequency_view => ReturnFrequencyPage}
import features.returns.views.html.{charge_expectancy_view => ChargeExpectancyPage, mandatory_start_date_confirmation => MandatoryStartDateConfirmationPage, mandatory_start_date_incorp_view => MandatoryStartDateIncorpPage, start_date_incorp_view => VoluntaryStartDateIncorpPage, start_date_view => VoluntaryStartDatePage}
import forms._
import models.{CurrentProfile, MonthYearModel}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class ReturnsControllerImpl @Inject()(val keystoreConnector: KeystoreConnect,
                                      val authConnector: AuthConnector,
                                      val returnsService: ReturnsService,
                                      val messagesApi: MessagesApi) extends ReturnsController {

}

trait ReturnsController extends VatRegistrationControllerNoAux with SessionProfile with FutureInstances
  with FlatMapSyntax with ApplicativeSyntax {

  val returnsService: ReturnsService
  val authConnector: AuthConnector
  val keystoreConnector: KeystoreConnect

  val chargeExpectancyPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            returnsService.getReturns map { returns =>
              returns.reclaimVatOnMostReturns match {
                case Some(chargeExpectancy) => Ok(ChargeExpectancyPage(ChargeExpectancyForm.form.fill(chargeExpectancy)))
                case None => Ok(ChargeExpectancyPage(ChargeExpectancyForm.form))
              }
            }
          }
        }
  }

  val submitChargeExpectancy: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            ChargeExpectancyForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(ChargeExpectancyPage(errors))),
              success => {
                returnsService.saveReclaimVATOnMostReturns(success) map { _ =>
                  if (success) {
                    Redirect(ReturnsController.returnsFrequencyPage())
                  } else {
                    Redirect(ReturnsController.accountPeriodsPage())
                  }
                }
              }
            )
          }
        }
  }

  val accountPeriodsPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            returnsService.getReturns map { returns =>
              returns.staggerStart match {
                case Some(stagger) => Ok(AccountingPeriodPage(AccountingPeriodForm.form.fill(stagger)))
                case None => Ok(AccountingPeriodPage(AccountingPeriodForm.form))
              }
            }
          }
        }
  }

  private def vatStartDatePage()(implicit hc : HeaderCarrier, currentProfile : CurrentProfile) =
    returnsService.getEligibilityChoice.ifM(
      ifTrue = ReturnsController.voluntaryStartPage().pure,
      ifFalse = ReturnsController.mandatoryStartPage().pure
    ) map Redirect

  val submitAccountPeriods: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            AccountingPeriodForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(AccountingPeriodPage(errors))),
              success => returnsService.saveStaggerStart(success) flatMap {_ => vatStartDatePage()}
            )
          }
        }
  }

  val returnsFrequencyPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            returnsService.getReturns map { returns =>
              returns.frequency match {
                case Some(frequency) => Ok(ReturnFrequencyPage(ReturnFrequencyForm.form.fill(frequency)))
                case None => Ok(ReturnFrequencyPage(ReturnFrequencyForm.form))
              }
            }
          }
        }
  }

  val submitReturnsFrequency: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            ReturnFrequencyForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(ReturnFrequencyPage(errors))),
              success => returnsService.saveFrequency(success) flatMap { _ =>
                if (success == Frequency.monthly) {
                  vatStartDatePage()
                } else {
                  Future.successful(Redirect(ReturnsController.accountPeriodsPage()))
                }
              }
            )
          }
        }
  }

  val voluntaryStartPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            returnsService.voluntaryStartPageViewModel(profile.incorporationDate) map { viewModel =>
              val form = profile.incorporationDate.fold(VoluntaryDateForm.form)(VoluntaryDateFormIncorp.form)
              val filledForm = viewModel.form.fold(form)(data => form.fill(
                (data._1, data._2)
              ))
              Ok(profile.incorporationDate match {
                case Some(incorp) => VoluntaryStartDateIncorpPage(filledForm, incorp, viewModel.ctActive)
                case None => VoluntaryStartDatePage(filledForm, viewModel.ctActive)
              })
            }
          }
        }
  }

  val submitVoluntaryStart: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            val form = profile.incorporationDate.fold(VoluntaryDateForm.form)(VoluntaryDateFormIncorp.form)
            returnsService.retrieveCTActiveDate flatMap { ctActiveDate =>
              form.bindFromRequest.fold(
                errors => BadRequest(profile.incorporationDate match {
                  case Some(incorpDate) => VoluntaryStartDateIncorpPage(errors, incorpDate, ctActiveDate)
                  case None => VoluntaryStartDatePage(errors, ctActiveDate)
                }).pure,
                success => returnsService.saveVoluntaryStartDate(success._1, success._2, profile.incorporationDate, ctActiveDate) map { _ =>
                  Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
                }
              )
            }
          }
        }
  }

  val mandatoryStartPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            profile.incorporationDate.fold(Future.successful(Ok(MandatoryStartDateConfirmationPage()))) {
              incorpDate =>
                returnsService.retrieveMandatoryDates map { dateModel =>
                  val form = MandatoryDateForm.form(incorpDate, dateModel.calculatedDate)
                  Ok(MandatoryStartDateIncorpPage(
                    dateModel.selected.fold(form) { selection =>
                      form.fill((selection, dateModel.startDate))
                    },
                    dateModel.calculatedDate.format(MonthYearModel.FORMAT_D_MMMM_Y)
                  ))
              }
            }
          }
        }
  }

  val submitMandatoryStart: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            profile.incorporationDate match {
              case Some(incorpDate) =>
                returnsService.retrieveCalculatedStartDate flatMap { calcDate =>
                    MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest.fold(
                      errors => Future.successful(BadRequest(MandatoryStartDateIncorpPage(errors, calcDate.format(MonthYearModel.FORMAT_D_MMMM_Y)))),
                      success => returnsService.saveVatStartDate(if (success._1 == DateSelection.calculated_date) Some(calcDate) else success._2) map {
                        _ => Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
                      }
                    )
                }
              case None => returnsService.saveVatStartDate(None) map {
                _ => Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
              }
            }
          }
        }
  }
}
