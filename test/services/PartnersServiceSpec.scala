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

package services

import config.FrontendAppConfig
import connectors.mocks.MockPartnersConnector
import models.PartnerEntity
import models.api.{Individual, UkCompany}
import testHelpers.VatRegSpec

class PartnersServiceSpec extends VatRegSpec with MockPartnersConnector {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val service = new PartnersService(
    mockS4LService,
    mockPartnersConnector,
    mockSessionService
  )

  val testLeadPartner: PartnerEntity = PartnerEntity(
    testSoleTrader,
    Individual,
    isLeadPartner = true
  )
  val testPartner: PartnerEntity = PartnerEntity(
    testLimitedCompany,
    UkCompany,
    isLeadPartner = false
  )
  val testPartners: List[PartnerEntity] = List(testLeadPartner, testPartner)
  val index = 1

  "getAllPartners" should {
    "return a list of partners" in {
      mockGetAllPartners(testRegId)(testPartners)

      val result = service.getAllPartners(testRegId)

      await(result) mustBe testPartners
    }

    "return an empty list of partners" in {
      mockGetAllPartners(testRegId)(Nil)

      val result = service.getAllPartners(testRegId)

      await(result) mustBe Nil
    }
  }

  "getLeadPartner" should {
    "return a partner" in {
      mockGetAllPartners(testRegId)(testPartners)

      val result = service.getLeadPartner(testRegId)

      await(result) mustBe Some(testLeadPartner)
    }

    "return no partner" in {
      mockGetAllPartners(testRegId)(List(testPartner))

      val result = service.getLeadPartner(testRegId)

      await(result) mustBe None
    }
  }

  "getPartner" should {
    "return a partner" in {
      mockGetPartner(testRegId, index)(Some(testPartner))

      val result = service.getPartner(testRegId, index)

      await(result) mustBe Some(testPartner)
    }

    "return no partner" in {
      mockGetPartner(testRegId, index)(None)

      val result = service.getPartner(testRegId, index)

      await(result) mustBe None
    }
  }

  "upsertPartner" should {
    "upsert and return the partner" in {
      mockUpsertPartner(testRegId, index, testPartner)(testPartner)

      val result = service.upsertPartner(testRegId, index, testPartner)

      await(result) mustBe testPartner
    }
  }

  "deletePartner" should {
    "delete and return true" in {
      mockDeletePartner(testRegId, index)(response = true)

      val result = service.deletePartner(testRegId, index)

      await(result) mustBe true
    }
  }
}
