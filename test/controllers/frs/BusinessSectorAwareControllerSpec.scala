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

package controllers.frs

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.api.SicCode
import models.view.frs.BusinessSectorView
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier

class BusinessSectorAwareControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val testBusinessSectorView = BusinessSectorView("test business sector", 4.33)
  val limitedCostCompanyRate = BusinessSectorView("", 16.5)

  "BusinessSectorAwareController" should {
    "retrieve a businessSectorView if one is saved" in
      new BusinessSectorAwareController(ds, mockConfigConnector) {
        implicit val hc = HeaderCarrier()
        save4laterReturnsViewModel(testBusinessSectorView)()
        businessSectorView() returns testBusinessSectorView
      }

    "determine a businessSectorView if a limited cost company rate is saved" in
      new BusinessSectorAwareController(ds, mockConfigConnector) {
        implicit val hc = HeaderCarrier()
        save4laterReturnsViewModel(limitedCostCompanyRate)()
        save4laterReturnsViewModel(MainBusinessActivityView(SicCode("12345678", "description", "displayDetails")))()
        when(mockConfigConnector.getBusinessSectorDetails("12345678")).thenReturn(testBusinessSectorView)
        businessSectorView() returns testBusinessSectorView
      }

    "determine a businessSectorView if none is saved but main business activity is known" in
      new BusinessSectorAwareController(ds, mockConfigConnector) {
        implicit val hc = HeaderCarrier()
        when(mockVatRegistrationService.getVatScheme()).thenReturn(validVatScheme.pure)
        save4laterReturnsNoViewModel[BusinessSectorView]()
        save4laterReturnsViewModel(MainBusinessActivityView(SicCode("12345678", "description", "displayDetails")))()
        when(mockConfigConnector.getBusinessSectorDetails("12345678")).thenReturn(testBusinessSectorView)
        businessSectorView() returns testBusinessSectorView
      }


    "fail if no BusinessSectorView is saved and main business activity is not known" in
      new BusinessSectorAwareController(ds, mockConfigConnector) {
        implicit val hc = HeaderCarrier()
        when(mockVatRegistrationService.getVatScheme()).thenReturn(validVatScheme.pure)
        save4laterReturnsNoViewModel[BusinessSectorView]()
        save4laterReturnsNoViewModel[MainBusinessActivityView]()
        businessSectorView().failedWith(new IllegalStateException("foo"))
      }

  }
}
