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

import java.time.LocalDate
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Request}
import play.twirl.api.Html
import services.{DateService, SessionProfile, SessionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.ExecutionContext

@Singleton
class TestWorkingDaysValidationController @Inject()(val dateService: DateService,
                                                    val authConnector: AuthClientConnector,
                                                    val sessionService: SessionService
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

  def show(): Action[AnyContent] = Action { _ =>
    Ok(Html((1 to 100).map(n =>
      s"$n working days from today => ${dateService.addWorkingDays(LocalDate.now(), n)}").mkString("<ul><li>", "</li><li>", "</li></ul>")
    ))
  }
}
