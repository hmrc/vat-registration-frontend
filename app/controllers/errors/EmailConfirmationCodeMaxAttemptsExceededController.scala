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
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.errors.MaxConfirmationCodeAttemptsExceeded

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmailConfirmationCodeMaxAttemptsExceededController @Inject()(view: MaxConfirmationCodeAttemptsExceeded,
                                                                   val authConnector: AuthConnector,
                                                                   val sessionService: SessionService,
                                                                   val vatRegistrationService: VatRegistrationService,
                                                                   transactorDetailsService: TransactorDetailsService,
                                                                   applicantDetailsService: ApplicantDetailsService
                                                                  )(implicit appConfig: FrontendAppConfig,
                                                                    val executionContext: ExecutionContext,
                                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          email <- if (isTransactor) {
            transactorDetailsService.getTransactorDetails.map(_.email)
          } else {
            applicantDetailsService.getApplicantDetails.map(_.contact.email)
          }
        } yield Ok(view(
          email = email.getOrElse(throw new InternalServerException("[EmailConfirmationCodeMaxAttemptsExceeded] email address is not found")),
          isTransactor = isTransactor
        ))
  }
}
