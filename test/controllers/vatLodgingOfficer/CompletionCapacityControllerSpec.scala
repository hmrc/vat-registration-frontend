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
import models.api.Officer
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

    when(mockPPService.getOfficerList()(any())).thenReturn(Seq(officer).pure)
    mockKeystoreCache[Seq[Officer]]("OfficerList", dummyCacheMap)

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[CompletionCapacityView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "Who is registering the company for VAT?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)
      save4laterReturnsNothing2[CompletionCapacityView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      callAuthorised(Controller.show()) {
        _ includesText "Who is registering the company for VAT?"
      }
    }
  }

  s"POST ${routes.CompletionCapacityController.submit()} with Empty data" should {

    "return 400" in {
      mockKeystoreFetchAndGet[Seq[Officer]]("OfficerList", None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.CompletionCapacityController.submit()} with selected completionCapacity but no completionCapacity list in keystore" should {

    "return 303" in {
      save4laterExpectsSave[CompletionCapacityView]()
      mockKeystoreFetchAndGet("OfficerList", Option.empty[Seq[Officer]])
      mockKeystoreCache[Officer](REGISTERING_OFFICER_KEY, dummyCacheMap)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> completionCapacity.name.id)
      )(_ redirectsTo s"$contextRoot/your-date-of-birth")

    }
  }

  s"POST ${routes.CompletionCapacityController.submit()} with selected completionCapacity" should {

    "return 303" in {
      val completionCapacityView = CompletionCapacityView(completionCapacity)
      when(mockPPService.getOfficerList()(any())).thenReturn(Seq(officer).pure)
      save4laterExpectsSave[CompletionCapacityView]()
      mockKeystoreFetchAndGet[Seq[Officer]]("OfficerList", Some(Seq(officer)))
      mockKeystoreCache[Officer](REGISTERING_OFFICER_KEY, dummyCacheMap)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> completionCapacity.name.id)
      )(_ redirectsTo s"$contextRoot/your-date-of-birth")

    }
  }

  s"POST ${routes.CompletionCapacityController.submit()} with 'someone else' selected" should {

    "redirect the user to 'ineligible' page" in {
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> "other")
      )(_ includesText "You can&#x27;t register for VAT using this service")
    }
  }

}
