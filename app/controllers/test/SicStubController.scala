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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.{ConfigConnector, KeystoreConnector}
import controllers.BaseController
import forms.test.SicStubForm
import models.ModelKeys.SIC_CODES_KEY
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile, SicAndComplianceService}
import views.html.test._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SicStubController @Inject()(val configConnect: ConfigConnector,
                                  val keystoreConnector: KeystoreConnector,
                                  val s4LService: S4LService,
                                  val sicAndCompService: SicAndComplianceService,
                                  val authConnector: AuthClientConnector)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(sic_stub(SicStubForm.form)))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        SicStubForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(sic_stub(badForm))),
          data => for {
            sicCodesList <- Future {
              data.fullSicCodes.map(configConnect.getSicCodeDetails).map(s => s.copy(code = s.code.substring(0, 5)))
            }
            _ <- keystoreConnector.cache(SIC_CODES_KEY, sicCodesList)
            _ <- sicAndCompService.submitSicCodes(sicCodesList)
          } yield {
            if (sicCodesList.size == 1) {
              if (sicAndCompService.needComplianceQuestions(sicCodesList)) {
                Redirect(controllers.routes.SicAndComplianceController.showComplianceIntro())
              } else {
                Redirect(controllers.registration.business.routes.TradingNameController.show())
              }
            } else {
              Redirect(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
            }
          }
        )
  }
}