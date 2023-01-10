/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.sicandcompliance

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.FeatureSwitching
import forms.MainBusinessActivityForm
import models.api.SicCode.SIC_CODES_KEY
import models.api.SicCode
import play.api.mvc.{Action, AnyContent}
import services.BusinessService._
import services.{BusinessService, FlatRateService, SessionProfile, SessionService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.sicandcompliance.MainBusinessActivity

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MainBusinessActivityController @Inject()(val authConnector: AuthClientConnector,
                                               val sessionService: SessionService,
                                               val businessService: BusinessService,
                                               val frsService: FlatRateService,
                                               val mainBusinessActivityPage: MainBusinessActivity)
                                              (implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile with FeatureSwitching {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          sicCodeList <- fetchSicCodeList
          businessDetails <- businessService.getBusiness
          formFilled = businessDetails.mainBusinessActivity.fold(MainBusinessActivityForm.form)(sicCode => MainBusinessActivityForm.form.fill(sicCode.code))
        } yield Ok(mainBusinessActivityPage(formFilled, sicCodeList))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        fetchSicCodeList flatMap { sicCodeList =>
          MainBusinessActivityForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(mainBusinessActivityPage(badForm, sicCodeList))),
            data => sicCodeList.find(_.code == data).fold(
              Future.successful(BadRequest(mainBusinessActivityPage(MainBusinessActivityForm.form.fill(data), sicCodeList)))
            )(selected => for {
              _ <- businessService.updateBusiness(MainBusinessActivity(selected))
              _ <- frsService.resetFRSForSAC(selected)
            } yield {
              Redirect(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve)
            })
          )
        }
  }

  private def fetchSicCodeList()(implicit hc: HeaderCarrier): Future[List[SicCode]] =
    sessionService.fetchAndGet[List[SicCode]](SIC_CODES_KEY) map (_.getOrElse(List.empty[SicCode]))
}
