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

package models.view

import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}

object SummaryListRowUtils {
  def optSummaryListRowString(questionId: String,
                              optAnswer: Option[String],
                              optUrl: Option[String],
                              questionArgs: Seq[String] = Nil
                             )(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRow(
      questionId,
      optAnswer.map(answer => HtmlContent(HtmlFormat.escape(messages(answer)))),
      optUrl,
      questionArgs
    )
  }

  def optSummaryListRowBoolean(questionId: String,
                               optAnswer: Option[Boolean],
                               optUrl: Option[String],
                               questionArgs: Seq[String] = Nil
                              )(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRow(
      questionId,
      optAnswer.map {
        case true => HtmlContent(messages("app.common.yes"))
        case false => HtmlContent(messages("app.common.no"))
      },
      optUrl,
      questionArgs
    )

  def optSummaryListRowSeq(questionId: String,
                           optAnswers: Option[Seq[String]],
                           optUrl: Option[String],
                           questionArgs: Seq[String] = Nil
                          )(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRow(
      questionId,
      optAnswers.map(answers => HtmlContent(answers.map(answer => HtmlFormat.escape(messages(answer))).mkString("<br>"))),
      optUrl,
      questionArgs
    )

  def optSummaryListRow(questionId: String,
                                optAnswer: Option[HtmlContent],
                                optUrl: Option[String],
                                questionArgs: Seq[String] = Nil
                               )(implicit messages: Messages): Option[SummaryListRow] =
    optAnswer.map { answer =>
      SummaryListRow(
        key = Key(Text(messages(questionId, questionArgs: _*)), "govuk-!-width-one-half"),
        value = Value(answer),
        actions = optUrl.map { url =>
          Actions(
            items = Seq(
              ActionItem(
                href = url,
                content = Text(messages("app.common.change")),
                visuallyHiddenText = Some(messages(questionId, questionArgs: _*))
              )
            )
          )
        }
      )
    }

  def optSummaryListRowIndexed(questionId: String,
                               optAnswer: Option[HtmlContent],
                               optUrl: Option[String],
                               index: Int
                              )(implicit messages: Messages): Option[SummaryListRow] =
    optAnswer.map { answer =>
      val ordinal = messages(s"ordinals.$index")

      SummaryListRow(
        key = Key(Text(messages(questionId)), "govuk-!-width-one-half"),
        value = Value(answer),
        actions = optUrl.map { url =>
          Actions(
            items = Seq(
              ActionItem(
                href = url,
                content = Text(messages("app.common.change")),
                visuallyHiddenText = Some(messages(s"$questionId.hiddenText", ordinal))
              )
            )
          )
        }
      )
    }
}

object EligibilityJsonParser {

  private def eligibilitySummaryRowReads(changeUrl: String => String)(implicit messages: Messages): Reads[SummaryListRow] = {
    (
      (__ \ "question").read[String] and
        (__ \ "answer").read[String] and
        (__ \ "questionId").read[String].map(_.replaceAll("[-](?<=-).*", ""))
      ) ((question, answer, questionId) =>
      SummaryListRow(
        key = Key(Text(messages(question)), "govuk-!-width-one-half"),
        value = Value(Text(messages(answer))),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href = changeUrl(questionId),
              content = Text(messages("app.common.change")),
              visuallyHiddenText = Some(messages(question))
            )
          )
        ))
      )
    )
  }

  def eligibilitySummaryListReads(changeUrl: String => String)(implicit messages: Messages): Reads[SummaryList] = {
    (__ \ "sections").read(Reads.seq[Seq[SummaryListRow]](
      (__ \ "data").read(Reads.seq[SummaryListRow](eligibilitySummaryRowReads(changeUrl)))
    )).map(rowLists => SummaryList(rowLists.flatten))
  }

}