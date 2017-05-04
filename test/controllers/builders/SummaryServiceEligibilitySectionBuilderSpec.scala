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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.EligibilityQuestion._
import models.api.VatServiceEligibility
import models.view.SummaryRow

class SummaryServiceEligibilitySectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val defaultVatServiceEligibility = VatServiceEligibility(
                                    haveNino = Some(false),
                                    doingBusinessAbroad = Some(false),
                                    doAnyApplyToYou = Some(false),
                                    applyingForAnyOf = Some(false),
                                    companyWillDoAnyOf = Some(false)
                                  )


  "The section builder composing a financial details section" should {

    "with haveNinoRow render" should {

      " 'No' selected haveNinoRow " in {
        val builder = SummaryServiceEligibilitySectionBuilder()
        builder.haveNinoRow mustBe
          SummaryRow(
            "serviceCriteria.nino",
            "app.common.no",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(HaveNinoQuestion.name))
          )
      }


      " 'YES' selected for haveNinoRow" in {
        val vatServiceEligibility = defaultVatServiceEligibility.copy(haveNino= Some(true))
        val builder = SummaryServiceEligibilitySectionBuilder(Some(vatServiceEligibility))
        builder.haveNinoRow mustBe
          SummaryRow(
            "serviceCriteria.nino",
            "app.common.yes",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(HaveNinoQuestion.name))
          )
      }
    }

    "with doingBusinessAbroadRow render" should {

      " 'No' selected haveNinoRow " in {
        val builder = SummaryServiceEligibilitySectionBuilder()
        builder.doingBusinessAbroadRow mustBe
          SummaryRow(
            "serviceCriteria.businessAbroad",
            "app.common.no",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name))
          )
      }


      " 'YES' selected for doingBusinessAbroadRow" in {
        val vatServiceEligibility = defaultVatServiceEligibility.copy(doingBusinessAbroad = Some(true))
        val builder = SummaryServiceEligibilitySectionBuilder(Some(vatServiceEligibility))
        builder.doingBusinessAbroadRow mustBe
          SummaryRow(
            "serviceCriteria.businessAbroad",
            "app.common.yes",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoingBusinessAbroadQuestion.name))
          )
      }
    }

    "with doAnyApplyToYouRow render" should {

      " 'No' selected doAnyApplyToYouRow " in {
        val builder = SummaryServiceEligibilitySectionBuilder()
        builder.doAnyApplyToYouRow mustBe
          SummaryRow(
            "serviceCriteria.doAnyApplyToYou",
            "app.common.no",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoAnyApplyToYouQuestion.name))
          )
      }


      " 'YES' selected for doAnyApplyToYouRow" in {
        val vatServiceEligibility = defaultVatServiceEligibility.copy(doAnyApplyToYou = Some(true))
        val builder = SummaryServiceEligibilitySectionBuilder(Some(vatServiceEligibility))
        builder.doAnyApplyToYouRow mustBe
          SummaryRow(
            "serviceCriteria.doAnyApplyToYou",
            "app.common.yes",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(DoAnyApplyToYouQuestion.name))
          )
      }
    }

    "with applyingForAnyOfRow render" should {

      " 'No' selected applyingForAnyOfRow " in {
        val builder = SummaryServiceEligibilitySectionBuilder()
        builder.applyingForAnyOfRow mustBe
          SummaryRow(
            "serviceCriteria.applyingForAnyOf",
            "app.common.no",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(ApplyingForAnyOfQuestion.name))
          )
      }


      " 'YES' selected for applyingForAnyOfRow" in {
        val vatServiceEligibility = defaultVatServiceEligibility.copy(applyingForAnyOf = Some(true))
        val builder = SummaryServiceEligibilitySectionBuilder(Some(vatServiceEligibility))
        builder.applyingForAnyOfRow mustBe
          SummaryRow(
            "serviceCriteria.applyingForAnyOf",
            "app.common.yes",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(ApplyingForAnyOfQuestion.name))
          )
      }
    }


    "with companyWillDoAnyOfRow render" should {

      " 'No' selected companyWillDoAnyOfRow " in {
        val builder = SummaryServiceEligibilitySectionBuilder()
        builder.companyWillDoAnyOfRow mustBe
          SummaryRow(
            "serviceCriteria.companyWillDoAnyOf",
            "app.common.no",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(CompanyWillDoAnyOfQuestion.name))
          )
      }

      " 'YES' selected for companyWillDoAnyOfRow" in {
        val vatServiceEligibility = defaultVatServiceEligibility.copy(companyWillDoAnyOf = Some(true))
        val builder = SummaryServiceEligibilitySectionBuilder(Some(vatServiceEligibility))
        builder.companyWillDoAnyOfRow mustBe
          SummaryRow(
            "serviceCriteria.companyWillDoAnyOf",
            "app.common.yes",
            Some(controllers.vatEligibility.routes.ServiceCriteriaQuestionsController.show(CompanyWillDoAnyOfQuestion.name))
          )
      }
    }

    "with section generate" should {
      val builder = SummaryServiceEligibilitySectionBuilder(Some(defaultVatServiceEligibility))
      "a valid summary section" in {
        val builder = SummaryServiceEligibilitySectionBuilder(Some(defaultVatServiceEligibility))
        builder.section.id mustBe "vatServiceEligibility"
        builder.section.rows.length mustEqual 5
      }
    }

  }
}
