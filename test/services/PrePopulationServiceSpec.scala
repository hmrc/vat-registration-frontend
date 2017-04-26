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

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

import cats.data.OptionT
import connectors.{KeystoreConnector, PPConnector}
import helpers.VatRegSpec
import models.external.{AccountingDetails, CorporationTaxRegistration}
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.Inspectors
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class PrePopulationServiceSpec extends VatRegSpec with Inspectors {

  import cats.instances.future._

  private class Setup {

    implicit def toOptionT(d: LocalDate): OptionT[Future, CorporationTaxRegistration] =
      OptionT.pure(CorporationTaxRegistration(Some(AccountingDetails("", Some(d.format(ofPattern("yyyy-MM-dd")))))))

    val none: OptionT[Future, CorporationTaxRegistration] = OptionT.none

    implicit val headerCarrier = HeaderCarrier()
    val mockPPConnector = Mockito.mock(classOf[PPConnector])
    val service = new PrePopulationService(mockPPConnector) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      mockFetchRegId()
    }

  }

  "CT Active Date" must {

    "be a LocalDate" in new Setup {
      val expectedDate = LocalDate.of(2017, 4, 24)
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any())).thenReturn(expectedDate)
      service.getCTActiveDate.value returns Some(expectedDate)
    }

    "be None" in new Setup {
      when(mockPPConnector.getCompanyRegistrationDetails(any())(any(), any())).thenReturn(none)
      service.getCTActiveDate().isEmpty returns true
    }

  }


}
