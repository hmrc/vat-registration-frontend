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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.vatapplication.VoluntaryDateForm
import models._
import play.api.mvc.{Action, AnyContent}
import services._
import utils.MessageDateFormat
import views.html.vatapplication.StartDateIncorp

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoluntaryStartDateController @Inject()(val sessionService: SessionService,
                                             val authConnector: AuthClientConnector,
                                             val vatApplicationService: VatApplicationService,
                                             val timeService: TimeService,
                                             voluntaryStartDateIncorpPage: StartDateIncorp
                                            )(implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      vatApplicationService.getVatApplication.flatMap { vatApplication =>
        vatApplicationService.calculateEarliestStartDate().map { incorpDate =>
          val exampleVatStartDate = timeService.dynamicFutureDateExample()

          val voluntaryDateForm = VoluntaryDateForm
            .form(timeService.getMinWorkingDayInFuture, timeService.addMonths(3))
          val filledForm = vatApplication.startDate match {
            case Some(startDate) if incorpDate == startDate =>
              voluntaryDateForm.fill((DateSelection.company_registration_date, Some(startDate)))
            case Some(startDate) =>
              voluntaryDateForm.fill((DateSelection.specific_date, Some(startDate)))
            case _ =>
              voluntaryDateForm
          }

          val incorpDateAfter = incorpDate.isAfter(timeService.minusYears(4))

          Ok(voluntaryStartDateIncorpPage(filledForm, MessageDateFormat.format(incorpDate), incorpDateAfter, exampleVatStartDate))
        }
      }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.calculateEarliestStartDate().flatMap { incorpDate =>
          val voluntaryDateForm = VoluntaryDateForm.form(incorpDate, timeService.addMonths(3))
          voluntaryDateForm.bindFromRequest.fold(
            errors => {
              val dynamicDate = timeService.dynamicFutureDateExample()
              val incorpDateAfter = incorpDate.isAfter(timeService.minusYears(4))

              Future.successful(BadRequest(voluntaryStartDateIncorpPage(errors, MessageDateFormat.format(incorpDate), incorpDateAfter, dynamicDate)))
            },
            success => vatApplicationService.saveVoluntaryStartDate(success._1, success._2, incorpDate).map(_ =>
              Redirect(controllers.vatapplication.routes.CurrentlyTradingController.show)
            )
          )
        }
  }
}
