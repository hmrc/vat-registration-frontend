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

package services

import connectors.{AttachmentsConnector, RegistrationApiConnector}
import models.api.{AttachmentMethod, AttachmentType, Attachments}
import services.AttachmentsService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentsService @Inject()(val attachmentsConnector: AttachmentsConnector,
                                   registrationApiConnector: RegistrationApiConnector
                                  )(implicit ec: ExecutionContext) {

  def getAttachmentDetails(regId: String)(implicit hc: HeaderCarrier): Future[Option[Attachments]] =
    registrationApiConnector.getSection[Attachments](regId)

  def storeAttachmentDetails[T](regId: String, data: T)(implicit hc: HeaderCarrier): Future[Attachments] = {
    getAttachmentDetails(regId).flatMap { attachmentDetails =>
      val presentAttachmentDetails = attachmentDetails.getOrElse(Attachments())
      val updatedAttachmentDetails = data match {
        case answer: AttachmentMethod =>
          presentAttachmentDetails.copy(method = Some(answer), supplyVat1614h = None, supplyVat1614a = None, supplySupportingDocuments = None)
        case Supply1614AAnswer(answer) =>
          if (answer) {
            presentAttachmentDetails.copy(supplyVat1614a = Some(answer), supplyVat1614h = None)
          } else {
            presentAttachmentDetails.copy(supplyVat1614a = Some(answer))
          }
        case Supply1614HAnswer(answer) =>
          presentAttachmentDetails.copy(supplyVat1614a = Some(false), supplyVat1614h = Some(answer))
        case SupplySupportingDocumentsAnswer(answer) =>
          presentAttachmentDetails.copy(supplySupportingDocuments = Some(answer))
        case AdditionalPartnersDocumentsAnswer(answer) =>
          presentAttachmentDetails.copy(additionalPartnersDocuments = Some(answer))
      }

      registrationApiConnector.replaceSection[Attachments](regId, updatedAttachmentDetails)
    }

  }

  def getAttachmentList(regId: String)(implicit hc: HeaderCarrier): Future[List[AttachmentType]] =
    attachmentsConnector.getAttachmentList(regId)

  def getIncompleteAttachments(regId: String)(implicit hc: HeaderCarrier): Future[List[AttachmentType]] =
    attachmentsConnector.getIncompleteAttachments(regId)
}

object AttachmentsService {
  case class Supply1614AAnswer(answer: Boolean)

  case class Supply1614HAnswer(answer: Boolean)

  case class SupplySupportingDocumentsAnswer(answer: Boolean)

  case class AdditionalPartnersDocumentsAnswer(answer: Boolean)
}