/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

class TestCacheControllerImpl @Inject()(val s4LService: S4LService,
                                        val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnect,
                                        implicit val messagesApi: MessagesApi) extends TestCacheController


trait TestCacheController extends VatRegistrationControllerNoAux with SessionProfile {
  val s4LService: S4LService

  def tearDownS4L: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          s4LService.clear.map(_ => Ok("Save4Later cleared"))
        }
  }
}
