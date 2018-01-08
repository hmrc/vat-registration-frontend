/*
 * Copyright 2018 HM Revenue & Customs
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

package features.iv.services

import java.time.LocalDate

import common.enums.IVResult
import features.iv.models.{IVSetup, UserData}
import features.officer.controllers.routes
import features.officer.models.view.SecurityQuestionsView
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LVatLodgingOfficer
import models.view.vatLodgingOfficer.CompletionCapacityView
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.Inspectors
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class IdentityVerificationServiceSpec extends VatRegSpec with Inspectors with VatRegistrationFixture   {

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
      }
  }
  val validS4LLodgingOfficer = S4LVatLodgingOfficer(
    completionCapacity = Some(CompletionCapacityView("",Some(completionCapacity))),
    officerSecurityQuestions = Some(SecurityQuestionsView(LocalDate.of(2017,11,5),"nino",Some(officerName)))
  )

  "buildIVSetupData" should {
    "sucessfully return an IVSetup class" in new Setup  {

      val res = service.buildIVSetupData(validS4LLodgingOfficer)

      res mustBe IVSetup(
        "vat-registration-frontend",
        """vrfe""" + controllers.iv.routes.IdentityVerificationController.completedIVJourney().url,
        """vrfe/register-for-vat""" + """/ivFailure""",
        200,
        UserData("Bob","Bobbings","2017-11-05","nino"))
    }
  }

  "setupAndGetIVJourneyURL" should {
    "successfully return a String (link) with feature switch off" in new Setup(false) {
      when(mockVATFeatureSwitch.useIvStub).thenReturn(disabledFeatureSwitch)
      when(mockS4LService.fetchAndGet[S4LVatLodgingOfficer]).thenReturn(Future.successful(Some(validS4LLodgingOfficer)))
      when(mockS4LService.saveIv(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))))
      when(mockRegConnector.upsertVatLodgingOfficer(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(validLodgingOfficer))


      when(mockIdentityVerificationConnector.setupIVJourney(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(Json.parse(
        """{
          |"link":"foo/bar/and/wizz",
          |"journeyLink" : "lifeisAJourney"
          |}
        """.stripMargin)))

      val res = await(service.setupAndGetIVJourneyURL)

      res mustBe """foo/bar/and/wizz"""
    }

    "successfully return a (link) with feature switch on" in new Setup(false) {
      when(mockVATFeatureSwitch.useIvStub).thenReturn(enabledFeatureSwitch)
      when(mockS4LService.fetchAndGet[S4LVatLodgingOfficer]).thenReturn(Future.successful(Some(validS4LLodgingOfficer)))
      when(mockS4LService.saveIv(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))))
      when(mockRegConnector.upsertVatLodgingOfficer(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful(validLodgingOfficer))
      val res = await(service.setupAndGetIVJourneyURL)
      res.contains("""/test-iv-response/""") mustBe true
    }

    "return string to formername controller if cp.ivPassed is already true" in new Setup(true) {
      val res = await(service.setupAndGetIVJourneyURL)
      res mustBe features.officer.controllers.routes.OfficerController.showFormerName().url
    }
  }
  "getIVJourneyID" should {
    "successfully return a future option string of journeyID" in new Setup {
      when(mockS4LService.fetchIv(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(Some("fooJourneyID")))

      val res = await(service.getIVJourneyID)
      res mustBe Some("fooJourneyID")
    }
    "return None when S4L returns None" in new Setup {
      when(mockS4LService.fetchIv(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(None))
      val res = await(service.getIVJourneyID)
      res mustBe None
    }
  }
  "saveJourneyID" should {
    "successfully save journeyID" in new Setup{
      when(mockS4LService.saveIv(ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))))
      val res = await(service.saveJourneyID(Json.parse("""{"journeyLink":"/foo"}""")))
      res mustBe CacheMap("",Map("IVJourneyID" -> Json.toJson("foo")))
    }
  }
  "getJourneyIdFromJson" should {
    "return journeyID from JourneyUrl" in new Setup {
      val res = service.getJourneyIdFromJson(JsObject(Map("journeyLink" -> Json.toJson("""/foo/bar/wizzz"""))))
      res.as[String] mustBe "wizzz"
    }
  }
  "setIvStatus" should {
    "return true when iv is success" in new Setup {
      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(validHttpResponse))
      val res = await(service.setIvStatus(IVResult.Success))
      res mustBe Some(IVResult.Success)
    }
    "return None if a non valid http response is returned from updateIVStatus but iv result was success" in new Setup{
      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.failed(new Exception))
      val res = await(service.setIvStatus(IVResult.Success))
      res mustBe None
    }
    "return failedIV status if result was not a success" in new Setup {
      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(validHttpResponse))
      val res = await(service.setIvStatus(IVResult.FailedIV))
      res mustBe Some(IVResult.FailedIV)
    }
    "return None if a non valid http response is returned from updateIVstatus and iv result is not success" in new Setup {
      when(mockRegConnector.updateIVStatus(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.failed(new Exception))
      val res = await(service.setIvStatus(IVResult.FailedIV))
      res mustBe None
    }

  }
  "getJourneyIdAndJourneyOutcome" should {
    "return iv success if journeyID exists and iv status is success" in new Setup {
      when(mockS4LService.fetchIv(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some("fooJourneyID")))
      when(mockIdentityVerificationConnector.getJourneyOutcome(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(IVResult.Success))

      val res = await(service.getJourneyIdAndJourneyOutcome)
      res mustBe IVResult.Success
    }
  }
  "startIVJourney" should {
  "return JsObject with a link and a journey ID" in new Setup {
    val res = await(service.startIVJourney("foo"))
    res mustBe JsObject(Map("link" ->Json.toJson(controllers.test.routes.TestIVController.show("foo").url),"journeyLink" -> Json.toJson("""/foo""")))
  }}

}
