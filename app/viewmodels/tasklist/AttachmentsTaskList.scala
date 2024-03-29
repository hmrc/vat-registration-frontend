/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.tasklist

import config.FrontendAppConfig
import models._
import models.api._
import play.api.i18n.Messages
import play.api.mvc.Request
import services.{AttachmentsService, BusinessService}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
object AttachmentsTaskList {

  def attachmentsRequiredRow(attachmentsService: AttachmentsService, businessService: BusinessService)(implicit profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext, request: Request[_], appConfig: FrontendAppConfig): Future[Option[TaskListRowBuilder]] =
    for {
      attachments <- attachmentsService.getAttachmentList(profile.registrationId)
      incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
    } yield if (attachments.nonEmpty) {
      Some(
        TaskListRowBuilder(
          messageKey = _ => resolveMessageKey(attachments),
          url = _ => _ => controllers.attachments.routes.DocumentsRequiredController.resolve.url,
          tagId = "attachmentsRequiredRow",
          checks = scheme => checks(scheme, incompleteAttachments),
          prerequisites = vatScheme => Seq(
            VatRegistrationTaskList.resolveFlatRateSchemeRow(vatScheme, businessService).getOrElse(
              VatRegistrationTaskList.vatReturnsRow(businessService)
            )
          )
        )
      )
    } else {
      None
    }

  def build(vatScheme: VatScheme, attachmentsRow: TaskListRowBuilder)(implicit messages: Messages): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.attachments.heading"),
      rows = Seq(attachmentsRow.build(vatScheme))
    )

  private def resolveMessageKey(attachments: List[AttachmentType]): String = {
    val documentsKey = "tasklist.attachments.identityDocuments"
    val formsKey = "tasklist.attachments.additionalForms"
    val documentAndFormsKey = "tasklist.attachments.documentsAndForms"
    attachments match {
      case Nil => throw new InternalServerException(s"[AttachmentsTaskList][resolveMessageKey] Missing attachments")
      case attachments =>
        val idDocuments = attachments.collect {
          case idDocument@(IdentityEvidence | TransactorIdentityEvidence) => idDocument
        }
        idDocuments match {
          case Nil => formsKey
          case list => if (list.size == attachments.size) {
            documentsKey
          } else {
            documentAndFormsKey
          }
        }
    }
  }

  private def checks(scheme: VatScheme, incompleteAttachments: List[AttachmentType]): Seq[Boolean] = {
    val checkPassed = Seq(true)
    val checkFailed = Seq(false)
    scheme.attachments.map {
      _.method match {
        case Some(Upload) =>
          incompleteAttachments match {
            case Nil => checkPassed
            case _ => checkPassed ++ checkFailed
          }
        case Some(Post) => checkPassed
        case _ => checkFailed
      }
    }.getOrElse(checkFailed)
  }
}
