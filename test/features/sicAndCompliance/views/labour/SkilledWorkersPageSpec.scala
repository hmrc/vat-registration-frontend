/*
 * Copyright 2019 HM Revenue & Customs
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

import features.sicAndCompliance.forms.SkilledWorkersForm
import features.sicAndCompliance.models.SkilledWorkers
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import features.sicAndCompliance.views.html.labour.{skilled_workers => SkilledWorkersPage}

class SkilledWorkersPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  lazy val form = SkilledWorkersForm.form

  "Skilled Workers Page" should {
    "display radio button not pre selected" in {
      lazy val view = SkilledWorkersPage(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "skilledWorkersRadio").size shouldBe 2
      document.getElementsByAttributeValue("checked", "checked").size shouldBe 0
    }

    "display radio button not selected" in {
      lazy val view = SkilledWorkersPage(form.fill(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES)))
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "skilledWorkersRadio").size shouldBe 2

      val preSelected = document.getElementsByAttributeValue("checked", "checked")
      preSelected.size shouldBe 1
      preSelected.get(0).attr("value") shouldBe SkilledWorkers.SKILLED_WORKERS_YES
    }
  }
}
