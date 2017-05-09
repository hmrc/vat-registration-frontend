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

package services

import cats.data.OptionT
import connectors.IncorporationInformationConnector
import helpers.VatRegSpec
import models.external.CoHoRegisteredOfficeAddress
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.Inspectors
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class IncorporationInformationServiceSpec extends VatRegSpec with Inspectors {

  import cats.instances.future._

  private class Setup {
    implicit val headerCarrier = HeaderCarrier()
    val mockIIConnector = Mockito.mock(classOf[IncorporationInformationConnector])
    val service = new IncorporationInformationService(mockIIConnector)
  }

  "getOfficerAddressList" must {
    "call IncorporationInformationConnector to get a CoHoRegisteredOfficeAddress" in new Setup {

      val coHoRegisteredOfficeAddress =
        CoHoRegisteredOfficeAddress(
          premises = "premises",
          addressLine1 = "address_line_1",
          addressLine2 = Some("address_line_2"),
          locality = "locality",
          country = Some("country"),
          poBox = Some("po_box"),
          postalCode = Some("postal_code"),
          region = Some("region"))


      when(mockIIConnector.getRegisteredOfficeAddress("transactionId")).thenReturn(OptionT.pure(coHoRegisteredOfficeAddress))

      service.getOfficerAddressList().value returns Some(coHoRegisteredOfficeAddress)
    }
  }


}
