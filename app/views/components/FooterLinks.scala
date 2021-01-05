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

package views.components

import config.FrontendAppConfig
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.footer.FooterItem

object FooterLinks {

  def cookieLink(implicit messages: Messages, appConfig: FrontendAppConfig) = FooterItem(
    Some(messages("footer.cookies")),
    Some(appConfig.cookies)
  )

  def privacyLink(implicit messages: Messages, appConfig: FrontendAppConfig) = FooterItem(
    Some(messages("footer.privacy")),
    Some(appConfig.privacy)
  )

  def termsConditionsLink(implicit messages: Messages, appConfig: FrontendAppConfig) = FooterItem(
    Some(messages("footer.termsConditions")),
    Some(appConfig.termsConditions)
  )

  def govukHelpLink(implicit messages: Messages, appConfig: FrontendAppConfig) = FooterItem(
    Some(messages("footer.govukHelp")),
    Some(appConfig.govukHelp)
  )

  def accessibilityStatement(implicit messages: Messages) = FooterItem(
    Some(messages("footer.accessibilityStatement")),
    None
  )

  def items(implicit messages: Messages, appConfig: FrontendAppConfig) = Seq(
    cookieLink,
    accessibilityStatement,
    privacyLink,
    termsConditionsLink,
    govukHelpLink
  )
}
