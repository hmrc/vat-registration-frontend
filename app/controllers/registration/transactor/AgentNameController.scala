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

package controllers.registration.transactor

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.AgentNameForm
import models.{AccountantAgent, DeclarationCapacityAnswer, FinancialController, PersonalDetails}
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, TransactorDetailsService}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import views.html.transactor.AgentNameView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentNameController @Inject()(val authConnector: AuthConnector,
                                    val sessionService: SessionService,
                                    transactorDetailsService: TransactorDetailsService,
                                    form: AgentNameForm,
                                    view: AgentNameView
                                   )(implicit val executionContext: ExecutionContext,
                                    bcc: BaseControllerComponents,
                                    appConfig: FrontendAppConfig
                                   ) extends BaseController  with AuthorisedFunctions {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    for {
      transactorDetails <- transactorDetailsService.getTransactorDetails
      optPersonalDetails = transactorDetails.personalDetails
      filledForm = optPersonalDetails.fold(form())(details => form().fill(details.firstName, details.lastName))
    } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    form().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
      name => {
        val personalDetails = PersonalDetails(
          firstName = name._1,
          lastName = name._2,
          arn = profile.agentReferenceNumber,
          identifiersMatch = true
        )

        for {
          _ <- transactorDetailsService.saveTransactorDetails(personalDetails)
          _ <- transactorDetailsService.saveTransactorDetails(DeclarationCapacityAnswer(AccountantAgent))
        } yield Redirect(routes.TelephoneNumberController.show)
      }
    )
  }

}
