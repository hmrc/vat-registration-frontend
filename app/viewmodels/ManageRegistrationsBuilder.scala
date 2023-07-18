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

package viewmodels

import common.enums.VatRegStatus
import featuretoggle.FeatureToggleSupport
import models.api.VatSchemeHeader
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.MessageDateFormat

import javax.inject.Inject

class ManageRegistrationsBuilder @Inject()(govukTag: GovukTag) extends FeatureToggleSupport {

  private final val GREY = "govuk-tag--grey"
  private final val YELLOW = "govuk-tag--yellow"
  private final val GREEN = "govuk-tag--green"

  def tableHead(implicit messages: Messages) = Seq(
    HeadCell(content = Text(messages("manageRegistrations.table.reference"))),
    HeadCell(content = Text(messages("manageRegistrations.table.dateCreated"))),
    HeadCell(content = Text(messages("manageRegistrations.table.status")))
  )

  def statusTag(registration: VatSchemeHeader)(implicit messages: Messages): Html = registration.status match {
    case VatRegStatus.draft if registration.requiresAttachments =>
      govukTag(Tag(Text(messages("manageRegistrations.attachmentsRequired")), classes = YELLOW))
    case VatRegStatus.draft | VatRegStatus.contact =>
      govukTag(Tag(Text(messages("manageRegistrations.draft")), classes = GREY))
    case VatRegStatus.submitted =>
      govukTag(Tag(Text(messages("manageRegistrations.submitted")), classes = GREEN))
  }

  def link(registration: VatSchemeHeader): String =
    controllers.routes.JourneyController.continueJourney(Some(registration.registrationId)).url

  def tableRows(registrations: List[VatSchemeHeader])(implicit messages: Messages): Seq[Seq[TableRow]] = {
    registrations.map(registration =>
      Seq(
        TableRow(content = HtmlContent(
          Html(
            s"""<a class="govuk-link" href="${link(registration)}">
             |    ${HtmlFormat.escape(registration.applicationReference.getOrElse(messages("manageRegistrations.noReference")))}
             |  </a>""".stripMargin
          )
        )),
        TableRow(content = Text(MessageDateFormat.format(registration.createdDate))),
        TableRow(content = HtmlContent(statusTag(registration)))
      )
    )
  }

}
