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

import featureswitch.core.config.FeatureSwitching
import models.Business
import models.api.VatScheme
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowIndexed}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import javax.inject.Inject


class OtherBusinessInvolvementSummaryBuilder @Inject()(govukSummaryList: GovukSummaryList,
                                                       h3: views.html.components.h3) extends FeatureSwitching {

  private val extraBottomMarginClass = "govuk-!-margin-bottom-9"
  private val noExtraClasses = ""

  // scalastyle:off
  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val obis = vatScheme.otherBusinessInvolvements.getOrElse(Nil)
    val business = vatScheme.business.getOrElse(throw
      new InternalServerException(s"[OtherBusinessInvolvementSummaryBuilder] Couldn't construct CYA due to missing section: 'Business details'")
    )

    HtmlFormat.fill(
      govukSummaryList(SummaryList(
        rows = List(
          otherBusinessInvolvements(business)
      ).flatten)) +:
      obis
        .zip(1 to obis.size)
        .map {
          case (obi, n) =>
            HtmlFormat.fill(List(
              h3(messages("cya.heading.obi", messages(s"ordinals.$n"))),
              govukSummaryList(SummaryList(
                classes = if (n < obis.size) extraBottomMarginClass else noExtraClasses,
                rows = List(
                  optSummaryListRowIndexed(
                    questionId = "obi.cya.businessName",
                    optAnswer = obi.businessName.map(name => HtmlContent(HtmlFormat.escape(name))),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(n).url),
                    index = n
                  ),
                  optSummaryListRowIndexed(
                    questionId = "obi.cya.hasVatNumber",
                    optAnswer = obi.hasVrn.map(hasVrn => if (hasVrn) HtmlContent(messages("app.common.yes")) else HtmlContent(messages("app.common.no"))),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.HaveVatNumberController.show(n).url),
                    index = n
                  ),
                  optSummaryListRowIndexed(
                    questionId ="obi.cya.vatNumber",
                    optAnswer = obi.vrn.map(HtmlContent(_)),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.CaptureVrnController.show(n).url),
                    index = n
                  ),
                  optSummaryListRowIndexed(
                    questionId ="obi.cya.hasUtr",
                    optAnswer = obi.hasUtr.map(hasUtr => if (hasUtr) HtmlContent(messages("app.common.yes")) else HtmlContent(messages("app.common.no"))),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.HasUtrController.show(n).url),
                    index = n
                  ),
                  optSummaryListRowIndexed(
                    questionId ="obi.cya.utr",
                    optAnswer = obi.utr.map(HtmlContent(_)),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.CaptureUtrController.show(n).url),
                    index = n
                  ),
                  optSummaryListRowIndexed(
                    questionId ="obi.cya.stillActivelyTrading",
                    optAnswer = obi.stillTrading.map(trading => if (trading) HtmlContent(messages("app.common.yes")) else HtmlContent(messages("app.common.no"))),
                    optUrl = Some(controllers.otherbusinessinvolvements.routes.OtherBusinessActivelyTradingController.show(n).url),
                    index = n
                  )
                ).flatten
              ))
            ))
        }
    )
  }

  private def otherBusinessInvolvements(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"obi.cya.involvement",
      business.otherBusinessInvolvement,
      Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)
    )

}
