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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import play.api.mvc.{Action, AnyContent}
import services.SaveAndRetrieveService
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SaveAndRetrieveController @Inject()(val authConnector: AuthConnector,
                                          val keystoreConnector: KeystoreConnector,
                                          saveAndRetrieveService: SaveAndRetrieveService)
                                         (implicit val executionContext: ExecutionContext,
                                          bcc: BaseControllerComponents,
                                          appConfig: FrontendAppConfig) extends BaseController {

  def save(regId: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    // TODO: Update to return OK with the new save and come back later page once implemented
    saveAndRetrieveService.savePartialVatScheme(regId).map(_ => NotImplemented)
  }

  def retrieve(regId: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => _ =>
    saveAndRetrieveService.retrievePartialVatScheme(regId).map(_ => NoContent)
  }

}