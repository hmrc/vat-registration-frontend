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

import config.FrontendAppConfig
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.components.{button, h1, p}
import views.html.layouts.layout

import scala.collection.JavaConverters._

class VatRegViewSpec extends PlaySpec with GuiceOneAppPerSuite with I18nSupport {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testCall: Call = Call("POST", "/test-url")

  object Selectors extends BaseSelectors

  val layout: layout = app.injector.instanceOf[layout]
  val h1: h1 = app.injector.instanceOf[h1]
  val p: p = app.injector.instanceOf[p]
  val button: button = app.injector.instanceOf[button]
  val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class ViewSetup(implicit val doc: Document) {
    case class Link(text: String, href: String)
    case class Details(summary: String, body: String)
    case class DateField(legend: String, hint: Option[String] = None)
    case class SummaryRow(label: String, answer: String, actions: Seq[Link])
    case class RadioGroup(legend: String, options: List[String], hint: Option[String] = None)

    implicit class ElementExtractor(elements: Elements) {
      def toList: List[Element] = elements.iterator.asScala.toList

      def headOption: Option[Element] = toList.headOption
    }

    implicit class SelectorDoc(doc: Document) extends BaseSelectors {
      private def selectText(selector: String): List[String] =
        doc.select(selector).asScala.toList.map(_.text())

      def heading: Option[String] = doc.select(h1).headOption.map(_.text)

      def headingLevel2(n: Int): Option[String] = doc.select(h2).toList.map(_.text).lift(n-1)

      def hasBackLink: Boolean = doc.select(".govuk-back-link").headOption.isDefined

      def errorSummary: Option[Element] = doc.select(".govuk-error-summary").headOption

      def errorSummaryLinks: List[Link] =
        doc.select(".govuk-error-summary__list a").toList
          .map(l => Link(l.text, l.attr("href")))

      def hasErrorSummary: Boolean = errorSummary.isDefined

      def summaryRow(n: Int): Option[SummaryRow] = {
        val key = doc.select(".govuk-summary-list__key").toList.lift(n).map(_.text)
        val value = doc.select(".govuk-summary-list__value").toList.lift(n).map(_.text)
        val actions = doc.select(".govuk-summary-list__actions").toList.lift(n).map(_.select("a").toList.map(l => Link(l.text, l.attr("href"))))

        for {
          keyValue <- key
          answerValue <- value
          actionsSeq <- actions
        } yield SummaryRow(keyValue, answerValue, actionsSeq)
      }

      def hintWithMultiple(n: Int): Option[String] = doc.select(multipleHints).toList.map(_.text).lift(n-1)

      def paras: List[String] = doc.select("main p").toList.map(_.text)

      def para(n: Int): Option[String] = doc.select(p).toList.map(_.text).lift(n-1)

      def panelIndentHeading(n: Int): Option[String] = selectText(panelHeading).lift(n)

      def panelIndent(n: Int): Option[String] = selectText("main .govuk-inset-text").lift(n - 1)

      def unorderedList(n: Int): List[String] = doc.select(s"main ul").toList.lift(n-1).map(_.children().eachText().asScala.toList).getOrElse(throw new Exception("List element does not exist"))

      def legend(n: Int): Option[String] = doc.select(legends).toList.map(_.text).lift(n-1)

      def bullet(n: Int): Option[String] = doc.select(bullets).toList.map(_.text).lift(n-1)

      def orderedList(n: Int): List[String] = doc.select(s"main ol").toList.lift(n-1).map(_.children().eachText().asScala.toList).getOrElse(throw new Exception("List element does not exist"))

      def link(n: Int): Option[Link] = doc.select(a).toList.map(l => Link(l.text, l.attr("href"))).lift(n - 1)

      def submitButton: Option[String] = doc.select(button).headOption.map(_.text)

      def hintText: Option[String] = doc.select(hint).headOption.map(_.text)

      def details: Option[Details] = {
        doc.select(detailsSummary).headOption map { summary =>
          Details(summary.text, doc.select(detailsContent).first.text)
        }
      }

      private def input(inputType: String, selector: String, selectorValue: String): Option[String] = {
        doc.select(s"input[type=$inputType][$selector=$selectorValue]").headOption.map { elem =>
          doc.select(s"label[for=${elem.id}]").first.text
        }
      }

      def dateInput(n: Int): Option[DateField] =
        doc.select(s"main .govuk-fieldset").asScala.toList.lift(n - 1).map { elem =>
          DateField(
            legend = elem.select(".govuk-fieldset__legend").asScala.toList.head.text(),
            hint = elem.select(".govuk-hint").asScala.toList.headOption.map(_.text)
          )
        }

      def radio(value: String): Option[String] = input("radio", "value", value)

      def radioGroup(n: Int): Option[RadioGroup] =
        doc.select("main .govuk-fieldset").toList.lift(n - 1).map { elem =>
          RadioGroup(
            legend = elem.select("legend").first().text(),
            options = elem.select("label").toList.map(_.text()),
            hint = elem.select(".govuk-hint").toList.headOption.map(_.text)
          )
        }

      def checkbox(value: String): Option[String] = input("checkbox", "value", value)

      def textBox(id: String): Option[String] = input("text", "id", id)

      def textArea(id: String): Option[String] =
        doc.select(s"textarea[id=$id]").headOption.map { elem =>
          doc.select(s"label[for=${elem.id}]").first.text
        }

      def warningText(n: Int): Option[String] =
        doc.select(warning).toList.map(_.text).lift(n-1)
    }
  }
}
