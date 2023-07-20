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
import models.api.{Attachments, LandPropertyOtherDocs, VAT5L}
import models.external.upscan.{Ready, UpscanDetails}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class SupplySupportingDocumentsControllerISpec extends ControllerISpec {

  val url: String = routes.SupplySupportingDocumentsController.show.url

  val testReference = "testReference"
  val testReference2 = "testReference2"
  val testReference3 = "testReference3"
  val testDetails: UpscanDetails = UpscanDetails(attachmentType = LandPropertyOtherDocs, reference = testReference, fileStatus = Ready)
  val testDetails2: UpscanDetails = UpscanDetails(attachmentType = LandPropertyOtherDocs, reference = testReference2, fileStatus = Ready)
  val testDetails3: UpscanDetails = UpscanDetails(attachmentType = VAT5L, reference = testReference3, fileStatus = Ready)

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
      }
    }

    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplySupportingDocuments = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val document = Jsoup.parse(res.body)
        document.select("input[value=true]").hasAttr("checked") mustBe true
        document.select("input[value=false]").hasAttr("checked") mustBe false
      }
    }

    "return OK with 'No' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplySupportingDocuments = Some(false))))

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
    "redirect to Upload Supporting Document page if 'yes' is selected" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false))))
        .registrationApi.replaceSection[Attachments](Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false), supplySupportingDocuments = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadSupportingDocumentController.show.url)
    }

    "remove old supplySupportingDocuments files and redirect to Tasklist page if 'no' is selected" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false))))
        .registrationApi.replaceSection[Attachments](Attachments(supplyVat1614a = Some(false), supplyVat1614h = Some(false), supplySupportingDocuments = Some(false)))
        .upscanApi.fetchAllUpscanDetails(List(testDetails, testDetails2, testDetails3))
        .upscanApi.deleteUpscanDetails(testRegId, testReference)
        .upscanApi.deleteUpscanDetails(testRegId, testReference2)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }

}
