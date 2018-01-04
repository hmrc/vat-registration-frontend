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

import java.time.LocalDate
import javax.inject.Inject

import cats.instances.FutureInstances
import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.financials.models.{DateSelection, Frequency}
import features.financials.views.html.vatAccountingPeriod.{accounting_period_view => AccountingPeriodPage, return_frequency_view => ReturnFrequencyPage}
import features.financials.views.html.{charge_expectancy_view => ChargeExpectancyPage}
import features.tradingDetails.views.html.vatChoice.{mandatory_start_date_confirmation => MandatoryStartDateConfirmationPage, mandatory_start_date_incorp_view => MandatoryStartDateIncorpPage, start_date_incorp_view => VoluntaryStartDateIncorpPage, start_date_view => VoluntaryStartDatePage}
import forms._
import models.CurrentProfile
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.{PrePopService, ReturnsService, SessionProfile}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class ReturnsController @Inject()(val keystoreConnector: KeystoreConnect,
                                  val authConnector: AuthConnector,
                                  val prePopService : PrePopService,
                                  val returnsService: ReturnsService,
                                  val messagesApi: MessagesApi) extends ReturnsCtrl {

}

trait ReturnsCtrl extends VatRegistrationControllerNoAux with SessionProfile with FutureInstances {

  val returnsService: ReturnsService
  val prePopService: PrePopService
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
                returnsService.saveReclaimVATOnMostReturns(success) map {
                  _ =>
                    if (success) {
                      Redirect(features.returns.routes.ReturnsController.returnsFrequencyPage())
                    } else {
                      returnsService.saveFrequency(Frequency.quarterly)
                      Redirect(features.returns.routes.ReturnsController.accountPeriodsPage())
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

  val submitAccountPeriods: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            AccountingPeriodForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(AccountingPeriodPage(errors))),
              success => {
                returnsService.saveStaggerStart(success) flatMap {
                  _ =>
                    returnsService.getEligibilityChoice map { voluntary =>
                      if (voluntary) {
                        Redirect(features.returns.routes.ReturnsController.voluntaryStartPage())
                      } else {
                        Redirect(features.returns.routes.ReturnsController.mandatoryStartPage())
                      }
                    }
                }
              }
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
              success => {
                returnsService.saveFrequency(success) flatMap {
                  _ =>
                    if (success == Frequency.monthly) {
                      returnsService.getEligibilityChoice map { voluntary =>
                        if (voluntary) {
                          Redirect(features.returns.routes.ReturnsController.voluntaryStartPage())
                        } else {
                          Redirect(features.returns.routes.ReturnsController.mandatoryStartPage())
                        }
                      }
                    } else {
                      Future.successful(Redirect(features.returns.routes.ReturnsController.accountPeriodsPage()))
                    }
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
            prePopService.getCTActiveDate.filter(LocalDate.now().plusMonths(3).isAfter).value flatMap {businessStart =>
              profile.incorporationDate match {
                case Some(incorpDate) => returnsService.getReturns map { returns =>
                  val vatDate = returns.start.flatMap(_.date)
                  val form = VoluntaryDateFormIncorp.form(incorpDate)
                  Ok(vatDate match {
                    case Some(startDate) => VoluntaryStartDateIncorpPage(form.fill((
                      if (incorpDate == startDate) DateSelection.company_registration_date else DateSelection.specific_date,
                      vatDate
                    )), incorpDate, businessStart)
                    case None => VoluntaryStartDateIncorpPage(form, incorpDate, businessStart)
                  })
                }
                case None => returnsService.getReturns map { returns =>
                  val form = VoluntaryDateForm.form
                  Ok(returns.start match {
                    case Some(start) => VoluntaryStartDatePage(form.fill((
                      (start.date, businessStart) match {
                        case (Some(sd), Some(bd)) if sd == bd => DateSelection.business_start_date
                        case (None, _) => DateSelection.company_registration_date
                        case _ => DateSelection.specific_date
                      }, returns.start.flatMap(_.date)
                    )), businessStart)
                    case None => VoluntaryStartDatePage(form, businessStart)
                  })
                }
              }
            }
          }
        }
  }

  private def saveStartDate(startDate : Option[LocalDate] = None)(implicit hc : HeaderCarrier, currentProfile : CurrentProfile): Future[Result] = {
    returnsService.saveVatStartDate(startDate) map {
      _ => Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
    }
  }

  val submitVoluntaryStart: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            prePopService.getCTActiveDate.filter(LocalDate.now().plusMonths(3).isAfter).value flatMap {ctAct =>
              profile.incorporationDate match {
                case Some(incorpDate) => VoluntaryDateFormIncorp.form(incorpDate).bindFromRequest.fold(
                  errors => Future.successful(BadRequest(VoluntaryStartDateIncorpPage(errors, incorpDate, ctAct))),
                  success => saveStartDate(success._1 match {
                    case DateSelection.company_registration_date => Some(incorpDate)
                    case DateSelection.business_start_date => Some(ctAct.get)
                    case _ => success._2
                  })
                )
                case None => VoluntaryDateForm.form.bindFromRequest.fold(
                  errors => Future.successful(BadRequest(VoluntaryStartDatePage(errors, ctAct))),
                  success => success._1 match {
                    case DateSelection.company_registration_date => saveStartDate()
                    case DateSelection.business_start_date => saveStartDate(Some(ctAct.get))
                    case _ => saveStartDate(success._2)
                  }
                )
              }
            }
          }
        }
  }

  val mandatoryStartPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            profile.incorporationDate match {
              case Some(incorpDate) =>
                for {
                  calcDateOpt <- returnsService.retrieveCalculatedStartDate
                  calcDate = calcDateOpt.get
                  returns <- returnsService.getReturns
                  vatDate = returns.start.flatMap(_.date)
                } yield {
                  val form = MandatoryDateForm.form(incorpDate, calcDate)
                  vatDate match {
                    case Some(startDate) => Ok(MandatoryStartDateIncorpPage(
                      form.fill((
                        if (startDate == calcDate) DateSelection.calculated_date else DateSelection.specific_date,
                        vatDate
                      )), calcDate.toString
                    ))
                    case None => Ok(MandatoryStartDateIncorpPage(form, calcDate.toString))
                  }
                }
              case None => Future.successful(Ok(MandatoryStartDateConfirmationPage()))
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
                returnsService.retrieveCalculatedStartDate flatMap {
                  case Some(calcDate) =>
                    MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest.fold(
                      errors => Future.successful(BadRequest(MandatoryStartDateIncorpPage(errors, calcDate.toString))),
                      success => saveStartDate(
                        if (success._1 == DateSelection.calculated_date) Some(calcDate) else success._2
                      )
                    )
                  case None => Future.successful(InternalServerError("[ReturnsController] [submitMandatoryStart] No calculated start date"))
                }
              case None => saveStartDate()
            }
          }
        }
  }
}
