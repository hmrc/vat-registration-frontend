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
import featureswitch.core.config.TaskList
import forms.vatapplication.ChargeExpectancyForm
import models.api.{NETP, NonUkNonEstablished}
import models.{BackwardLook, ForwardLook, NonUk, TransferOfAGoingConcern}
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.ClaimVatRefunds
import services.{SessionProfile, SessionService, VatApplicationService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException
import views.html.vatapplication.claim_refunds_view

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimRefundsController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val vatApplicationService: VatApplicationService,
                                       val vatRegistrationService: VatRegistrationService,
                                       val claimRefundsView: claim_refunds_view)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.claimVatRefunds match {
            case Some(claimVatRefunds) => Ok(claimRefundsView(ChargeExpectancyForm.form.fill(claimVatRefunds)))
            case None => Ok(claimRefundsView(ChargeExpectancyForm.form))
          }
        }
  }

  //scalastyle:off
  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ChargeExpectancyForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(claimRefundsView(errors))),
          success => {
            for {
              vatApplication <- vatApplicationService.saveVatApplication(ClaimVatRefunds(success))
              turnover = vatApplication.turnoverEstimate.getOrElse(throw new InternalServerException("[ClaimRefundsController] Missing turnover"))
              zeroRatedSupplies = vatApplication.zeroRatedSupplies.getOrElse(throw new InternalServerException("[ClaimRefundsController] Missing zero rated turnover"))
              eligibilityData <- vatRegistrationService.getEligibilitySubmissionData
              canApplyForExemption = success &&
                (zeroRatedSupplies * 2 > turnover) &&
                !eligibilityData.appliedForException.contains(true) &&
                List(ForwardLook, BackwardLook, NonUk, TransferOfAGoingConcern).contains(eligibilityData.registrationReason)
            } yield eligibilityData.partyType match {
              case _ if canApplyForExemption =>
                Redirect(routes.VatExemptionController.show)
              case NETP | NonUkNonEstablished =>
                Redirect(routes.SendGoodsOverseasController.show)
              case _ =>
                if (isEnabled(TaskList)) {
                  Redirect(controllers.routes.TaskListController.show.url)
                } else {
                  Redirect(controllers.bankdetails.routes.HasBankAccountController.show)
                }
            }
          }
        )
  }
}
