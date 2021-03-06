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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}

import scala.concurrent.ExecutionContext

@Singleton
class TestCacheController @Inject()(val s4LService: S4LService,
                                    val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector)
                                   (implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def tearDownS4L: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        s4LService.clear.map(_ => Ok("Save4Later cleared"))
  }
}
