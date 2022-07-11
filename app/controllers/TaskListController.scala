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

import cats.instances.future
import config.{BaseControllerComponents, FrontendAppConfig}
import featureswitch.core.config.TaskList
import models.{ApplicantDetails, CurrentProfile}
import models.api.VatScheme
import play.api.mvc.{Action, AnyContent, Request}
import services.{ApplicantDetailsService, S4LService, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import viewmodels.tasklist.{RegistrationReasonTaskList, VerifyBusinessTaskList}
import views.html.TaskList

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskListController @Inject()(vatRegistrationService: VatRegistrationService,
                                   val authConnector: AuthConnector,
                                   val sessionService: SessionService,
                                   registrationReasonSection: RegistrationReasonTaskList,
                                   verifyBusinessTaskList: VerifyBusinessTaskList,
                                   applicantDetailsService: ApplicantDetailsService,
                                   view: TaskList)
                                  (implicit val executionContext: ExecutionContext,
                                   bcc: BaseControllerComponents,
                                   appConfig: FrontendAppConfig) extends BaseController {


  def show(): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    if (isEnabled(TaskList)) {
      for {
        vatScheme <- vatRegistrationService.getVatScheme
        applicantDetails <- applicantDetailsService.getApplicantDetails // This is temporary, until we've removed S4L
          .recover { case _ => ApplicantDetails() }
        scheme = vatScheme.copy(applicantDetails = Some(applicantDetails))
      } yield Ok(view(
        registrationReasonSection.build(scheme),
        verifyBusinessTaskList.build(scheme)
      ))
    } else {
      Future.successful(NotFound)
    }
  }

}
