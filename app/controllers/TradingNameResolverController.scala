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
import models.api.{Individual, Partnership, RegSociety, UkCompany}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TradingNameResolverController @Inject()(val keystoreConnector: KeystoreConnector,
                                              val authConnector: AuthConnector,
                                              vatRegistrationService: VatRegistrationService
                                             )(implicit val appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def resolve: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatRegistrationService.partyType map {
          case Individual | Partnership => Redirect(controllers.registration.applicant.routes.SoleTraderNameController.show())
          case UkCompany | RegSociety => Redirect(controllers.registration.business.routes.TradingNameController.show())
          case pt          => throw new InternalServerException(s"PartyType: $pt not supported")
        }
  }
}
