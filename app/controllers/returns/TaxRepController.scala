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

package controllers.returns

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.TaxRepForm
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.returns.TaxRepresentative

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxRepController @Inject()(val authConnector: AuthConnector,
                                 val sessionService: SessionService,
                                 val returnsService: ReturnsService,
                                 val vatRegistrationService: VatRegistrationService,
                                 val taxRepPage: TaxRepresentative
                                )(implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  val baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.hasTaxRepresentative match {
            case Some(true) => Ok(taxRepPage(TaxRepForm.form.fill(true)))
            case _ => Ok(taxRepPage(TaxRepForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
      TaxRepForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(taxRepPage(errors))),
        success => {
          for {
            returns <- returnsService.getReturns
            updatedReturns = returns.copy(
              hasTaxRepresentative = Some(success)
            )
            _ <- returnsService.submitReturns(updatedReturns)
          } yield {
            Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show)
          }
        }
      )
  }

}