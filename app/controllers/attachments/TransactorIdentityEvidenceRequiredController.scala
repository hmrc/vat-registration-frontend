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

package controllers.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService, TransactorDetailsService}
import views.html.attachments.TransactorIdentityEvidenceRequired

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TransactorIdentityEvidenceRequiredController @Inject()(val authConnector: AuthClientConnector,
                                                             val sessionService: SessionService,
                                                             view: TransactorIdentityEvidenceRequired,
                                                             applicantDetailsService: ApplicantDetailsService,
                                                             transactorDetailsService: TransactorDetailsService)
                                                            (implicit appConfig: FrontendAppConfig,
                                                             val executionContext: ExecutionContext,
                                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicantName <- applicantDetailsService.getApplicantDetails.map(data =>
            if (data.personalDetails.exists(!_.identifiersMatch)) {
              data.personalDetails.map(_.fullName)
            } else {
              None
            }
          )
          transactorName <- transactorDetailsService.getTransactorDetails.map(data =>
            if (data.personalDetails.exists(!_.identifiersMatch)) {
              data.personalDetails.map(_.fullName)
            } else {
              None
            }
          )
          names = List(applicantName, transactorName).flatten
        } yield Ok(view(names))
  }

}
