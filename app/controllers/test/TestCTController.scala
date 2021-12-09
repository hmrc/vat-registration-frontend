/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.test

import java.time.format.DateTimeFormatter
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.Html
import services.{PrePopulationService, SessionProfile, SessionService}

import scala.concurrent.ExecutionContext

@Singleton
class TestCTController @Inject()(val prePopService: PrePopulationService,
                                 val authConnector: AuthClientConnector,
                                 val sessionService: SessionService)
                                (implicit val appConfig: FrontendAppConfig,
                                 val executionContext: ExecutionContext,
                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show(): Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit req =>
      implicit profile =>
        prePopService.getCTActiveDate.map(_.fold("NONE")(x => DateTimeFormatter.ISO_LOCAL_DATE.format(x))).map(x => Ok(Html(x)))
  }
}


