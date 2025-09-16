/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.CaptureTradingNameForm
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.TradingName
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.business.CaptureTradingNameView
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureTradingNameController @Inject()(val sessionService: SessionService,
                                             val authConnector: AuthConnector,
                                             val applicantDetailsService: ApplicantDetailsService,
                                             val businessService: BusinessService,
                                             view: CaptureTradingNameView
                                            )(implicit val appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          optTradingName <- businessService.getBusiness.map(_.tradingName)
          form = optTradingName.fold(CaptureTradingNameForm.form)(CaptureTradingNameForm.form.fill)
        } yield Ok(view(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        CaptureTradingNameForm.form.bindFromRequest().fold(
          errors =>
            Future.successful(BadRequest(view(errors))),
          success => {
            businessService.updateBusiness(TradingName(success)).map { _ =>
              Redirect(controllers.business.routes.AddressCharacterLimitGuideController.show)
            }
          }
        )
  }
}
