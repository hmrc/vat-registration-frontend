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
import controllers.BaseController
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.flatratescheme.frs_confirm_business_sector

import java.util.MissingResourceException
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ConfirmBusinessTypeController @Inject()(val authConnector: AuthConnector,
                                              val sessionService: SessionService,
                                              flatRateService: FlatRateService,
                                              view: frs_confirm_business_sector)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        flatRateService.retrieveBusinessTypeDetails map { businessTypeDetails =>
          Ok(view(businessTypeDetails.businessTypeLabel))
        } recover {
          case _: MissingResourceException => Redirect(controllers.flatratescheme.routes.ChooseBusinessTypeController.show)
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        flatRateService.saveConfirmSector map { _ =>
          Redirect(controllers.flatratescheme.routes.FlatRateController.yourFlatRatePage)
        }
  }

}
