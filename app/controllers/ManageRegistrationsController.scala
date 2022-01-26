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

package controllers

import common.enums.VatRegStatus
import config.{BaseControllerComponents, FrontendAppConfig}
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.ManageRegistrations

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ManageRegistrationsController @Inject()(val authConnector: AuthConnector,
                                              val sessionService: SessionService,
                                              vatRegistrationService: VatRegistrationService,
                                              view: ManageRegistrations)
                                             (implicit val executionContext: ExecutionContext,
                                              bcc: BaseControllerComponents,
                                              appConfig: FrontendAppConfig) extends BaseController {

  def show: Action[AnyContent] = isAuthenticated { implicit reequest =>
    vatRegistrationService.getAllRegistrations.map { registrations =>
      Ok(view(registrations.filter(reg => reg.status == VatRegStatus.draft || reg.status == VatRegStatus.submitted)))
    }
  }

}
