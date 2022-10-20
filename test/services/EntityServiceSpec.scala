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
import models.Entity
import models.api.{Individual, ScotPartnership, UkCompany}
import services.EntityService.ScottishPartnershipName
import testHelpers.VatRegSpec

class EntityServiceSpec extends VatRegSpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val service = new EntityService(
    mockS4LService,
    mockSessionService,
    mockRegistrationApiConnector
  )

  val testLeadPartner: Entity = Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)
  val testPartner: Entity = Entity(Some(testLimitedCompany), UkCompany, Some(false), None, None, None, None)
  val testPartners: List[Entity] = List(testLeadPartner, testPartner)
  val index = 1

  "getAllPartners" should {
    "return a list of partners" in {
      mockGetListSection[Entity](testRegId, testPartners)
      val result = service.getAllEntities(testRegId)
      await(result) mustBe testPartners
    }

    "return an empty list of partners" in {
      mockGetListSection[Entity](testRegId, Nil)
      val result = service.getAllEntities(testRegId)
      await(result) mustBe Nil
    }
  }

  "getPartner" should {
    "return a partner" in {
      mockGetSection(testRegId, Some(testPartner))
      val result = service.getEntity(testRegId, 1)
      await(result) mustBe Some(testPartner)
    }

    "return no partner" in {
      mockGetSection(testRegId, None)
      val result = service.getEntity(testRegId, 1)
      await(result) mustBe None
    }
  }

  "upsertPartner" should {
    "upsert and return the partner" in {
      val updatedEntity = testPartner.copy(optScottishPartnershipName = Some(testCompanyName))

      mockGetSection(testRegId, Some(testPartner))
      mockReplaceSection(testRegId, updatedEntity)
      val result = service.upsertEntity(testRegId, index, ScottishPartnershipName(testCompanyName))
      await(result) mustBe updatedEntity
    }

    "upsert and clear model if new party type is different" in {
      val updatedEntity = testPartner.copy(details = None, partyType = ScotPartnership, optScottishPartnershipName = None)

      mockGetSection(testRegId, Some(testPartner))
      mockReplaceSection(testRegId, updatedEntity)
      val result = service.upsertEntity(testRegId, index, ScotPartnership)
      await(result) mustBe updatedEntity
    }
  }

  "deletePartner" should {
    "delete and return true" in {
      mockDeleteSection(testRegId)
      val result = service.deleteEntity(testRegId, index)
      await(result) mustBe true
    }
  }
}
