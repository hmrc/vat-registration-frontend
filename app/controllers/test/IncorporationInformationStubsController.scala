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

package controllers.test

import javax.inject.Inject

import connectors.VatRegistrationConnector
import connectors.test.TestRegistrationConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, RegistrationService, SessionProfile}

class IncorporationInformationStubsController @Inject()(
                                                         vatRegistrationService: RegistrationService,
                                                        vatRegConnector: TestRegistrationConnector,
                                                        vr:VatRegistrationConnector,

                                                        ds: CommonPlayDependencies)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  def postTestData(): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        for {
          _          <- vatRegConnector.setupCurrentProfile()
          (regId, _) <- vatRegistrationService.createRegistrationFootprint()
          _          <- vatRegConnector.wipeTestData
          _          <- vatRegConnector.postTestData(defaultTestData(regId))
          _          <- vr.updateIVStatus(regId,JsObject(Map("ivPassed" -> Json.toJson(true))))
        } yield Ok("Data inserted")
  }

  def incorpCompany(): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          vatRegConnector.incorpCompany(profile.transactionId).map {
            _=> Ok("Company incorporated")
          }
        }
  }

  def defaultTestData(id: String): JsValue =
    Json.parse(
      s"""
         |{
         |      "transaction_id" : "000-434-${id}",
         |      "company_name" : "DPP LIMITED",
         |      "company_type" : "ltd",
         |      "anything" : "something",
         |      "registered_office_address" : {
         |          "premises" : "98",
         |          "address_line_1" : "LIMBRICK LANE",
         |          "address_line_2" : "GORING-BY-SEA",
         |          "locality" : "WORTHING",
         |          "country" : "United Kingdom",
         |          "postal_code" : "BN12 6AG"
         |      },
         |      "officers" : [
         |          {
         |              "name_elements" : {
         |                  "forename" : "Bob",
         |                  "other_forenames" : "Bimbly Bobblous",
         |                  "surname" : "Bobbings"
         |              },
         |              "date_of_birth" : {
         |                  "day" : "12",
         |                  "month" : "11",
         |                  "year" : "1973"
         |              },
         |              "address" : {
         |                  "premises" : "98",
         |                  "address_line_1" : "LIMBRICK LANE",
         |                  "address_line_2" : "GORING-BY-SEA",
         |                  "locality" : "WORTHING",
         |                  "country" : "United Kingdom",
         |                  "postal_code" : "BN12 6AG"
         |              },
         |              "officer_role" : "director"
         |          },
         |          {
         |              "name_elements" : {
         |                  "title" : "Mx",
         |                  "forename" : "Jingly",
         |                  "surname" : "Jingles"
         |              },
         |              "date_of_birth" : {
         |                  "day" : "12",
         |                  "month" : "07",
         |                  "year" : "1988"
         |              },
         |              "address" : {
         |                  "premises" : "713",
         |                  "address_line_1" : "ST. JAMES GATE",
         |                  "locality" : "NEWCASTLE UPON TYNE",
         |                  "country" : "England",
         |                  "postal_code" : "NE1 4BB"
         |              },
         |              "officer_role" : "secretary",
         |              "resigned_on" : "2017-10-10"
         |          },
         |          {
         |              "name_elements" : {
         |                  "forename" : "Jorge",
         |                  "surname" : "Freshwater"
         |              },
         |              "date_of_birth" : {
         |                  "day" : "10",
         |                  "month" : "06",
         |                  "year" : "1994"
         |              },
         |              "address" : {
         |                  "premises" : "1",
         |                  "address_line_1" : "L ST",
         |                  "locality" : "TYNE",
         |                  "country" : "England",
         |                  "postal_code" : "AA1 4AA"
         |              },
         |              "officer_role" : "secretary"
         |          }
         |      ],
         |      "sic_codes" : [
         |          {
         |              "sic_description" : "Public order and safety activities",
         |              "sic_code" : "84240"
         |          },
         |          {
         |              "sic_description" : "Raising of dairy cattle",
         |              "sic_code" : "01410"
         |          }
         |      ]
         |  }
        """.stripMargin)

}
