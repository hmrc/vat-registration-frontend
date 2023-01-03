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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.api.SicCode
import models.api.SicCode.SIC_CODES_KEY
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessActivitiesResolverController @Inject()(val sessionService: SessionService,
                                                     val authConnector: AuthConnector,
                                                     businessService: BusinessService)
                                                    (implicit val appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  // scalastyle:off
  def resolve: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          iclCodes <- sessionService.fetchAndGet[List[SicCode]](SIC_CODES_KEY).map(_.getOrElse(List.empty[SicCode]))
          mainBusinessActivitySubmissionFlow <- Future.successful(request.headers.get(REFERER).exists(
            _.contains(controllers.sicandcompliance.routes.MainBusinessActivityController.submit.url)
          ))
        } yield {
          iclCodes match {
            case codes if codes.size > 1 && !mainBusinessActivitySubmissionFlow =>
              Redirect(controllers.sicandcompliance.routes.MainBusinessActivityController.show)
            case List() =>
              throw new InternalServerException("[SicResolverController][resolve] Failed to resolve due to empty sic code list")
            case codes if businessService.needComplianceQuestions(codes) =>
              Redirect(controllers.business.routes.ComplianceIntroductionController.show)
            case _ =>
              Redirect(controllers.routes.TaskListController.show)
          }
        }
  }
}