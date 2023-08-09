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

import config.FrontendAppConfig
import models.TransactorDetails
import models.api._
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import services.mocks.{MockApplicantDetailsService, MockTransactorDetailsService, MockVatRegistrationService}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException
import views.html.components.{bullets, link, p}

import scala.concurrent.Future

class UploadDocumentHintBuilderSpec extends VatRegSpec with MockApplicantDetailsService with MockVatRegistrationService with MockTransactorDetailsService {

  class Setup {
    val bullets: bullets = app.injector.instanceOf[bullets]
    val p: p = app.injector.instanceOf[p]
    val link: link = app.injector.instanceOf[link]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    object Builder extends UploadDocumentHintBuilder(
      mockApplicantDetailsService,
      vatRegistrationServiceMock,
      mockTransactorDetailsService,
      bullets,
      p,
      link
    )
  }

  implicit val request: Request[_] = FakeRequest()

  object ExpectedContent {
    val hintPrimary = "This must be a passport, photocard drivers licence or a national identity card. The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    def hintPrimary3pt(name: String) = s"This must be $name’s passport, photocard driving licence or their national identity card. The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val extraIdText = "This could be a:"
    def extraIdText3pt(name: String) = s"This could be any of the following for $name:"
    val bullet1 = "mortgage statement"
    val bullet2 = "lease or rental agreement"
    val bullet3 = "work permit or Visa"
    val bullet4 = "any correspondence from the Department for Work and Pensions confirming entitlement to benefits"
    val bullet5 = "recent utility bill"
    val bullet6 = "birth certificate"
    val thirdPartyBullet1 = "a mortgage statement"
    val thirdPartyBullet2 = "a lease or rental agreement"
    val thirdPartyBullet3 = "a work permit or Visa"
    val thirdPartyBullet4 = "any correspondence from the Department for Work and Pensions confirming entitlement to benefits"
    val thirdPartyBullet5 = "a recent utility bill"
    val thirdPartyBullet6 = "a birth certificate"
    val fileType = "The file must be a JPG, BMP, PNG, PDF, DOC, DOCX, XLS, XLSX, GIF or TXT."
    val VAT5LUploadMessage : String => String = link => s"Upload a completed $link (opens in new tab)"
  }

  "build" when {
    "called with PrimaryIdentityEvidence" must {
      "generate the correct hint for non Transactor" in new Setup {
        mockIsTransactor(Future.successful(false))

        val testHtml: Html = Html(ExpectedContent.hintPrimary)

        await(Builder.build(PrimaryIdentityEvidence)) mustBe testHtml
      }

      "generate the correct hint for Transactor" in new Setup {
        mockIsTransactor(Future.successful(true))
        mockGetApplicantDetails(currentProfile)(completeApplicantDetails)

        val testHtml: Html = Html(ExpectedContent.hintPrimary3pt(testPersonalDetails.fullName))

        await(Builder.build(PrimaryIdentityEvidence)) mustBe testHtml
      }
    }

    "called with ExtraIdentityEvidence" must {
      "generate the correct hint for non Transactor" in new Setup {
        mockIsTransactor(Future.successful(false))

        val testHtml: Html = HtmlFormat.fill(collection.immutable.Seq(
          p(Html(ExpectedContent.extraIdText)),
          bullets(
            ExpectedContent.bullet1,
            ExpectedContent.bullet2,
            ExpectedContent.bullet3,
            ExpectedContent.bullet4,
            ExpectedContent.bullet5,
            ExpectedContent.bullet6
          ),
          p(Html(ExpectedContent.fileType))
        ))

        await(Builder.build(ExtraIdentityEvidence)) mustBe testHtml
      }


      "generate the correct hint for Transactor" in new Setup {
        mockIsTransactor(Future.successful(true))
        mockGetApplicantDetails(currentProfile)(completeApplicantDetails)

        val testHtml: Html = HtmlFormat.fill(collection.immutable.Seq(
          p(Html(ExpectedContent.extraIdText3pt(testPersonalDetails.fullName))),
          bullets(
            ExpectedContent.thirdPartyBullet1,
            ExpectedContent.thirdPartyBullet2,
            ExpectedContent.thirdPartyBullet3,
            ExpectedContent.thirdPartyBullet4,
            ExpectedContent.thirdPartyBullet5,
            ExpectedContent.thirdPartyBullet6
          ),
          p(Html(ExpectedContent.fileType))
        ))

        await(Builder.build(ExtraIdentityEvidence)) mustBe testHtml
      }
    }

    "called with PrimaryTransactorIdentityEvidence" must {
      "generate the correct hint" in new Setup {
        mockGetTransactorDetails(currentProfile)(validTransactorDetails)

        val testHtml: Html = Html(ExpectedContent.hintPrimary3pt(validTransactorDetails.personalDetails.get.fullName))

        await(Builder.build(PrimaryTransactorIdentityEvidence)) mustBe testHtml
      }

      "fail if transactor name is missing" in new Setup {
        mockGetTransactorDetails(currentProfile)(TransactorDetails())

        intercept[InternalServerException](await(Builder.build(PrimaryTransactorIdentityEvidence))).message mustBe "User was missing Transactor Name"
      }
    }

    "called with ExtraTransactorIdentityEvidence" must {
      "generate the correct hint" in new Setup {
        mockGetTransactorDetails(currentProfile)(validTransactorDetails)

        val testHtml: Html = HtmlFormat.fill(collection.immutable.Seq(
          p(Html(ExpectedContent.extraIdText3pt(validTransactorDetails.personalDetails.get.fullName))),
          bullets(
            ExpectedContent.thirdPartyBullet1,
            ExpectedContent.thirdPartyBullet2,
            ExpectedContent.thirdPartyBullet3,
            ExpectedContent.thirdPartyBullet4,
            ExpectedContent.thirdPartyBullet5,
            ExpectedContent.thirdPartyBullet6
          ),
          p(Html(ExpectedContent.fileType))
        ))

        await(Builder.build(ExtraTransactorIdentityEvidence)) mustBe testHtml
      }

      "fail if transactor name is missing" in new Setup {
        mockGetTransactorDetails(currentProfile)(TransactorDetails())

        intercept[InternalServerException](await(Builder.build(ExtraTransactorIdentityEvidence))).message mustBe "User was missing Transactor Name"
      }
    }

    "called with an unsupported AttachmentType" must {
      "fail" in new Setup {
        intercept[InternalServerException](await(Builder.build(LetterOfAuthority))).message mustBe "Attachment Type not recognised"
      }
    }

    "called with chosen attachment type" must {
      "generate hint for document uploading form" in new Setup {

        val expectedHtml: (String, String) => Html = (href, msgSuffix) => HtmlFormat.fill(List(
          Html(messages("supplementary.uploadDocument.start")),
          link(href, messages(s"supplementary.uploadDocument.$msgSuffix"), isExternal = true)
        ))

        await(Builder.build(VAT5L)) mustBe expectedHtml(appConfig.vat5LLink, "vat5LLink")
        await(Builder.build(VAT51)) mustBe expectedHtml(appConfig.vat51Link, "vat51Link")
        await(Builder.build(VAT2)) mustBe expectedHtml(appConfig.vat2Link, "vat2Link")
        await(Builder.build(TaxRepresentativeAuthorisation)) mustBe expectedHtml(appConfig.vat1trLink, "vat1trLink")
      }
    }
  }
}
