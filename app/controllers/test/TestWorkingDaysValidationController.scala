/*
 * Copyright 2018 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}

import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent, Request}
import play.twirl.api.Html
import services.DateService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

@Singleton
class TestWorkingDaysValidationController @Inject()(dateService: DateService)
                                                   (implicit val messagesApi: MessagesApi) {

  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

  def show(): Action[AnyContent] = Action { implicit req =>
    Ok(Html((1 to 100).map(n =>
      s"$n working days from today => ${dateService.addWorkingDays(LocalDate.now(), n)}").mkString("<ul><li>", "</li><li>", "</li></ul>")
    ))
  }
}
