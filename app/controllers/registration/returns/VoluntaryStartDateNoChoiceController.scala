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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.VoluntaryStartDateNoChoiceForm
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionService, TimeService}
import views.html.returns.VoluntaryStartDateNoChoice

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoluntaryStartDateNoChoiceController @Inject()(val sessionService: SessionService,
                                                     val authConnector: AuthClientConnector,
                                                     returnsService: ReturnsService,
                                                     formProvider: VoluntaryStartDateNoChoiceForm,
                                                     timeService: TimeService,
                                                     view: VoluntaryStartDateNoChoice)
                                                    (implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents) extends BaseController {

  private val exampleDateFormat = "d M yyyy"
  private val exampleDateFormatter = DateTimeFormatter.ofPattern(exampleDateFormat)

  def show: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    returnsService.getReturns.map { returnsSection =>
      val form = returnsSection.startDate.fold(formProvider())(formProvider().fill)
      Ok(view(form, timeService.today.format(exampleDateFormatter)))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    formProvider().bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, timeService.today.format(exampleDateFormatter)))),
      startDate =>
       returnsService.saveVatStartDate(Some(startDate)).map { _ =>
         Redirect(routes.ReturnsController.returnsFrequencyPage)
       }
    )
  }

}
