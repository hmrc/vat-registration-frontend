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

package controllers.fileupload

import itutil.ControllerISpec
import models.api.{Attachment1614a, Attachment1614h, Attachments, LandPropertyOtherDocs}
import models.external.upscan.{Ready, UpscanDetails}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class Supply1614HControllerISpec extends ControllerISpec {

  val url: String = routes.Supply1614HController.show.url

  val testReference = "testReference"
  val test1614ADetails: UpscanDetails = UpscanDetails(attachmentType = Attachment1614a, reference = testReference, fileStatus = Ready)
  val test1614HDetails: UpscanDetails = UpscanDetails(attachmentType = Attachment1614h, reference = testReference, fileStatus = Ready)
  val testSupportingDocumentDetails: UpscanDetails = UpscanDetails(attachmentType = LandPropertyOtherDocs, reference = testReference, fileStatus = Ready)

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'Yes' pre-populated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614h = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe true
        document.select("input[value=false]").hasAttr("checked") mustBe false
      }
    }

    "return OK with 'No' pre-populated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614h = Some(false))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe false
        document.select("input[value=false]").hasAttr("checked") mustBe true
      }
    }
  }

  s"POST $url" must {
    "change old supplyVat1614a answer and uploaded file and redirect to Upload 1614H page if 'yes' is selected" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(true), supplyVat1614h = Some(false))))
        .registrationApi.replaceSection[Attachments](Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(true)))
        .upscanApi.fetchAllUpscanDetails(List(test1614ADetails))
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadOptionToTaxDocumentController.show.url)
    }

    "remove old supplyVat1614h uploaded file and redirect to Supply Supporting Documents page if 'no' is selected" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(true))))
        .registrationApi.replaceSection[Attachments](Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false)))
        .upscanApi.fetchAllUpscanDetails(List(test1614HDetails))
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.SupplySupportingDocumentsController.show.url)
    }

    "remove old supplyVat1614h uploaded file and redirect to Supply Supporting Documents page if 'no' is selected and there is already a supporting document uploaded" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(true))))
        .registrationApi.replaceSection[Attachments](Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false)))
        .upscanApi.fetchAllUpscanDetails(List(test1614HDetails, testSupportingDocumentDetails))
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }

}
