/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.vatLodgingOfficer

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys.REGISTERING_OFFICER_KEY
import models.external.Officer
import models.view.vatLodgingOfficer.CompletionCapacityView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

class CompletionCapacityControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends CompletionCapacityController(ds)(
    mockS4LService,
    mockVatRegistrationService,
    mockPPService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())

  s"GET ${routes.CompletionCapacityController.show()}" should {

    "return HTML when there's no view in S4L" in {
      save4laterReturnsNoViewModel[CompletionCapacityView]()
      when(mockPPService.getOfficerList()(any())).thenReturn(Seq(officer).pure)
      mockKeystoreCache[Seq[Officer]]("OfficerList", dummyCacheMap)

      callAuthorised(Controller.show()) {
        _ includesText "Who is registering the company for VAT?"
      }
    }

    "return HTML when view is present in S4L" in {
      save4laterReturnsViewModel(CompletionCapacityView("id", Some(completionCapacity)))()
      when(mockPPService.getOfficerList()(any())).thenReturn(Seq(officer).pure)
      mockKeystoreCache[Seq[Officer]]("OfficerList", dummyCacheMap)

      callAuthorised(Controller.show()) {
        _ includesText "Who is registering the company for VAT?"
      }
    }
  }

  s"POST ${routes.CompletionCapacityController.submit()}" should {

    "return 400 with Empty data" in {
      mockKeystoreFetchAndGet[Seq[Officer]]("OfficerList", None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 303  with selected completionCapacity but no completionCapacity list in keystore" in {
      save4laterExpectsSave[CompletionCapacityView]()
      mockKeystoreFetchAndGet("OfficerList", Option.empty[Seq[Officer]])
      mockKeystoreCache[Officer](REGISTERING_OFFICER_KEY, dummyCacheMap)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> completionCapacity.name.id)
      )(_ redirectsTo s"$contextRoot/pass-security")

    }

    "return 303 with selected completionCapacity" in {
      val completionCapacityView = CompletionCapacityView(completionCapacity)
      when(mockPPService.getOfficerList()(any())).thenReturn(Seq(officer).pure)
      save4laterExpectsSave[CompletionCapacityView]()
      mockKeystoreFetchAndGet[Seq[Officer]]("OfficerList", Some(Seq(officer)))
      mockKeystoreCache[Officer](REGISTERING_OFFICER_KEY, dummyCacheMap)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> completionCapacity.name.id)
      )(_ redirectsTo s"$contextRoot/pass-security")

    }
  }

}
