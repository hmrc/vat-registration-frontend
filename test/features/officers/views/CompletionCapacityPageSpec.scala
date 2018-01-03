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

package features.officers.views

import forms.vatLodgingOfficer.CompletionCapacityForm
import models.api.Name
import models.external.Officer
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import features.officers.views.html.{completion_capacity => CompletionCapacityPage}
import play.api.inject.Injector
import play.api.test.FakeRequest

class CompletionCapacityPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  val validOfficer1 = Officer(
    name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
    role = "Director"
  )
  val validOfficer2 = Officer(
    name = Name(forename = Some("First2"), otherForenames = None, surname = "Last2"),
    role = "Super director"
  )
  val officerList = Seq(validOfficer1, validOfficer2)

  lazy val form = CompletionCapacityForm.form

  "Completion Capacity Page" should {
    "display a list of officers without pre selection" in {
      lazy val view = CompletionCapacityPage(form, officerList)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "completionCapacityRadio").size shouldBe 2
      document.getElementsByAttributeValue("checked", "checked").size shouldBe 0
    }
  }
}
