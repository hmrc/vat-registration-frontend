/*
 * Copyright 2024 HM Revenue & Customs
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

package config

import play.api.i18n.MessagesApi
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.errors.ErrorTemplate

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FrontendGlobal @Inject()(
           val messagesApi: MessagesApi,
           val appConfig: FrontendAppConfig,
           errorTemplate: ErrorTemplate
   )(implicit val ec: ExecutionContext)
      extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: RequestHeader): Future[Html] = {
    implicit val ac: FrontendAppConfig = appConfig
    Future.successful(errorTemplate(pageTitle, heading, message))
  }

}
