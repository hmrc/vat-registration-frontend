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

package features.frs.controllers

import java.text.DecimalFormat
import java.util.MissingResourceException
import javax.inject.Inject

import config.AuthClientConnector
import connectors.{ConfigConnector, KeystoreConnect}
import controllers.BaseController
import features.frs.services.FlatRateService
import features.sicAndCompliance.services.SicAndComplianceService
import features.turnoverEstimates.TurnoverEstimatesService
import forms._
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent}
import services.{DateService, DateServiceImpl, SessionProfile}

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class FlatRateControllerImpl @Inject()(val messagesApi: MessagesApi,
                                       val flatRateService: FlatRateService,
                                       val turnoverEstimatesService: TurnoverEstimatesService,
                                       val authConnector: AuthClientConnector,
                                       val keystoreConnector: KeystoreConnect,
                                       val configConnector: ConfigConnector,
                                       val dateService: DateServiceImpl,
                                       val sicAndComplianceService: SicAndComplianceService) extends FlatRateController

trait FlatRateController extends BaseController with SessionProfile {

  val flatRateService: FlatRateService
  val turnoverEstimatesService: TurnoverEstimatesService
  val configConnector: ConfigConnector
  val sicAndComplianceService: SicAndComplianceService
  val dateService: DateService

  val registerForFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrs")("frs.registerFor")
  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("joinFrs")("frs.join")
  val yourFlatRateForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrsWithSector")("frs.registerForWithSector")
  val overBusinessGoodsForm = OverBusinessGoodsForm.form
  val startDateForm = FRSStartDateForm.form
  def overBusinessGoodsPercentForm(formPct : Long = 0) = new OverBusinessGoodsPercentForm {
    override val pct: Long = formPct
  }.form
  lazy val groupingBusinessTypesValues: ListMap[String, Seq[(String, String)]] = ListMap(configConnector.businessTypes.map { jsObj =>
    (
      (jsObj \ "groupLabel").as[String],
      (jsObj \ "categories").as[Seq[JsObject]].map(js => ((js \ "id").as[String], (js \ "businessType").as[String]))
    )
  }.sortBy(_._1):_*)

  lazy val businessTypeIds: Seq[String] = groupingBusinessTypesValues.values.toSeq.flatMap(radioValues => radioValues map Function.tupled((id, _) => id))



