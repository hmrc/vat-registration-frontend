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

package connectors


import config.FrontendAppConfig
import models.PartnerEntity
import models.api.{Individual, UkCompany}
import play.api.libs.json.Json
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class PartnersConnectorSpec extends VatRegSpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val connector = new PartnersConnector(mockHttpClient, appConfig)

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
      mockHttpGET(appConfig.partnersApiUrl(testRegId), HttpResponse(OK, Json.toJson(testPartners).toString))

      val result = connector.getAllPartners(testRegId)

      await(result) mustBe testPartners
    }

    "return an empty list of partners" in {
      mockHttpGET(appConfig.partnersApiUrl(testRegId), HttpResponse(NOT_FOUND, ""))

      val result = connector.getAllPartners(testRegId)

      await(result) mustBe Nil
    }

    "throw an exception" in {
      mockHttpGET(appConfig.partnersApiUrl(testRegId), HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.getAllPartners(testRegId)

      intercept[InternalServerException](await(result))
    }
  }

  "getPartner" should {
    "return a partner" in {
      mockHttpGET(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(OK, Json.toJson(testLeadPartner).toString))

      val result = connector.getPartner(testRegId, index)

      await(result) mustBe Some(testLeadPartner)
    }

    "return no partner" in {
      mockHttpGET(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(NOT_FOUND, ""))

      val result = connector.getPartner(testRegId, index)

      await(result) mustBe None
    }

    "throw an exception" in {
      mockHttpGET(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.getPartner(testRegId, index)

      intercept[InternalServerException](await(result))
    }
  }

  "upsertPartner" should {
    "upsert and return the partner" in {
      mockHttpPUT(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(CREATED, Json.toJson(testLeadPartner).toString))

      val result = connector.upsertPartner(testRegId, index, testLeadPartner)

      await(result) mustBe testLeadPartner
    }

    "throw an exception" in {
      mockHttpPUT(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.upsertPartner(testRegId, index, testLeadPartner)

      intercept[InternalServerException](await(result))
    }
  }

  "deletePartner" should {
    "delete and return true" in {
      mockHttpDELETE(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(NO_CONTENT, ""))

      val result = connector.deletePartner(testRegId, index)

      await(result) mustBe true
    }

    "throw an exception" in {
      mockHttpDELETE(s"${appConfig.partnersApiUrl(testRegId)}/$index", HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.deletePartner(testRegId, index)

      intercept[InternalServerException](await(result))
    }
  }
}
