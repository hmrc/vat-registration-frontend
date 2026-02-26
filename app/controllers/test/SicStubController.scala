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

package controllers.test

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.test.SicStubForm
import models.api.SicCode.SIC_CODES_KEY
import models.api.SicCode
import models.test._
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.test._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SicStubController @Inject()(val configConnect: ConfigConnector,
                                  val sessionService: SessionService,
                                  val businessService: BusinessService,
                                  val authConnector: AuthClientConnector,
                                  view: SicStubPage)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  private val sicCodes = List(
    SicCode("36000", "Collection of rain water", "Collection of rain water cy"),
    SicCode("42110", "Airport or Airfield runway construction", "Airport or Airfield runway construction cy"),
    SicCode("01110", "Barley, oats or wheat growing", "Barley, oats or wheat growing cy"),
    SicCode("81300", "Aerographing (manufacture)", "Aerographing (manufacture) cy"),
    SicCode("82190", "Blueprinting", "Blueprinting cy"),
    SicCode("81221", "Window cleaning", "Window cleaning cy"),
    SicCode("81222", "Cleaning of ventilation, heat and air ducts", "Cleaning of ventilation, heat and air ducts cy"),
    SicCode("81223", "Boiler cleaning and scaling", "Boiler cleaning and scaling cy")
  )

  val single = List("36000")
  val singleCompliance = List("42110")
  val multiple = List("01110", "81300", "82190")
  val labourSicCodes = List("81221", "81222", "81223")

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      _ =>
        Future.successful(Ok(view(SicStubForm.form)))
  }

  // scalastyle:off
  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
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
              codes.flatMap(c => sicCodes.filter(_.code == c))
            }
            _ <- sessionService.cache(SIC_CODES_KEY, sicCodesList)
            _ <- businessService.submitSicCodes(sicCodesList)
          } yield {
            Redirect(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve)
          }
        )
  }
}
