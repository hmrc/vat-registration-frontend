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

package views

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.SubmitDeadline
import featuretoggle.FeatureToggleSupport
import forms.ApplicationReferenceForm
import org.jsoup.Jsoup
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.ApplicationReference

class ApplicationReferenceViewSpec extends VatRegViewSpec with FeatureToggleSupport {

  val form = app.injector.instanceOf[ApplicationReferenceForm]
  val view = app.injector.instanceOf[ApplicationReference]

  val featureToggle = new FrontendAppConfig (app.injector.instanceOf[ServicesConfig], app.injector.instanceOf[Configuration])

  implicit val doc = Jsoup.parse(view(form()).body)

  object ExpectedMessages {
    val heading = "Choose an application reference"
    val para1 = "You can add a memorable word, phrase or number to every application." +
      " This could be a business name or a unique number to help you manage ongoing applications."
    val label = "Application reference"
    val hint = "For example, Blue Steelworks"
    val button = "Continue"

    val bannerHeading = "You must complete and submit this VAT registration application by 19 May 2025."
  }

  "the Application Reference page" must {
    "have a title that contains the page heading" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have an explanation of what the reference is for" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.para1)
    }
    "have an input with the correct label" in new ViewSetup {
      doc.textBox("value") mustBe Some(ExpectedMessages.label)
    }
    "have the correct hint text" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedMessages.hint)
    }
    "have a button with the correct label" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.button)
    }

    "have a banner message appears when TTMayJunJourney is Enabled" should {
      appConfig.setValue(SubmitDeadline,"true")
      implicit val doc = Jsoup.parse(view(form()).body)

      "have the correct heading" in new ViewSetup {
        doc.headingLevel3 mustBe Some(ExpectedMessages.bannerHeading)
      }
    }
  }

}
