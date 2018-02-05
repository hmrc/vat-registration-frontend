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

package controllers

import java.text.DecimalFormat
import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnect
import forms._
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import frs.AnnualCosts
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionProfile}

import scala.concurrent.Future

class FlatRateControllerImpl @Inject()(val messagesApi: MessagesApi,
                                       val flatRateService: FlatRateService,
                                       val authConnector: AuthClientConnector,
                                       val keystoreConnector: KeystoreConnect) extends FlatRateController

trait FlatRateController extends BaseController with SessionProfile {

  val flatRateService: FlatRateService

  val registerForFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrs")("frs.registerFor")
  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("joinFrs")("frs.join")
  val yourFlatRateForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrsWithSector")("frs.registerForWithSector")
  val overBusinessGoodsForm = OverBusinessGoodsForm.form
  val startDateForm = FRSStartDateForm.form
  def overBusinessGoodsPercentForm(formPct : Long = 0L) = new OverBusinessGoodsPercentForm {
    override val pct: Long = formPct
  }.form

  def joinFrsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      flatRateService.getFlatRate map { flatRateScheme =>
        val form = flatRateScheme.joinFrs match {
          case Some(joinFrs) => joinFrsForm.fill(YesOrNoAnswer(joinFrs))
          case None => joinFrsForm
        }
        Ok(features.frs.views.html.frs_join(form))
      }
  }

  def submitJoinFRS: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      joinFrsForm.bindFromRequest().fold(
        badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
        joiningFRS => flatRateService.saveJoiningFRS(joiningFRS.answer) map { _ =>
          if (joiningFRS.answer) {
            Redirect(controllers.routes.FlatRateController.annualCostsInclusivePage())
          } else {
            Redirect(controllers.routes.SummaryController.show())
          }
        }
      )
  }

  def annualCostsInclusivePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate map { flatRateScheme =>
          val viewForm = flatRateScheme.overBusinessGoods.fold(overBusinessGoodsForm)(overBusinessGoodsForm.fill)
          Ok(features.frs.views.html.annual_costs_inclusive(viewForm))
        }
      }
  }

  def submitAnnualInclusiveCosts: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        overBusinessGoodsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.annual_costs_inclusive(badForm))),
          view => if (view == AnnualCosts.DoesNotSpend) {
            flatRateService.saveOverAnnualCosts(view) flatMap { _ =>
              flatRateService.isOverLimitedCostTraderThreshold map {
                case true => Redirect(controllers.routes.FlatRateController.annualCostsLimitedPage())
                case false => Redirect(controllers.routes.FlatRateController.confirmSectorFrsPage())
              }
            }
          } else {
            flatRateService.saveOverAnnualCosts(view) map { _ =>
              Redirect(controllers.routes.FlatRateController.registerForFrsPage())
            }
          }
        )
      }
  }

  def annualCostsLimitedPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          flatRateScheme <- flatRateService.getFlatRate
          flatRateSchemeThreshold <- flatRateService.getFlatRateSchemeThreshold
        } yield {
          val viewForm = flatRateScheme.overBusinessGoodsPercent.fold(overBusinessGoodsPercentForm())(overBusinessGoodsPercentForm().fill)
          Ok(features.frs.views.html.annual_costs_limited(viewForm, flatRateSchemeThreshold))
        }
      }
  }

  def submitAnnualCostsLimited: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRateSchemeThreshold flatMap { frsThreshold =>
          overBusinessGoodsPercentForm(frsThreshold).bindFromRequest().fold(
            errors => Future.successful(BadRequest(features.frs.views.html.annual_costs_limited(errors, frsThreshold))),
            view => flatRateService.saveOverAnnualCostsPercent(view) map { _ =>
              if (view == AnnualCosts.DoesNotSpend) {
                Redirect(controllers.routes.FlatRateController.confirmSectorFrsPage())
              } else {
                Redirect(controllers.routes.FlatRateController.registerForFrsPage())
              }
            }
          )
        }
      }
  }

  def confirmSectorFrsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.retrieveSectorPercent map { view =>
          Ok(features.frs.views.html.frs_confirm_business_sector(view))
        }
      }
  }

  def submitConfirmSectorFrs: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.saveConfirmSector map { _ =>
          Redirect(controllers.routes.FlatRateController.yourFlatRatePage())
        }
      }
  }

  def frsStartDatePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getPrepopulatedStartDate map { prepop =>
          val (choOpt, date) = prepop
          val viewForm = choOpt.foldLeft(startDateForm)((form, choice) => form.fill((choice, date)))
          Ok(features.frs.views.html.frs_start_date(viewForm))
        }
      }
  }

  def submitFrsStartDate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        startDateForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.frs_start_date(badForm))),
          view => {
            val (choice, date) = view
            flatRateService.saveStartDate(choice, date) map { _ =>
              Redirect(controllers.routes.SummaryController.show())
            }
          }
        )
      }
  }

  def registerForFrsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = flatRateScheme.useThisRate match {
            case Some(useRate) => registerForFrsForm.fill(YesOrNoAnswer(useRate))
            case None => registerForFrsForm
          }
          Ok(features.frs.views.html.frs_register_for(form))
        }
      }
  }

  def submitRegisterForFrs: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        registerForFrsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.frs_register_for(badForm))),
          view => flatRateService.saveRegister(view.answer) map { _ =>
            if (view.answer) {
              Redirect(controllers.routes.FlatRateController.frsStartDatePage())
            } else {
              Redirect(controllers.routes.SummaryController.show())
            }
          }
        )
      }
  }

  def yourFlatRatePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      flatRateService.getFlatRate flatMap { flatRateScheme =>
        val form = flatRateScheme.useThisRate match {
          case Some(useRate) => yourFlatRateForm.fill(YesOrNoAnswer(useRate))
          case None => yourFlatRateForm
        }
        flatRateService.retrieveSectorPercent map { sectorInfo =>
          val decimalFormat = new DecimalFormat("#0.##")
          val (sector, pct) = sectorInfo
          Ok(features.frs.views.html.frs_your_flat_rate(sector, decimalFormat.format(pct), form))
        }
      }
  }

  def submitYourFlatRate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        yourFlatRateForm.bindFromRequest().fold(
          badForm => flatRateService.retrieveSectorPercent map { view =>
            val decimalFormat = new DecimalFormat("#0.##")
            val (sector, pct) = view
            BadRequest(features.frs.views.html.frs_your_flat_rate(sector, decimalFormat.format(pct), badForm))
          },
          view => for {
            _   <- flatRateService.saveUseFlatRate(view.answer)
          } yield if(view.answer) {
            Redirect(controllers.routes.FlatRateController.frsStartDatePage())
          } else {
            Redirect(controllers.routes.SummaryController.show())
          }
        )
      }
  }
}
