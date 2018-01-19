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

import javax.inject.Inject

import connectors.KeystoreConnect
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import forms.{AnnualCostsInclusiveForm, AnnualCostsLimitedFormFactory, FrsStartDateFormFactory}
import models._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class FlatRateControllerImpl @Inject()(val messagesApi: MessagesApi,
                                       val flatRateService: FlatRateService,
                                       val authConnector: AuthConnector,
                                       val keystoreConnector: KeystoreConnect,
                                       val frsStartDateFormFactory: FrsStartDateFormFactory) extends FlatRateController {

  override val startDateForm: Form[FrsStartDateView] = frsStartDateFormFactory.form()
}

trait FlatRateController extends VatRegistrationControllerNoAux with SessionProfile {

  val flatRateService: FlatRateService
  val startDateForm : Form[FrsStartDateView]

  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("joinFrs")("frs.join")
  val registerForFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrs")("frs.registerFor")
  val yourFlatRateForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("registerForFrsWithSector")("frs.registerForWithSector")
  val annualCostsInclusiveForm: Form[AnnualCostsInclusiveView] = AnnualCostsInclusiveForm.form
  val annualCostsLimitedForm: Form[AnnualCostsLimitedView] = AnnualCostsLimitedFormFactory.form()

  def joinFrsPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          flatRateService.fetchFlatRateScheme map { flatRateScheme =>
            val form = flatRateScheme.joinFrs match {
              case Some(joinFrs) => joinFrsForm.fill(YesOrNoAnswer(joinFrs.selection))
              case None => joinFrsForm
            }
            Ok(features.frs.views.html.frs_join(form))
          }
        }
  }

  def submitJoinFRS: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          joinFrsForm.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
            joiningFRS => flatRateService.saveJoinFRS(JoinFrsView(joiningFRS.answer)) map { _ =>
              if (joiningFRS.answer) {
                Redirect(controllers.routes.FlatRateController.annualCostsInclusivePage())
              } else {
                Redirect(controllers.routes.SummaryController.show())
              }
            }
          )
        }
  }

  def annualCostsInclusivePage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            flatRateService.fetchFlatRateScheme.map { flatRateScheme =>
              val viewForm = flatRateScheme.annualCostsInclusive.fold(annualCostsInclusiveForm)(annualCostsInclusiveForm.fill)
              Ok(features.frs.views.html.annual_costs_inclusive(viewForm))
            }
          }
        }
  }

  def submitAnnualInclusiveCosts: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            annualCostsInclusiveForm.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.annual_costs_inclusive(badForm))),
              view => if (view.selection == AnnualCostsInclusiveView.NO) {
                flatRateService.isOverLimitedCostTraderThreshold map {
                  case true => Redirect(controllers.routes.FlatRateController.annualCostsLimitedPage())
                  case false => Redirect(controllers.routes.FlatRateController.confirmSectorFrsPage())
                }
              } else {
                flatRateService.saveAnnualCostsInclusive(view) map { _ =>
                  Redirect(controllers.routes.FlatRateController.registerForFrsPage())
                }
              }
            )
          }
        }
  }

  def annualCostsLimitedPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              flatRateScheme <- flatRateService.fetchFlatRateScheme
              flatRateSchemeThreshold <- flatRateService.getFlatRateSchemeThreshold()
            } yield {
              val viewForm = flatRateScheme.annualCostsLimited.fold(annualCostsLimitedForm)(annualCostsLimitedForm.fill)
              Ok(features.frs.views.html.annual_costs_limited(viewForm, flatRateSchemeThreshold))
            }
          }
        }
  }

  def submitAnnualCostsLimited: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            flatRateService.getFlatRateSchemeThreshold() flatMap { frsThreshold =>
              AnnualCostsLimitedFormFactory.form(Seq(frsThreshold)).bindFromRequest().fold(
                errors => Future.successful(BadRequest(features.frs.views.html.annual_costs_limited(errors, frsThreshold))),
                view => flatRateService.saveAnnualCostsLimited(view) map { _ =>
                  if (view.selection == AnnualCostsLimitedView.NO) {
                    Redirect(controllers.routes.FlatRateController.confirmSectorFrsPage())
                  } else {
                    Redirect(controllers.routes.FlatRateController.registerForFrsPage())
                  }
                }
              )
            }
          }
        }
  }



  def confirmSectorFrsPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            flatRateService.businessSectorView() map { view =>
              Ok(features.frs.views.html.frs_confirm_business_sector(view))
            }
          }
        }
  }

  def submitConfirmSectorFrs: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            flatRateService.businessSectorView() flatMap {
              flatRateService.saveBusinessSector(_) map { _ =>
                Redirect(controllers.routes.FlatRateController.yourFlatRatePage())
              }
            }
          }
        }
  }

  def frsStartDatePage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            flatRateService.fetchFlatRateScheme map { flatRateScheme =>
              val viewForm = flatRateScheme.frsStartDate.fold(startDateForm)(startDateForm.fill)
              Ok(features.frs.views.html.frs_start_date(viewForm))
            }
          }
        }
  }

  def submitFrsStartDate: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            startDateForm.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.frs_start_date(badForm))),
              view => flatRateService.saveFRSStartDate(view) map { _ =>
                Redirect(controllers.routes.SummaryController.show())
              }
            )
          }
        }
  }

  def registerForFrsPage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            // TODO no fetch from S4L / backend
            Future.successful(Ok(features.frs.views.html.frs_register_for(registerForFrsForm)))
          }
        }
  }

  def submitRegisterForFrs: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            registerForFrsForm.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.frs_register_for(badForm))),
              view => flatRateService.saveRegisterForFRS(view.answer) map { _ =>
                if (view.answer) {
                  Redirect(controllers.routes.FlatRateController.frsStartDatePage())
                } else {
                  Redirect(controllers.routes.SummaryController.show())
                }
              }
            )
          }
        }
  }

  def yourFlatRatePage: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          flatRateService.businessSectorView().map {
            sectorInfo => Ok(features.frs.views.html.frs_your_flat_rate(sectorInfo, yourFlatRateForm))
          }
        }
  }

  def submitYourFlatRate: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            yourFlatRateForm.bindFromRequest().fold(
              badForm => flatRateService.businessSectorView map { view =>
                BadRequest(features.frs.views.html.frs_your_flat_rate(view, badForm))
              },
              view => for {
                sector <- flatRateService.businessSectorView
                _      <- flatRateService.saveRegisterForFRS(view.answer, Some(sector))
              } yield if(view.answer) {
                Redirect(controllers.routes.FlatRateController.frsStartDatePage())
              } else {
                Redirect(controllers.routes.SummaryController.show())
              }
            )
          }
        }
  }
}
