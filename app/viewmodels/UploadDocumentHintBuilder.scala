/*
 * Copyright 2026 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.CurrentProfile
import models.api._
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import services.{ApplicantDetailsService, TransactorDetailsService, VatRegistrationService}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off
@Singleton
class UploadDocumentHintBuilder @Inject()(applicantDetailsService: ApplicantDetailsService,
                                          vatRegistrationService: VatRegistrationService,
                                          transactorDetailsService: TransactorDetailsService,
                                          bullets: views.html.components.bullets,
                                          p: views.html.components.p,
                                          link: views.html.components.link) {

  def build(attachmentType: AttachmentType)(
    implicit appConfig: FrontendAppConfig, messages: Messages, hc: HeaderCarrier, cp: CurrentProfile, ec: ExecutionContext, request: Request[_]
  ): Future[Html] = {

    def conditionalApplicantName: Future[Option[String]] =
      vatRegistrationService.isTransactor.flatMap { isTransactor =>
        if (isTransactor) {
          applicantDetailsService.getApplicantDetails.map(_.personalDetails.map(_.fullName))
        } else {
          Future.successful(None)
        }
      }

    def transactorName: Future[String] = transactorDetailsService.getTransactorDetails
      .map(_.personalDetails.map(_.fullName).getOrElse(throw new InternalServerException("User was missing Transactor Name")))

    attachmentType match {
      case PrimaryIdentityEvidence =>
        conditionalApplicantName.map(_.fold(Html(messages("fileUpload.uploadDocument.hint.primary")))(dynamicPrimaryIdentityEvidenceHint))
      case ExtraIdentityEvidence =>
        conditionalApplicantName.map(_.fold(HtmlFormat.fill(collection.immutable.Seq(
          p(Html(messages("fileUpload.uploadDocument.hint.extraIdText"))),
          bullets(
            messages("fileUpload.uploadDocument.hint.extraIdBullet1"),
            messages("fileUpload.uploadDocument.hint.extraIdBullet2"),
            messages("fileUpload.uploadDocument.hint.extraIdBullet3"),
            messages("fileUpload.uploadDocument.hint.extraIdBullet4"),
            messages("fileUpload.uploadDocument.hint.extraIdBullet5"),
            messages("fileUpload.uploadDocument.hint.extraIdBullet6")
          ),
          p(Html(messages("fileUpload.uploadDocument.hint.fileType")))
        )))(dynamicExtraIdentityEvidenceHint))
      case PrimaryTransactorIdentityEvidence =>
        transactorName.map(dynamicPrimaryIdentityEvidenceHint)
      case ExtraTransactorIdentityEvidence =>
        transactorName.map(dynamicExtraIdentityEvidenceHint)
      case Attachment1614a =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat1614ALink, "VAT1614ALink"))
      case Attachment1614h =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat1614HLink, "VAT1614HLink"))
      case VAT51 =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat51Link, "vat51Link"))
      case VAT5L =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat5LLink, "vat5LLink"))
      case VAT2 =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat2Link, "vat2Link"))
      case TaxRepresentativeAuthorisation =>
        Future.successful(supplementDocumentUploadHint(appConfig.vat1trLink, "vat1trLink"))
      case _ =>
        throw new InternalServerException("Attachment Type not recognised")
    }
  }

  private def supplementDocumentUploadHint(href: String, msgSuffix: String)(implicit messages: Messages) = {
    HtmlFormat.fill(
      List(
        Html(messages("supplementary.uploadDocument.start")),
        link(href, messages(s"supplementary.uploadDocument.$msgSuffix"), isExternal = true)
      )
    )
  }

  private def dynamicPrimaryIdentityEvidenceHint(name: String)(implicit messages: Messages) =
    Html(messages("fileUpload.uploadDocument.hint.primary3pt", name))

  private def dynamicExtraIdentityEvidenceHint(name: String)(implicit messages: Messages) =
    HtmlFormat.fill(collection.immutable.Seq(
      p(Html(messages("fileUpload.uploadDocument.hint.extraIdText3pt", name))),
      bullets(
        messages("fileUpload.uploadDocument.hint.extraIdBullet1.3pt"),
        messages("fileUpload.uploadDocument.hint.extraIdBullet2.3pt"),
        messages("fileUpload.uploadDocument.hint.extraIdBullet3.3pt"),
        messages("fileUpload.uploadDocument.hint.extraIdBullet4.3pt"),
        messages("fileUpload.uploadDocument.hint.extraIdBullet5.3pt"),
        messages("fileUpload.uploadDocument.hint.extraIdBullet6.3pt")
      ),
      p(Html(messages("fileUpload.uploadDocument.hint.fileType")))
    ))

}
