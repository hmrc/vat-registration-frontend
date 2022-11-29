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

import models.OtherBusinessInvolvement
import models.view.SummaryListRowUtils.{optSummaryListRowIndexed, optSummaryListRowBoolean}
import play.api.i18n.{Lang, MessagesApi}
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.components.h3

class OtherBusinessInvolvementSummaryBuilderSpec extends VatRegSpec {

  val builder = app.injector.instanceOf[OtherBusinessInvolvementSummaryBuilder]
  val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
  val h3 = app.injector.instanceOf[h3]
  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

  val testVrn = "testVrn"

  val testObi = OtherBusinessInvolvement(
    businessName = Some(testCompanyName),
    hasVrn = Some(true),
    vrn = Some(testVrn),
    stillTrading = Some(true)
  )

  val testObiSection = List(testObi, testObi)

  "The OBJ builder" when {

    "there are no OBIs" must {
      "return only obi involvement answer" in {
        val res = builder.build(emptyVatScheme.copy(
          business = Some(validBusiness.copy(otherBusinessInvolvement = Some(false)))
        ))

        res mustBe HtmlFormat.fill(List(
          govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowBoolean(s"obi.cya.involvement", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url))
            ).flatten
          ))
        ))
      }
    }

    "there are OBIs" must {
      "show the user has Other Business Involvements (OBIs)" in {
        val scheme = emptyVatScheme.copy(
          business = Some(validBusiness.copy(otherBusinessInvolvement = Some(true))),
          otherBusinessInvolvements = Some(testObiSection)
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(
          govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowBoolean(s"obi.cya.involvement", Some(true), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url))
            ).flatten
          )),
          h3("First other business involvement"),
          govukSummaryList(SummaryList(
            classes = "govuk-!-margin-bottom-9",
            rows = List(
              optSummaryListRowIndexed("obi.cya.businessName", Some(HtmlContent(testCompanyName)), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1).url), 1),
              optSummaryListRowIndexed("obi.cya.hasVatNumber", Some(HtmlContent("Yes")), Some(controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(1).url), 1),
              optSummaryListRowIndexed("obi.cya.vatNumber", Some(HtmlContent(testVrn)), Some(controllers.otherbusinessinvolvements.routes.CaptureVrnController.show(1).url), 1),
              optSummaryListRowIndexed("obi.cya.stillActivelyTrading", Some(HtmlContent("Yes")), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(1).url), 1)
            ).flatten
          )),
          h3("Second other business involvement"),
          govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowIndexed("obi.cya.businessName", Some(HtmlContent(testCompanyName)), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(2).url), 2),
              optSummaryListRowIndexed("obi.cya.hasVatNumber", Some(HtmlContent("Yes")), Some(controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(2).url), 2),
              optSummaryListRowIndexed("obi.cya.vatNumber", Some(HtmlContent(testVrn)), Some(controllers.otherbusinessinvolvements.routes.CaptureVrnController.show(2).url), 2),
              optSummaryListRowIndexed("obi.cya.stillActivelyTrading", Some(HtmlContent("Yes")), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(2).url), 2)
            ).flatten
          ))
        ))
      }
    }
  }

}
