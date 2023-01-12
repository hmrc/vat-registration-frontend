/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.flatratescheme

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.UseThisRateAnswer
import services._
import views.html.flatratescheme.YourFlatRate

import java.text.DecimalFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class YourFlatRateController @Inject()(val flatRateService: FlatRateService,
                                       val authConnector: AuthClientConnector,
                                       val sessionService: SessionService,
                                       view: YourFlatRate)
                                      (implicit appConfig: FrontendAppConfig,
                                   val executionContext: ExecutionContext,
                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val yourFlatRateForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form()("frs.registerForWithSector")

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = flatRateScheme.useThisRate match {
            case Some(useRate) => yourFlatRateForm.fill(YesOrNoAnswer(useRate))
            case None => yourFlatRateForm
          }
          flatRateService.retrieveBusinessTypeDetails.map { businessTypeDetails =>
            val decimalFormat = new DecimalFormat("#0.##")
            Ok(view(businessTypeDetails.businessTypeLabel, decimalFormat.format(businessTypeDetails.percentage), form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        yourFlatRateForm.bindFromRequest().fold(
          badForm => flatRateService.retrieveBusinessTypeDetails.map { businessTypeDetails =>
            val decimalFormat = new DecimalFormat("#0.##")
            BadRequest(view(businessTypeDetails.businessTypeLabel, decimalFormat.format(businessTypeDetails.percentage), badForm))
          },
          view => for {
            _ <- flatRateService.saveFlatRate(UseThisRateAnswer(view.answer))
          } yield {
            if (view.answer) {
              Redirect(controllers.flatratescheme.routes.StartDateController.show)
            } else {
              Redirect(controllers.routes.TaskListController.show)
            }
          }
        )
  }
}
