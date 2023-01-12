/*
 * Copyright 2023 HM Revenue & Customs
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

trait BaseSelectors {

  val panelHeading = "main div.govuk-panel.govuk-panel--confirmation h1"
  val panelBody = "main div.govuk-panel.govuk-panel--confirmation div.govuk-panel__body"
  val h1: String = "h1"
  val h2 = "main h2"
  val h2ConfirmationPage = "#main-content > div > div > h2"
  val p: String = "main p"
  val legends: String = "main div div div fieldset legend"
  val a: String = "main a"
  val indent = "div.govuk-inset-text"
  val hint = "div.govuk-hint"
  val multipleHints: String = "div.govuk-hint"
  val bullets: String = "main ul.govuk-list.govuk-list--bullet li"
  val label = "main label.govuk-label"
  val nthLabel: String = "form > div > div > label"
  val warning = "main .govuk-warning-text__text"
  val contactHmrc = "#contact-hmrc"
  val button = ".govuk-button"
  val saveProgressButton = ".govuk-button--secondary"
  val detailsSummary = ".govuk-details__summary-text"
  val detailsContent = ".govuk-details__text"
  val hidden = ".hidden"
  val radio: String = "div.govuk-radios__item label"

}
