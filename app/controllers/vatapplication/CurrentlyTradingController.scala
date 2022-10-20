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

package controllers.vatapplication

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaskList
import forms.vatapplication.CurrentlyTradingForm
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.CurrentlyTrading
import services.{SessionProfile, SessionService, VatApplicationService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.vatapplication.CurrentlyTradingView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CurrentlyTradingController@Inject()(val authConnector: AuthConnector,
                                          val sessionService: SessionService,
                                          val vatApplicationService: VatApplicationService,
                                          val vatRegistrationService: VatRegistrationService,
                                          val view: CurrentlyTradingView
                                         )(implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           val baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.startDate match {
            case Some(startDate) =>
              val msgKeySuffix = if (startDate.isBefore(LocalDate.now())) "past" else "future"
              val registrationDate = DateTimeFormatter.ofPattern("d MMMM yyyy").format(startDate)
              val form = CurrentlyTradingForm(msgKeySuffix, registrationDate).form

              Ok(view(vatApplication.currentlyTrading.fold(form)(form.fill), msgKeySuffix, registrationDate))
            case None =>
              throw new InternalServerException("[CurrentlyTradingController][show] Missing registration start date")
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.flatMap { vatApplication =>
          vatApplication.startDate match {
            case Some(startDate) =>
              val msgKeySuffix = if (startDate.isBefore(LocalDate.now())) "past" else "future"
              val registrationDate = DateTimeFormatter.ofPattern("dd MMMM yyyy").format(startDate)

              CurrentlyTradingForm(msgKeySuffix, registrationDate).form.bindFromRequest.fold(
                errors => Future.successful(BadRequest(view(errors, msgKeySuffix, registrationDate))),
                success => {
                  vatApplicationService.saveVatApplication(CurrentlyTrading(success)).map { _ =>
                    if (isEnabled(TaskList)) {
                      Redirect(controllers.routes.TaskListController.show)
                    } else {
                      Redirect(routes.ReturnsFrequencyController.show)
                    }
                  }
                }
              )
            case None =>
              throw new InternalServerException("[CurrentlyTradingController][submit] Missing registration start date")
          }
        }
  }
}
