/*
 * Copyright 2020 HM Revenue & Customs
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

import fixtures.LodgingOfficerFixtures
import models.external.Name
import models.view.LodgingOfficer
import models.{IVResult, IVSetup, UserData}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.Inspectors
import play.api.libs.json.{JsObject, JsValue, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HttpResponse, Upstream5xxResponse}

import scala.concurrent.Future

class IVServiceSpec extends VatRegSpec with Inspectors with LodgingOfficerFixtures   {

   class Setup(ivPassed: Boolean = true) {
      implicit val cp = currentProfile(Some(ivPassed))
      val service = new IVService {
        override val vrfeBaseUrl = """vrfe"""
        override val vrfeBaseUri: String = "/register-for-vat"
        override val ORIGIN = "vat-registration-frontend"
        override val s4lService = mockS4LService
        override val ivConnector = mockIdentityVerificationConnector
        override val vatRegFeatureSwitch = mockVATFeatureSwitch
        override val vatRegistrationConnector = mockRegConnector
        override val keystoreConnector = mockKeystoreConnector
      }
  }

  val applicant = Name(forename = Some("First"), otherForenames = None, surname = "Last")

  "buildIVSetupData" should {
    "sucessfully return an IVSetup class" in new Setup  {
      val res = service.buildIVSetupData(validPartialLodgingOfficer, applicant, "ZZ987654A")

      res mustBe IVSetup(
        "vat-registration-frontend",
        """vrfe""" + controllers.routes.IdentityVerificationController.completedIVJourney().url,
        """vrfe/register-for-vat""" + """/ivFailure""",
        200,
        UserData("First","Last","1998-07-12","ZZ987654A"))
    }
  }

  "setupAndGetIVJourneyURL" should {
    val jsonPartialLodgingOfficer = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "First",
         |    "middle": "Middle",
         |    "last": "Last"
         |  },
         |  "dob": "1998-07-12",
         |  "nino": "ZZ987654A"
         |}
       """.stripMargin)

    "successfully return a String (link) with feature switch off" in new Setup(false) {
      when(mockVATFeatureSwitch.useIvStub).thenReturn(disabledFeatureSwitch)
      when(mockS4LService.save(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))))
      when(mockRegConnector.getLodgingOfficer(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(jsonPartialLodgingOfficer)))

      when(mockIdentityVerificationConnector.setupIVJourney(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(Json.parse(
        """{
          |"link":"foo/bar/and/wizz",
          |"journeyLink" : "lifeisAJourney"
          |}
        """.stripMargin)))

      val res = service.setupAndGetIVJourneyURL returns """foo/bar/and/wizz"""
    }

    "successfully return a (link) with feature switch on" in new Setup(false) {
      when(mockVATFeatureSwitch.useIvStub).thenReturn(enabledFeatureSwitch)
      when(mockS4LService.fetchAndGetNoAux[LodgingOfficer](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(validPartialLodgingOfficer)))
      when(mockS4LService.save(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))))
      when(mockRegConnector.getLodgingOfficer(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(jsonPartialLodgingOfficer)))

      val res = await(service.setupAndGetIVJourneyURL)
      res.contains("""/test-iv-response/""") mustBe true
    }

    "return string to formername controller if cp.ivPassed is already true" in new Setup(true) {
      service.setupAndGetIVJourneyURL returns controllers.routes.OfficerController.showFormerName().url
    }
  }

  "getJourneyIdFromJson" should {
    "return journeyID from JourneyUrl" in new Setup {
      val res = service.getJourneyIdFromJson(JsObject(Map("journeyLink" -> Json.toJson("""/foo/bar/wizzz"""))))
      res mustBe "wizzz"
    }
  }

  "fetchAndSaveIVStatus" should {
    "return iv success if journeyID exists and iv status is success" in new Setup {
      when(mockS4LService.fetchAndGet[String](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("fooJourneyID")))

      when(mockIdentityVerificationConnector.getJourneyOutcome(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.Success))

      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      when(mockKeystoreConnector.cache(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      service.fetchAndSaveIVStatus returns IVResult.Success
    }

    "return failedIV if journeyID exists and iv status is not a success" in new Setup {
      when(mockS4LService.fetchAndGet[String](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("fooJourneyID")))

      when(mockIdentityVerificationConnector.getJourneyOutcome(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.FailedIV))

      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      when(mockKeystoreConnector.cache(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      service.fetchAndSaveIVStatus returns IVResult.FailedIV
    }

    "return an exception if the IV Status update failed in backend" in new Setup {
      when(mockS4LService.fetchAndGet[String](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("fooJourneyID")))

      when(mockIdentityVerificationConnector.getJourneyOutcome(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(IVResult.Success))

      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse("Error occurred while updating IV Status", 500, 500)))

      an[Exception] shouldBe thrownBy(await(service.fetchAndSaveIVStatus))
    }
  }

  "startIVStubJourney" should {
    "return a Json object with a link and a journey ID" in new Setup {
      val res = service.startIVStubJourney("foo")
      res mustBe Json.obj(
        "link" -> controllers.test.routes.TestIVController.show("foo").url,
        "journeyLink" -> "/foo"
      )
    }
  }

  "getIVStatus" should {

    def generateOfficerJson(ivStatus: Option[Boolean]): Option[JsValue] =
      Some(Json.obj("name" -> Json.obj("first" -> "First", "middle" -> "Middle", "last" -> "Last"), "dob" -> "1998-7-12", "nino" -> "fakenino") ++
        ivStatus.fold(Json.obj())(status => Json.obj("ivPassed" -> status)))


    "return none if no lodging officer exists" in new Setup {
      when(mockRegConnector.getLodgingOfficer(ArgumentMatchers.eq("testRegId"))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option.empty[JsValue]))

      service.getIVStatus("testRegId") returns None
    }

    "return Some(true) if an IVstatus is passed" in new Setup {
      when(mockRegConnector.getLodgingOfficer(ArgumentMatchers.eq("testRegId"))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(generateOfficerJson(Some(true))))

      service.getIVStatus("testRegId") returns Some(true)
    }

    "return Some(false) if an IVstatus is failed" in new Setup {
      when(mockRegConnector.getLodgingOfficer(ArgumentMatchers.eq("testRegId"))(ArgumentMatchers.any()))
        .thenReturn(Future.successful(generateOfficerJson(Some(false))))

      service.getIVStatus("testRegId") returns Some(false)
    }
  }
}
