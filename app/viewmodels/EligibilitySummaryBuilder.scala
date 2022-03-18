/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels

import config.FrontendAppConfig
import featureswitch.core.config.FeatureSwitching
import models.view.EligibilityJsonParser
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import javax.inject.{Inject, Singleton}

@Singleton
class EligibilitySummaryBuilder @Inject()(implicit appConfig: FrontendAppConfig) extends FeatureSwitching {

  private[viewmodels] def eligibilityCall(uri: String): String = s"${appConfig.eligibilityUrl}${appConfig.eligibilityQuestionUrl}?pageId=$uri"

  def build(json: JsValue, regId: String)(implicit messages: Messages): SummaryList = {
    json.validate[SummaryList](EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, messages("app.common.change"))).fold(
      errors => throw new Exception(s"[EligibilitySummaryBuilder] Json could not be parsed with errors: $errors with regId: $regId"),
      identity
    )
  }
}
