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

package controllers.test

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import featureswitch.core.config.OtherBusinessInvolvement
import forms.test.SicStubForm
import models.ModelKeys.SIC_CODES_KEY
import models.test._
import play.api.mvc.{Action, AnyContent}
import services.{BusinessService, S4LService, SessionProfile, SessionService}
import views.html.test._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SicStubController @Inject()(val configConnect: ConfigConnector,
                                  val sessionService: SessionService,
                                  val s4LService: S4LService,
                                  val businessService: BusinessService,
                                  val authConnector: AuthClientConnector,
                                  view: SicStubPage)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val single = List("36000")
  val singleCompliance = List("42110")
  val multiple = List("01110", "81300", "82190")
  val labourSicCodes = List("81221", "81222", "81223")

  def show: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(view(SicStubForm.form)))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        SicStubForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          data => for {
            sicCodesList <- Future {
              val codes = data.selection match {
                case SingleSicCode => single
                case SingleSicCodeCompliance => singleCompliance
                case MultipleSicCodeNoCompliance => multiple
                case MultipleSicCodeCompliance => labourSicCodes
                case CustomSicCodes => data.fullSicCodes
              }
              codes.map(configConnect.getSicCodeDetails).map(s => s.copy(code = s.code.substring(0, 5)))
            }
            _ <- sessionService.cache(SIC_CODES_KEY, sicCodesList)
            _ <- businessService.submitSicCodes(sicCodesList)
          } yield {
            if (sicCodesList.size == 1) {
              if (businessService.needComplianceQuestions(sicCodesList)) {
                Redirect(controllers.business.routes.ComplianceIntroductionController.show)
              } else {
                if (isEnabled(OtherBusinessInvolvement)) {
                  Redirect(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
                } else {
                  Redirect(controllers.routes.TradingNameResolverController.resolve(false))
                }
              }
            } else {
              Redirect(controllers.business.routes.SicController.showMainBusinessActivity)
            }
          }
        )
  }
}
