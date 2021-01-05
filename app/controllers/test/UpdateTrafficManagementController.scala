/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.test

import config.FrontendAppConfig
import connectors.test.TestVatRegistrationConnector
import forms.test.UpdateTrafficManagementFormProvider
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.test.update_traffic_management

import scala.concurrent.{ExecutionContext, Future}

class UpdateTrafficManagementController @Inject()(mcc: MessagesControllerComponents,
                                                  view: update_traffic_management,
                                                  form: UpdateTrafficManagementFormProvider,
                                                  testVatRegConnector: TestVatRegistrationConnector
                                                 )(implicit ec: ExecutionContext,
                                                   appConfig: FrontendAppConfig) extends FrontendController(mcc) {

  def show: Action[AnyContent] = Action { implicit request =>
    Ok(view(form()))
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      newQuota =>
        testVatRegConnector.updateTrafficManagementQuota(newQuota) map {
          _.status match {
            case OK => Ok("Updated")
            case BAD_REQUEST => BadRequest("Backend returned bad request")
            case _ => InternalServerError("Unexpected error when updating traffic management quota")
          }
        }
    )
  }

}
