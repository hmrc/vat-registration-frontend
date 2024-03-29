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

package controllers.flatratescheme

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.FRSStartDateForm
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionService, TimeService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.flatratescheme.FrsStartDate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartDateController @Inject()(val authConnector: AuthConnector,
                                    val sessionService: SessionService,
                                    flatRateService: FlatRateService,
                                    timeService: TimeService,
                                    view: FrsStartDate)
                                   (implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents) extends BaseController {

  private val maxDateAddedMonths = 3

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    for {
      earliestDate <- flatRateService.fetchVatStartDate
      (optChoice, optDate) <- flatRateService.getPrepopulatedStartDate(earliestDate)
      exampleDate = timeService.dynamicFutureDateExample(earliestDate)
      maxDate = timeService.today.plusMonths(maxDateAddedMonths)
    } yield {
      val filledForm = optChoice.fold(FRSStartDateForm.form(earliestDate, maxDate)) {
        choice => FRSStartDateForm.form(earliestDate, maxDate).fill((choice, optDate))
      }
      Ok(view(filledForm, exampleDate))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    flatRateService.fetchVatStartDate.flatMap { earliestDate =>
      val maxDate = timeService.today.plusMonths(maxDateAddedMonths)

      FRSStartDateForm.form(earliestDate, maxDate).bindFromRequest().fold(
        formWithErrors => {
          val dynamicDate = timeService.dynamicFutureDateExample()
          Future.successful(BadRequest(view(formWithErrors, dynamicDate)))
        },
        answers => {
          val (choice, optDate) = answers
          flatRateService.saveStartDate(choice, optDate) map { _ =>
            Redirect(controllers.routes.TaskListController.show)
          }
        }
      )
    }
  }

}
