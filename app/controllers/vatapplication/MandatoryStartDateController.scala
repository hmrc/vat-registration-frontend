/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.vatapplication.MandatoryDateForm
import models._
import play.api.mvc.{Action, AnyContent, Request}
import services._
import uk.gov.hmrc.http.HeaderCarrier
import utils.MessageDateFormat
import views.html.vatapplication.{MandatoryStartDateIncorpView, MandatoryStartDateNoChoiceView}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MandatoryStartDateController @Inject()(val sessionService: SessionService,
                                             val authConnector: AuthClientConnector,
                                             vatApplicationService: VatApplicationService,
                                             mandatoryStartDateIncorpView: MandatoryStartDateIncorpView,
                                             mandatoryStartDateNoChoiceView: MandatoryStartDateNoChoiceView
                                            )(implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.calculateEarliestStartDate().flatMap { incorpDate =>
          vatApplicationService.retrieveMandatoryDates.map {
            case dateModel if dateModel.calculatedDate.isBefore(LocalDate.now().minusYears(4)) =>
              Ok(mandatoryStartDateNoChoiceView(MessageDateFormat.format(dateModel.calculatedDate)))
            case dateModel =>
              val form = MandatoryDateForm.form(incorpDate, dateModel.calculatedDate)
              Ok(mandatoryStartDateIncorpView(
                dateModel.selected.fold(form) { selection => form.fill((selection, dateModel.startDate)) },
                MessageDateFormat.format(dateModel.calculatedDate)
              ))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.calculateEarliestStartDate().flatMap(incorpDate =>
          vatApplicationService.retrieveCalculatedStartDate.flatMap { calcDate =>
            MandatoryDateForm.form(incorpDate, calcDate).bindFromRequest().fold(
              errors => {
                Future.successful(BadRequest(mandatoryStartDateIncorpView(errors, MessageDateFormat.format(calcDate))))
              },
              {
                case (DateSelection.specific_date, Some(startDate)) => handleMandatoryStartDate(startDate)
                case (DateSelection.calculated_date, _) => handleMandatoryStartDate(calcDate)
              }
            )
          }
        )
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.retrieveCalculatedStartDate.flatMap { calculatedDate =>
          vatApplicationService.saveVatApplication(calculatedDate).map { _ =>
            Redirect(controllers.routes.TaskListController.show.url)
          }
        }
  }

  private def handleMandatoryStartDate(startDate: LocalDate)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile, request: Request[_]) = {
    vatApplicationService.saveVatApplication(startDate).map(_ =>
      Redirect(controllers.routes.TaskListController.show.url)
    )
  }
}
