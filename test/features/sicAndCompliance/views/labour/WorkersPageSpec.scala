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

package features.sicAndCompliance.views.labour

import features.sicAndCompliance.forms.WorkersForm
import features.sicAndCompliance.models.Workers
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import features.sicAndCompliance.views.html.labour.{workers => WorkersPage}

class WorkersPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  lazy val form = WorkersForm.form

  "Workers Page" should {
    "display page not pre populated" in {
      lazy val view = WorkersPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("numberOfWorkers").attr("value") shouldBe ""
    }

    "display page pre populated" in {
      lazy val view = WorkersPage(form.fill(Workers(5)))
      lazy val document = Jsoup.parse(view.body)

      document.getElementById("numberOfWorkers").attr("value") shouldBe "5"
    }
  }
}
