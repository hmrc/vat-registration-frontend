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

package models.view

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsResult, JsValue, Reads, _}
import play.api.mvc.Call

case class Summary(sections: Seq[SummarySection])

case class SummarySection(id: String,
                          rows: Seq[(SummaryRow, Boolean)],
                          display: Boolean = true)


object SummaryRow {
  def apply(id: String, answerMessageKey: String, changeLink: Option[Call]): SummaryRow = {
    SummaryRow(id, Seq(answerMessageKey), changeLink)
  }

  def apply(id: String, answerMessageKey: String, changeLink: Option[Call], questionArgs: Seq[String]): SummaryRow = {
    SummaryRow(id, Seq(answerMessageKey), changeLink, questionArgs)
  }

  def mandatory(id: String, answer: Option[String], changeLink: Option[Call]): SummaryRow =
    SummaryRow(id, answer.getOrElse(missingData(id)), changeLink)

  def optional(id: String, answer: Option[String], changeLink: Option[Call]): SummaryRow =
    SummaryRow(id, answer.getOrElse(""), changeLink)

  private def missingData(name: String) =
    throw new Exception(s"[SummarySectionBuilder] Cannot build Check your answers page because '$name' is missing")

}

case class SummaryRow(id: String,
                      answerMessageKeys: Seq[String],
                      changeLink: Option[Call],
                      questionArgs: Seq[String] = Nil) {

  def hasValue: Boolean = this.answerMessageKeys.head.nonEmpty

}


object SummaryFromQuestionAnswerJson {
  private def summaryRowReads(implicit f: String => Call): Reads[(SummaryRow, Boolean)] = (
    (__ \ "question").read[String] and
      (__ \ "answer").read[String] and
      (__ \ "questionId").read[String].map(_.replaceAll("[-](?<=-).*", ""))
    ) ((ques, ans, id) => (SummaryRow(ques, ans, Some(f(id))), true))

  private def summarySectionReads(implicit f: String => Call): Reads[SummarySection] = new Reads[SummarySection] {
    override def reads(json: JsValue): JsResult[SummarySection] = {
      val sectionId = (json \ "title").validate[String]
      val summaryRows = (json \ "data").validate(Reads.seq[(SummaryRow, Boolean)](summaryRowReads))

      val seqErrors = sectionId.fold(identity, _ => Seq.empty) ++ summaryRows.fold(identity, _ => Seq.empty)
      if (seqErrors.nonEmpty) {
        JsError(seqErrors)
      } else {
        JsSuccess(SummarySection(
          id = sectionId.get,
          rows = summaryRows.get,
          true
        ))
      }
    }
  }

  def summaryReads(implicit f: String => Call): Reads[Summary] = (__ \ "sections").read[Seq[SummarySection]](Reads.seq[SummarySection](summarySectionReads)) map Summary.apply
}