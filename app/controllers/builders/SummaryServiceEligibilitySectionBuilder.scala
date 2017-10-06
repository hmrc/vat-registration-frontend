/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.builders

import models.api.EligibilityQuestion._
import models.api._
import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig

case class SummaryServiceEligibilitySectionBuilder(vatServiceEligibility: Option[VatServiceEligibility] = None, useEligibilityFrontend: Boolean = true)
  extends SummarySectionBuilder with ServicesConfig {

  override val sectionId: String = "serviceCriteria"
  val vrefeConf: String = "vat-registration-eligibility-frontend"

  val haveNinoRow: SummaryRow = yesNoRow(
    "nino",
    vatServiceEligibility.flatMap(_.haveNino),
    if (useEligibilityFrontend) {
      getUrl(vrefeConf,"nino")
    } else {
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(HaveNinoQuestion.name)
    }
  )

  val doingBusinessAbroadRow: SummaryRow = yesNoRow(
    "businessAbroad",
    vatServiceEligibility.flatMap(_.doingBusinessAbroad),
    if (useEligibilityFrontend) {
      getUrl(vrefeConf, "business-abroad")
    }else{
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name)
    }
  )

  val doAnyApplyToYouRow: SummaryRow = yesNoRow(
    "doAnyApplyToYou",
    vatServiceEligibility.flatMap(_.doAnyApplyToYou),
    if (useEligibilityFrontend) {
      getUrl(vrefeConf, "change-status")
    }else{
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoAnyApplyToYouQuestion.name)
    }
  )

  val applyingForAnyOfRow: SummaryRow = yesNoRow(
    "applyingForAnyOf",
    vatServiceEligibility.flatMap(_.applyingForAnyOf),
    if (useEligibilityFrontend){
      getUrl(vrefeConf,"agric-flat-rate")
    } else {
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(ApplyingForAnyOfQuestion.name)
    }
  )

  val applyingForVatExemptionRow: SummaryRow = yesNoRow(
    "applyingForVatExemption",
    vatServiceEligibility.flatMap(_.applyingForVatExemption),
    getUrl(vrefeConf,"apply-exempt")
  )

  val applyingForVatExceptionRow: SummaryRow = yesNoRow(
    "applyingForVatException",
    vatServiceEligibility.flatMap(_.applyingForVatExemption),
    getUrl(vrefeConf,"apply-exempt")
  )

  val companyWillDoAnyOfRow: SummaryRow = yesNoRow(
    "companyWillDoAnyOf",
    vatServiceEligibility.flatMap(_.companyWillDoAnyOf),
    if (useEligibilityFrontend){
      getUrl(vrefeConf,"apply-for-any")
    } else {
      controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(CompanyWillDoAnyOfQuestion.name)
    }
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (haveNinoRow, vatServiceEligibility.exists(_.haveNino.isDefined)),
      (doingBusinessAbroadRow, vatServiceEligibility.exists(_.doingBusinessAbroad.isDefined)),
      (doAnyApplyToYouRow, vatServiceEligibility.exists(_.doAnyApplyToYou.isDefined)),
      (applyingForAnyOfRow, vatServiceEligibility.exists(_.applyingForAnyOf.isDefined)),
      (applyingForVatExemptionRow, useEligibilityFrontend && vatServiceEligibility.exists(_.applyingForVatExemption.isDefined)),
      (applyingForVatExceptionRow, useEligibilityFrontend && vatServiceEligibility.exists(_.applyingForVatExemption.isDefined)),
      (companyWillDoAnyOfRow, vatServiceEligibility.exists(_.companyWillDoAnyOf.isDefined))
    ),
    vatServiceEligibility.isDefined
  )

  def getUrl(serviceName: String, uri: String): Call = {
    val basePath = getConfString(s"$serviceName.www.host", "")
    val mainUri = getConfString(s"$serviceName.uri","/register-for-vat/")
    val serviceUri = getConfString(s"$serviceName.uris.$uri", throw new RuntimeException(s"Could not find config $serviceName.uris.$uri"))
    Call("Get", s"$basePath$mainUri$serviceUri")
  }
}
