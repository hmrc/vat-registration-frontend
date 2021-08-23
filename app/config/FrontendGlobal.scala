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

package config

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import views.html.pages.error.error_template
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler

class FrontendGlobal @Inject()(error_template: error_template)(val messagesApi: MessagesApi, val appConfig: FrontendAppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    implicit val ac = appConfig
    error_template(pageTitle, heading, message)
  }

}