  def joinFrsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        turnoverEstimatesService.fetchTurnoverEstimates flatMap { res =>
          res.fold(Future.successful(InternalServerError(views.html.pages.error.restart()))) { turnoverEstimates =>
            if (turnoverEstimates.vatTaxable > 150000L) {
              Future.successful(Redirect(controllers.routes.SummaryController.show()))
            } else {
              flatRateService.getFlatRate map { flatRateScheme =>
                val form = flatRateScheme.joinFrs.fold(joinFrsForm)(v => joinFrsForm.fill(YesOrNoAnswer(v)))
                Ok(features.frs.views.html.frs_join(form))
              }
            }
          }
        }
      }
  }

  def submitJoinFRS: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        joinFrsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
          joiningFRS => flatRateService.saveJoiningFRS(joiningFRS.answer) map { _ =>
            if (joiningFRS.answer) {
              Redirect(features.frs.controllers.routes.FlatRateController.annualCostsInclusivePage())
            } else {
              Redirect(controllers.routes.SummaryController.show())
            }
          }
        )
      }
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
          view => flatRateService.saveOverBusinessGoods(view) map { _ =>
            if (!view) {
              Redirect(features.frs.controllers.routes.FlatRateController.registerForFrsPage())
            } else {
              Redirect(features.frs.controllers.routes.FlatRateController.estimateTotalSales())
            }
          }
        )
    }
  }

  def annualCostsLimitedPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          val viewForm = flatRateScheme.overBusinessGoodsPercent.fold(form)(form.fill)

          Ok(features.frs.views.html.annual_costs_limited(viewForm, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get)))
        }
      }
  }

  def submitAnnualCostsLimited: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          form.bindFromRequest().fold( formErr => {
            Future.successful(BadRequest(features.frs.views.html.annual_costs_limited(formErr, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))))},
            view => flatRateService.saveOverBusinessGoodsPercent(view) map { _ =>
              if (!view) {
                Redirect(features.frs.controllers.routes.FlatRateController.registerForFrsPage())
              } else {
                Redirect(features.frs.controllers.routes.FlatRateController.confirmSectorFrsPage())
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
          val (_, sector, pct) = view
          Ok(features.frs.views.html.frs_confirm_business_sector((sector, pct)))
        } recover {
          case _ : MissingResourceException => Redirect(features.frs.controllers.routes.FlatRateController.businessType(true))
        }
      }
  }

  def submitConfirmSectorFrs: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.saveConfirmSector map { _ =>
          Redirect(features.frs.controllers.routes.FlatRateController.yourFlatRatePage())
        }
      }
  }

  def frsStartDatePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getPrepopulatedStartDate map { prepop =>
          val (choOpt, date) = prepop
          val dynamicDate = dateService.dynamicFutureDateExample()
          val viewForm = choOpt.fold(startDateForm)(choice => startDateForm.fill((choice, date)))
          Ok(features.frs.views.html.frs_start_date(viewForm, dynamicDate))
        }
      }
  }

  def submitFrsStartDate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        startDateForm.bindFromRequest().fold(
          badForm => {
            val dynamicDate = dateService.dynamicFutureDateExample()
            Future.successful(BadRequest(features.frs.views.html.frs_start_date(badForm, dynamicDate)))
          },
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
              Redirect(features.frs.controllers.routes.FlatRateController.frsStartDatePage())
            } else {
              Redirect(controllers.routes.SummaryController.show())
            }
          }
        )
      }
  }

  def yourFlatRatePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = flatRateScheme.useThisRate match {
            case Some(useRate) => yourFlatRateForm.fill(YesOrNoAnswer(useRate))
            case None => yourFlatRateForm
          }
          flatRateService.retrieveSectorPercent map { sectorInfo =>
            val decimalFormat = new DecimalFormat("#0.##")
            val (_, sector, pct) = sectorInfo
            Ok(features.frs.views.html.frs_your_flat_rate(sector, decimalFormat.format(pct), form))
          }
        }
      }
  }

  def submitYourFlatRate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        yourFlatRateForm.bindFromRequest().fold(
          badForm => flatRateService.retrieveSectorPercent map { view =>
            val decimalFormat = new DecimalFormat("#0.##")
            val (_,sector, pct) = view
            BadRequest(features.frs.views.html.frs_your_flat_rate(sector, decimalFormat.format(pct), badForm))
          },
          view => for {
            _   <- flatRateService.saveUseFlatRate(view.answer)
          } yield if(view.answer) {
            Redirect(features.frs.controllers.routes.FlatRateController.frsStartDatePage())
          } else {
            Redirect(controllers.routes.SummaryController.show())
          }
        )
      }
  }

  def estimateTotalSales: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = flatRateScheme.estimateTotalSales.fold(EstimateTotalSalesForm.form)(v => EstimateTotalSalesForm.form.fill(v))
          Ok(features.frs.views.html.estimateTotalSales(form))
        }
      }
  }

  def submitEstimateTotalSales: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        EstimateTotalSalesForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.estimateTotalSales(badForm))),
          data => flatRateService.saveEstimateTotalSales(data) map {
            _ => Redirect(features.frs.controllers.routes.FlatRateController.annualCostsLimitedPage())
          }
        )
      }
  }

  def businessType(sendGA: Boolean = false): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          flatRateScheme   <- flatRateService.getFlatRate
          sicAndCompliance <- sicAndComplianceService.getSicAndCompliance
          form             = ChooseBusinessTypeForm.form(businessTypeIds)
          formFilled       = flatRateScheme.categoryOfBusiness.fold(form)(v => form.fill(v))
          sendGAText       = if (sendGA) sicAndCompliance.mainBusinessActivity.map(_.id) else None
        } yield {
          Ok(features.frs.views.html.chooseBusinessType(formFilled, groupingBusinessTypesValues, sendGAText))
        }
      }
  }

  def submitBusinessType: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        ChooseBusinessTypeForm.form(businessTypeIds).bindFromRequest().fold(
          badForm => Future.successful(BadRequest(features.frs.views.html.chooseBusinessType(badForm, groupingBusinessTypesValues))),
          data => flatRateService.saveBusinessType(data) map {
            _ => Redirect(features.frs.controllers.routes.FlatRateController.yourFlatRatePage())
          }
        )
      }
  }
}
