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
import forms.TemporaryContractsForm
import views.html.labour.temporary_contracts
import models.TemporaryContracts
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import testHelpers.VatRegSpec

class TemporaryContractsPageSpec extends VatRegSpec with I18nSupport {
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val form = TemporaryContractsForm.form

  "Temporary Contracts Page" should {
    "display radio button not pre selected" in {
      lazy val view = temporary_contracts(form)
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "temporaryContractsRadio").size mustBe 2
      document.getElementsByAttributeValue("checked", "checked").size mustBe 0
    }

    "display radio button not selected" in {
      lazy val view = temporary_contracts(form.fill(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)))
      lazy val document = Jsoup.parse(view.body)

      document.getElementsByAttributeValue("name", "temporaryContractsRadio").size mustBe 2

      val preSelected = document.getElementsByAttributeValue("checked", "checked")
      preSelected.size mustBe 1
      preSelected.get(0).attr("value") mustBe TemporaryContracts.TEMP_CONTRACTS_YES
    }
  }
}
