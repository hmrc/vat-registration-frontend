/*
 * Copyright 2020 HM Revenue & Customs
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

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.{PrePopulationService, SessionProfile}

import scala.concurrent.ExecutionContext

@Singleton
class TestCTController @Inject()(mcc: MessagesControllerComponents,
                                 val prePopService: PrePopulationService,
                                 val authConnector: AuthClientConnector,
                                 val keystoreConnector: KeystoreConnector)
                                (implicit val appConfig: FrontendAppConfig,
                                 ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def show(): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit req =>
      implicit profile =>
        prePopService.getCTActiveDate.map(_.fold("NONE")(x => DateTimeFormatter.ISO_LOCAL_DATE.format(x))).map(x => Ok(Html(x)))
  }
}


