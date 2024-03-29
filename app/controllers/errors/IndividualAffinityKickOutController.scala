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

package controllers.errors

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.errors.IndividualAffinityKickOut

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualAffinityKickOutController @Inject()(view: IndividualAffinityKickOut,
                                                    val authConnector: AuthConnector,
                                                    val sessionService: SessionService
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised()
      Future.successful(Ok(view()))
  }

  val signOutAndRedirect: Action[AnyContent] = Action.async {
    Future.successful(SeeOther(appConfig.individualKickoutUrl(controllers.routes.JourneyController.show.url)).withNewSession)
  }

  val businessSignInRedirect: Action[AnyContent] = Action.async {
    Future.successful(SeeOther(appConfig.businessSignInLink).withNewSession)
  }
}
