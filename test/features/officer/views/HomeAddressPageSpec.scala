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

package features.officer.views

import features.officer.forms.HomeAddressForm
import features.officer.models.view.HomeAddressView
import features.officer.views.html.{officer_home_address => HomeAddressPage}
import models.api.ScrsAddress
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class HomeAddressPageSpec extends UnitSpec with WithFakeApplication with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  val address1 = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
  val address2 = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE 1ST"))
  val addressList = Seq(address1, address2)

  lazy val form = HomeAddressForm.form

  "Home Address Page" should {
    "display the page without pre populated data" in {
      lazy val view = HomeAddressPage(form, addressList)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "homeAddressRadio").size shouldBe 3
      document.getElementsByAttributeValue("checked", "checked").size shouldBe 0

      document.getElementById(s"homeAddressRadio-${address1.id.toLowerCase}").attr("value") shouldBe address1.id
      document.getElementById(s"homeAddressRadio-${address2.id.toLowerCase}").attr("value") shouldBe address2.id
    }

    "display the page with form pre populated" in {
      val validContactDetails = HomeAddressView(address2.id, Some(address1))

      lazy val view = HomeAddressPage(form.fill(validContactDetails), addressList)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "homeAddressRadio").size shouldBe 3

      val preSelected = document.getElementsByAttributeValue("checked", "checked")
      preSelected.size shouldBe 1
      preSelected.get(0).attr("value") shouldBe address2.id
    }
  }
}
