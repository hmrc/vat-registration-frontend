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

package models.view

import models.api._
import models.view.EligibilityJsonParser.EligibilityPageIds._
import play.api.i18n.Messages
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.MessageDateFormat

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

  //scalastyle:off
  def reads(changeUrl: String => String)(implicit messages: Messages): Reads[SummaryList] = Reads { json =>
    val answerMap = json.validate[Map[String, JsValue]]

    def isOptDate(js: JsValue) = (js \ "value").asOpt[JsValue].isDefined

    def isDate(js: JsValue) = (js \ "date").asOpt[JsValue].isDefined

    answerMap.map { answers =>
      val regReason = answers(registrationReason).as[String]

      SummaryList(reorder(answers).flatMap {
        case (questionId, answerJson) if isOptDate(answerJson) =>
          val optDateAnswer = (answerJson \ "optionalData").asOpt[String]
            .map(date => LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE))
            .map(MessageDateFormat.format)
          val yesNoAnswer = lookupAnswer(questionId, (answerJson \ "value").as[JsValue])

          val question = lookupQuestion(questionId, regReason)
          val answer = optDateAnswer.map(date => {
            s"$yesNoAnswer - ${messages("eligibility.answer.thresholdOver.on")} $date"
          }).getOrElse(yesNoAnswer)

          Seq(eligibilitySummaryRow(changeUrl, questionId, question, answer))
        case (questionId, answerJson) if isDate(answerJson) =>
          val question = lookupQuestion(questionId, regReason)
          val dateAnswer = MessageDateFormat.format(LocalDate.parse((answerJson \ "date").as[String], DateTimeFormatter.ISO_LOCAL_DATE))

          Seq(eligibilitySummaryRow(changeUrl, questionId, question, dateAnswer))
        case (questionId, answerJson) if questionId == businessEntity =>
          val isPartnership = Seq(Partnership, LtdPartnership, ScotPartnership, ScotLtdPartnership, LtdLiabilityPartnership)
            .contains(answerJson.as[PartyType])
          val optPartnershipAnswer = if (isPartnership) {
            Seq(eligibilitySummaryRow(changeUrl, businessEntityPartnership, lookupQuestion(businessEntityPartnership, regReason), lookupAnswer(businessEntityPartnership, answerJson)))
          } else {
            Nil
          }

          val question = lookupQuestion(questionId, regReason)
          val answer = if (isPartnership) lookupAnswer(questionId, JsString("partnership")) else lookupAnswer(questionId, answerJson)

          Seq(eligibilitySummaryRow(changeUrl, questionId, question, answer)) ++ optPartnershipAnswer
        case (questionId, answerJson) =>
          val question = lookupQuestion(questionId, regReason)
          val answer = lookupAnswer(questionId, answerJson)

          Seq(eligibilitySummaryRow(changeUrl, questionId, question, answer))
      })
    }
  }
  //scalastyle:on

  private def lookupQuestion(questionId: String, regReason: String)(implicit messages: Messages): String = {
    val togcColeSuffix = if (togcColeQuestionIds.contains(questionId)) s".$regReason" else ""
    messages(s"eligibility.question.$questionId" + togcColeSuffix)
  }

  private def lookupAnswer(questionId: String, json: JsValue)(implicit messages: Messages): String =
    json.validate[String].fold(
      invalid => messages(s"eligibility.answer.${json.as[Boolean]}"),
      valid => {
        val key = s"eligibility.answer.$questionId.$valid"
        if (messages.isDefinedAt(key)) messages(s"eligibility.answer.$questionId.$valid") else valid
      }
    )

  private def eligibilitySummaryRow(changeUrl: String => String, questionId: String, question: String, answer: String)(implicit messages: Messages): SummaryListRow = {
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
  }

  private def reorder(answers: Map[String, JsValue]): List[(String, JsValue)] =
    List(
      (fixedEstablishment, answers.get(fixedEstablishment)),
      (businessEntity, answers.get(businessEntity)),
      (agriculturalFlatRateScheme, answers.get(agriculturalFlatRateScheme)),
      (internationalActivities, answers.get(internationalActivities)),
      (registeringBusiness, answers.get(registeringBusiness)),
      (registrationReason, answers.get(registrationReason)),
      (dateOfBusinessTransfer, answers.get(dateOfBusinessTransfer)),
      (previousBusinessName, answers.get(previousBusinessName)),
      (vatNumber, answers.get(vatNumber)),
      (keepOldVrn, answers.get(keepOldVrn)),
      (termsAndConditions, answers.get(termsAndConditions)),
      (thresholdInTwelveMonths, answers.get(thresholdInTwelveMonths)),
      (thresholdNextThirtyDays, answers.get(thresholdNextThirtyDays)),
      (thresholdPreviousThirtyDays, answers.get(thresholdPreviousThirtyDays)),
      (vatRegistrationException, answers.get(vatRegistrationException)),
      (voluntaryRegistration, answers.get(voluntaryRegistration)),
      (taxableSuppliesInUk, answers.get(taxableSuppliesInUk)),
      (thresholdTaxableSupplies, answers.get(thresholdTaxableSupplies))
    ).collect { case (id, Some(value)) => (id, value) }

  object EligibilityPageIds {
    val thresholdInTwelveMonths = "thresholdInTwelveMonths"
    val thresholdNextThirtyDays = "thresholdNextThirtyDays"
    val thresholdPreviousThirtyDays = "thresholdPreviousThirtyDays"
    val thresholdTaxableSupplies = "thresholdTaxableSupplies"
    val vatRegistrationException = "vatRegistrationException"
    val voluntaryRegistration = "voluntaryRegistration"
    val dateOfBusinessTransfer = "dateOfBusinessTransfer"
    val previousBusinessName = "previousBusinessName"
    val keepOldVrn = "keepOldVrn"
    val vatNumber = "vatNumber"
    val termsAndConditions = "termsAndConditions"
    val fixedEstablishment = "fixedEstablishment"
    val businessEntity = "businessEntity"
    val businessEntityPartnership = "businessEntityPartnership"
    val agriculturalFlatRateScheme = "agriculturalFlatRateScheme"
    val internationalActivities = "internationalActivities"
    val registeringBusiness = "registeringBusiness"
    val registrationReason = "registrationReason"
    val taxableSuppliesInUk = "taxableSuppliesInUk"

    val togcColeQuestionIds = Seq(dateOfBusinessTransfer, previousBusinessName, vatNumber, keepOldVrn, termsAndConditions)
  }
}