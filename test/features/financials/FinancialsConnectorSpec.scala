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

package connectors

import java.time.LocalDate

import helpers.VatRegSpec
import models.api.VatFinancials
import config.WSHttp
import features.financials.models.{BankAccount, Returns, TurnoverEstimates}

import scala.language.postfixOps

class FinancialsConnectorSpec extends VatRegSpec {

  class Setup {
    val connector: FinancialsConnector = new RegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val vatRegElUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  val date = LocalDate.now()

  val turnoverEstimates = TurnoverEstimates(1234567890L)
  val returns           = Returns(reclaimVATOnMostReturns = true, "frequency", None, date)
  val bankAccount       = BankAccount("accountName", "SortCode", "AccountNumber")

  "Calling upsertVatFinancials" should {
    "return the correct VatResponse when the microservice completes and returns a VatFinancials model" in new Setup {
      mockHttpPATCH[VatFinancials, VatFinancials]("tst-url", validVatFinancials)
      connector.upsertVatFinancials("tstID", validVatFinancials) returns validVatFinancials
    }
    "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", forbidden)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith forbidden
    }
    "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", notFound)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith notFound
    }
    "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[VatFinancials, VatFinancials]("tst-url", internalServiceException)
      connector.upsertVatFinancials("tstID", validVatFinancials) failedWith internalServiceException
    }
  }

  "Calling getTurnoverEstimates" should {
    "return the correct response when the microservice completes and returns a TurnoverEstimates model" in new Setup {
      mockHttpGET[TurnoverEstimates]("tst-url", turnoverEstimates)
      connector.getTurnoverEstimates("tstID") returns turnoverEstimates
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[TurnoverEstimates]("tst-url", forbidden)
      connector.getTurnoverEstimates("tstID") failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[TurnoverEstimates]("tst-url", notFound)
      connector.getTurnoverEstimates("tstID") failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[TurnoverEstimates]("tst-url", internalServiceException)
      connector.getTurnoverEstimates("tstID") failedWith internalServiceException
    }
  }

  "Calling patchTurnoverEstimates" should {
    "return the correct response when the microservice completes and returns a TurnoverEstimates model" in new Setup {
      mockHttpPATCH[TurnoverEstimates, TurnoverEstimates]("tst-url", turnoverEstimates)
      connector.patchTurnoverEstimates("tstID", turnoverEstimates) returns turnoverEstimates
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TurnoverEstimates, TurnoverEstimates]("tst-url", forbidden)
      connector.patchTurnoverEstimates("tstID", turnoverEstimates) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[TurnoverEstimates, TurnoverEstimates]("tst-url", notFound)
      connector.patchTurnoverEstimates("tstID", turnoverEstimates) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[TurnoverEstimates, TurnoverEstimates]("tst-url", internalServiceException)
      connector.patchTurnoverEstimates("tstID", turnoverEstimates) failedWith internalServiceException
    }
  }

  "Calling getReturns" should {
    "return the correct response when the microservice completes and returns a Returns model" in new Setup {
      mockHttpGET[Returns]("tst-url", returns)
      connector.getReturns("tstID") returns returns
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", forbidden)
      connector.getReturns("tstID") failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", notFound)
      connector.getReturns("tstID") failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[Returns]("tst-url", internalServiceException)
      connector.getReturns("tstID") failedWith internalServiceException
    }
  }

  "Calling patchReturns" should {
    "return the correct response when the microservice completes and returns a Returns model" in new Setup {
      mockHttpPATCH[Returns, Returns]("tst-url", returns)
      connector.patchReturns("tstID", returns) returns returns
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", forbidden)
      connector.patchReturns("tstID", returns) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", notFound)
      connector.patchReturns("tstID", returns) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[Returns, Returns]("tst-url", internalServiceException)
      connector.patchReturns("tstID", returns) failedWith internalServiceException
    }
  }

  "Calling getBankAccount" should {
    "return the correct response when the microservice completes and returns a BankAccount model" in new Setup {
      mockHttpGET[BankAccount]("tst-url", bankAccount)
      connector.getBankAccount("tstID") returns bankAccount
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", forbidden)
      connector.getBankAccount("tstID") failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", notFound)
      connector.getBankAccount("tstID") failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[BankAccount]("tst-url", internalServiceException)
      connector.getBankAccount("tstID") failedWith internalServiceException
    }
  }

  "Calling patchBankAccount" should {
    "return the correct response when the microservice completes and returns a BankAccount model" in new Setup {
      mockHttpPATCH[BankAccount, BankAccount]("tst-url", bankAccount)
      connector.patchBankAccount("tstID", bankAccount) returns bankAccount
    }
    "return the correct response when a Forbidden response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", forbidden)
      connector.patchBankAccount("tstID", bankAccount) failedWith forbidden
    }
    "return a Not Found response when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", notFound)
      connector.patchBankAccount("tstID", bankAccount) failedWith notFound
    }
    "return the correct response when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedPATCH[BankAccount, BankAccount]("tst-url", internalServiceException)
      connector.patchBankAccount("tstID", bankAccount) failedWith internalServiceException
    }
  }

}
