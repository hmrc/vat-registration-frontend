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

import config.{BaseControllerComponents, FrontendAppConfig}
import featureswitch.core.config.TaskList
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.tasklist._
import views.html.TaskList

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskListController @Inject()(vatRegistrationService: VatRegistrationService,
                                   val authConnector: AuthConnector,
                                   val sessionService: SessionService,
                                   registrationReasonSection: RegistrationReasonTaskList,
                                   aboutYouTransactorTaskList: AboutYouTransactorTaskList,
                                   verifyBusinessTaskList: VerifyBusinessTaskList,
                                   aboutYouTaskList: AboutYouTaskList,
                                   aboutTheBusinessTaskList: AboutTheBusinessTaskList,
                                   vatRegistrationTaskList: VatRegistrationTaskList,
                                   attachmentsTaskList: AttachmentsTaskList,
                                   summaryTaskList: SummaryTaskList,
                                   applicantDetailsService: ApplicantDetailsService,
                                   transactorDetailsService: TransactorDetailsService,
                                   businessService: BusinessService,
                                   vatApplicationService: VatApplicationService,
                                   flatRateService: FlatRateService,
                                   view: TaskList)
                                  (implicit val executionContext: ExecutionContext,
                                   bcc: BaseControllerComponents,
                                   appConfig: FrontendAppConfig) extends BaseController {


  def show(): Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(TaskList)) {
      for {
        vatScheme <- vatRegistrationService.getVatScheme
        applicantDetails <- applicantDetailsService.getApplicantDetails
        transactorDetails <- transactorDetailsService.getTransactorDetails
        business <- businessService.getBusiness
        vatApplication <- vatApplicationService.getVatApplication
        attachmentsTaskListRow <- attachmentsTaskList.attachmentsRequiredRow
        scheme = vatScheme.copy(
          applicantDetails = Some(applicantDetails),
          transactorDetails = Some(transactorDetails),
          business = Some(business),
          vatApplication = Some(vatApplication)
        ) // Grabbing the data from two sources is temporary, until we've removed S4L
        sections = List(
          Some(registrationReasonSection.build(scheme)),
          if (vatScheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(aboutYouTransactorTaskList.build(scheme)) else None,
          Some(verifyBusinessTaskList.build(scheme)),
          Some(aboutYouTaskList.build(scheme)),
          Some(aboutTheBusinessTaskList.build(scheme)),
          Some(vatRegistrationTaskList.build(scheme)),
          attachmentsTaskListRow.map(attachmentsTaskList.build(scheme, _)),
          Some(summaryTaskList.build(scheme, attachmentsTaskListRow))
        ).flatten
      } yield Ok(view(sections: _*))
    } else {
      Future.successful(NotFound)
    }
  }

}
