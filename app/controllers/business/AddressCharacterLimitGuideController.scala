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
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.business.AddressCharacterLimitGuideView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import services.VatRegistrationService
import models.api.{Individual, LtdLiabilityPartnership, NETP, NonUkNonEstablished, Partnership, Trust}

@Singleton
class AddressCharacterLimitGuideController @Inject()(val authConnector: AuthConnector,
                                                     val sessionService: SessionService,
                                                     val vatRegistrationService: VatRegistrationService,
                                                     view: AddressCharacterLimitGuideView)
                                                    (implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>_ =>
      Future.successful(Ok(view()))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      vatRegistrationService.getEligibilitySubmissionData.map { data =>
        (data.partyType, data.fixedEstablishmentInManOrUk) match {
          case (Individual | NonUkNonEstablished | Partnership | LtdLiabilityPartnership | Trust, false) => Redirect(controllers.business.routes.InternationalPpobAddressController.show)
          case _ => Redirect(controllers.business.routes.PpobAddressController.startJourney)
        }
      }
  }
}