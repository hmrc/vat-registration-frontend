/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.UrBannerEnabled
import featuretoggle.FeatureToggleSupport
import play.api.i18n.{Lang, Messages}
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En, UserResearchBanner}
import viewmodels.UrBannerHelper

class UrBannerHelperSpec extends ControllerSpec with FeatureToggleSupport {

  "getUrBanner" when {
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
    def messages: Messages = messagesApi.preferred(FakeRequest())


    "the showUserResearchBanner feature is disabled" should {

      "return None" in {
        disable(UrBannerEnabled)
        UrBannerHelper.getUrBanner()(appConfig, messages) mustBe None
      }
    }

    "the showUserResearchBanner feature is enabled" when {

      "the language is English" should {

        "return a UserResearchBanner with En language and the English URL" in {
          enable(UrBannerEnabled)
          UrBannerHelper.getUrBanner()(appConfig, messages) mustBe Some(UserResearchBanner(
            language = En,
            url = appConfig.urBannerBaseUrl,
            hideCloseButton = true
          ))
        }
      }

      "the language is Welsh" should {

        "return a UserResearchBanner with Cy language and the Welsh URL" in {
          enable(UrBannerEnabled)
          val welshMessages = messagesApi.preferred(Seq(Lang("cy")))
          UrBannerHelper.getUrBanner()(appConfig, welshMessages) mustBe Some(UserResearchBanner(
            language = Cy,
            url = s"${appConfig.urBannerBaseUrl}&Q_Language=CY",
            hideCloseButton = true
          ))
        }
      }

      "hideCloseButton is false" should {

        "return a UserResearchBanner with hideCloseButton set to false" in {
          enable(UrBannerEnabled)
          val result = UrBannerHelper.getUrBanner(hideCloseButton = false)(appConfig, messages)
          result.map(_.hideCloseButton) mustBe Some(false)
        }
      }
    }
  }
}