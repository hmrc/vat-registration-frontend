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

package controllers.flatratescheme

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.ChooseBusinessTypeForm
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.CategoryOfBusinessAnswer
import services.{FlatRateService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.flatratescheme.choose_business_type

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChooseBusinessTypeController @Inject()(val authConnector: AuthConnector,
                                             val sessionService: SessionService,
                                             configConnector: ConfigConnector,
                                             flatRateService: FlatRateService,
                                             chooseBusinessTypeView: choose_business_type)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          flatRateScheme <- flatRateService.getFlatRate
          form = ChooseBusinessTypeForm.form(configConnector.businessTypes.flatMap(_.categories.map(_.id)))
          formFilled = flatRateScheme.categoryOfBusiness.fold(form)(v => form.fill(v))
        } yield {
          Ok(chooseBusinessTypeView(formFilled, configConnector.businessTypes))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ChooseBusinessTypeForm.form(configConnector.businessTypes.flatMap(_.categories.map(_.id))).bindFromRequest().fold(
          badForm => Future.successful(BadRequest(chooseBusinessTypeView(badForm, configConnector.businessTypes))),
          data => flatRateService.saveFlatRate(CategoryOfBusinessAnswer(data)) map {
            _ => Redirect(controllers.flatratescheme.routes.FlatRateController.yourFlatRatePage)
          }
        )
  }


}
