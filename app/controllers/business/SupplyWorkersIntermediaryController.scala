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

package controllers.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.{OtherBusinessInvolvement, TaskList}
import forms.IntermediarySupplyForm
import models.LabourCompliance
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, BusinessService, SessionProfile, SessionService}
import views.html.sicandcompliance.intermediary_supply

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SupplyWorkersIntermediaryController @Inject()(val authConnector: AuthClientConnector,
                                                    val sessionService: SessionService,
                                                    val businessService: BusinessService,
                                                    val applicantDetailsService: ApplicantDetailsService,
                                                    view: intermediary_supply)
                                                   (implicit val appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          businessDetails <- businessService.getBusiness
          name <- applicantDetailsService.getTransactorApplicantName
          intermediarySupply = businessDetails.labourCompliance.flatMap(_.intermediaryArrangement)
          intermediarySupplyForm = IntermediarySupplyForm(name)
          formFilled = intermediarySupply.fold(intermediarySupplyForm.form)(intermediarySupplyForm.form.fill)
        } yield Ok(view(formFilled, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          name <- applicantDetailsService.getTransactorApplicantName
          businessDetails <- businessService.getBusiness
          intermediarySupplyForm = IntermediarySupplyForm(name)
          result <- {
            intermediarySupplyForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(view(badForm, name))),
              data => {
                val updatedLabourCompliance = businessDetails.labourCompliance
                  .getOrElse(LabourCompliance())
                  .copy(intermediaryArrangement = Some(data))

                businessService.updateBusiness(updatedLabourCompliance) map { _ =>
                  if (isEnabled(TaskList)) {
                    Redirect(controllers.routes.TaskListController.show.url)
                  } else {
                    if (isEnabled(OtherBusinessInvolvement)) {
                      Redirect(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
                    } else {
                      Redirect(controllers.routes.TradingNameResolverController.resolve(false))
                    }
                  }
                }
              }
            )
          }
        } yield result
  }

}
