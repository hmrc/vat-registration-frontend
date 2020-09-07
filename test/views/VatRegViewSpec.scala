/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.html.helpers.formWithCSRF
import views.html.components.{button, h1, p}
import views.html.layouts.layout

class VatRegViewSpec extends PlaySpec with GuiceOneAppPerSuite with I18nSupport {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testCall: Call = Call("POST", "/test-url")

  object Selectors extends BaseSelectors

  val layout: layout = app.injector.instanceOf[layout]
  val h1: h1 = app.injector.instanceOf[h1]
  val p: p = app.injector.instanceOf[p]
  val button: button = app.injector.instanceOf[button]
  val formWithCSRF: formWithCSRF = app.injector.instanceOf[formWithCSRF]
}
