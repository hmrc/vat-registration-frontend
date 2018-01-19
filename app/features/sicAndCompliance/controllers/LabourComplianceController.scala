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

package features.sicAndCompliance.controllers

import javax.inject.Inject

import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.sicAndCompliance.forms.{CompanyProvideWorkersForm, SkilledWorkersForm, TemporaryContractsForm, WorkersForm}
import features.sicAndCompliance.services.SicAndComplianceService
import features.sicAndCompliance.views.html.labour.{company_provide_workers, skilled_workers, temporary_contracts, workers}
import features.sicAndCompliance.models.CompanyProvideWorkers.PROVIDE_WORKERS_YES
import features.sicAndCompliance.models.{SicAndCompliance, TemporaryContracts}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class LabourComplianceControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnect,
                                               val sicAndCompService: SicAndComplianceService) extends LabourComplianceController

trait LabourComplianceController extends VatRegistrationControllerNoAux with SessionProfile {
  val sicAndCompService: SicAndComplianceService

  def showProvideWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled    = sicCompliance.companyProvideWorkers.fold(CompanyProvideWorkersForm.form)(CompanyProvideWorkersForm.form.fill)
            } yield Ok(company_provide_workers(formFilled))
          }
        }
  }

  def submitProvideWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            CompanyProvideWorkersForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(company_provide_workers(badForm))),
              view => sicAndCompService.updateSicAndCompliance(view).map { _ =>
                if (PROVIDE_WORKERS_YES == view.yesNo) {
                  routes.LabourComplianceController.showWorkers()
                } else {
                  controllers.routes.TradingDetailsController.euGoodsPage()
                }
              } map Redirect
            )
          }
        }
  }

  def showWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled    = sicCompliance.workers.fold(WorkersForm.form)(WorkersForm.form.fill)
            } yield Ok(workers(formFilled))
          }
        }
  }

  def submitWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            WorkersForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(workers(badForm))),
              data => sicAndCompService.updateSicAndCompliance(data) map { _ =>
                if (data.numberOfWorkers >= SicAndCompliance.NUMBER_OF_WORKERS_THRESHOLD) {
                  routes.LabourComplianceController.showTemporaryContracts()
                } else {
                  controllers.routes.TradingDetailsController.euGoodsPage()
                }
              } map Redirect
            )
          }
        }
  }

  def showTemporaryContracts: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled    = sicCompliance.temporaryContracts.fold(TemporaryContractsForm.form)(TemporaryContractsForm.form.fill)
            } yield Ok(temporary_contracts(formFilled))
          }
        }
  }

  def submitTemporaryContracts: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            TemporaryContractsForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(temporary_contracts(badForm))),
              data => sicAndCompService.updateSicAndCompliance(data) map { _ =>
                if(data.yesNo == TemporaryContracts.TEMP_CONTRACTS_YES) {
                  routes.LabourComplianceController.showSkilledWorkers()
                } else {
                  controllers.routes.TradingDetailsController.euGoodsPage()
                }
              } map Redirect
            )
          }
        }
  }

  def showSkilledWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled = sicCompliance.skilledWorkers.fold(SkilledWorkersForm.form)(SkilledWorkersForm.form.fill)
            } yield Ok(skilled_workers(formFilled))
          }
        }
  }

  def submitSkilledWorkers: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          SkilledWorkersForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(skilled_workers(badForm))),
            view => for {
              _ <- sicAndCompService.updateSicAndCompliance(view)
            } yield Redirect(controllers.routes.TradingDetailsController.euGoodsPage())
          )
        }
  }
}
