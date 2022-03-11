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

package models.view

import models.api.{Address, NETP, UkCompany}
import models.external.{EmailAddress, EmailVerified, Name}
import models.{ApplicantDetails, Director, OwnerProprietor, TelephoneNumber}
import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec

import java.time.LocalDate

class ApplicantDetailsSpec extends VatRegSpec {
  val currentAddress = Address(line1 = "TestLine1", line2 = Some("TestLine2"), postcode = Some("TE 1ST"), addressValidated = true)
  val previousAddress = Address(line1 = "TestLine11", line2 = Some("TestLine22"), postcode = Some("TE1 1ST"), addressValidated = true)

  "apiReads" should {
    "return a correct full ApplicantDetails view model with max data" in {
      val json = Json.parse(
        s"""
           |{
           |  "personalDetails": {
           |    "name": {
           |      "first": "testFirstName",
           |      "last": "testLastName"
           |    },
           |    "dateOfBirth": "2020-01-01",
           |    "nino": "AB123456C",
           |    "identifiersMatch": true
           |  },
           |  "entity": {
           |    "companyNumber": "testCrn",
           |    "companyName": "testCompanyName",
           |    "dateOfIncorporation": "2020-02-03",
           |    "countryOfIncorporation": "GB",
           |    "identifiersMatch": true,
           |    "businessVerification": "PASS",
           |    "registration": "REGISTERED",
           |    "bpSafeId": "testBpId",
           |    "ctutr": "testCtUtr"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "role": "Director",
           |  "changeOfName": {
           |    "hasFormerName": true,
           |    "name": {
           |      "first": "New",
           |      "middle": "Name",
           |      "last": "Cosmo"
           |    },
           |    "change": "2000-07-12"
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST",
           |    "addressValidated": true
           |  }
           |}
         """.stripMargin)

      val formerName = Name(first = Some("New"), middle = Some("Name"), last = "Cosmo")

      val applicantDetails = ApplicantDetails(
        entity = Some(testLimitedCompany),
        personalDetails = Some(testPersonalDetails),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(true),
        formerName = Some(formerName),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      ApplicantDetails.reads(UkCompany).reads(json) mustBe JsSuccess(applicantDetails)
    }

    "return a correct full ApplicantDetails view model with netp data" in {
      val json = Json.parse(
        s"""
           |{
           |  "personalDetails": {
           |    "name": {
           |      "first": "testFirstName",
           |      "last": "testLastName"
           |    },
           |    "dateOfBirth": "2020-01-01",
           |    "trn": "$testTrn",
           |    "identifiersMatch": true
           |  },
           |  "entity": {
           |    "firstName": "$testFirstName",
           |    "lastName": "$testLastName",
           |    "dateOfBirth": "$testApplicantDob",
           |    "trn": "$testTrn",
           |    "sautr": "$testSautr",
           |    "identifiersMatch": true,
           |    "businessVerification": "PASS",
           |    "registration": "REGISTERED",
           |    "bpSafeId": "$testSafeId"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "roleInTheBusiness": "03",
           |  "changeOfName": {
           |    "hasFormerName": true,
           |    "name": {
           |      "first": "New",
           |      "middle": "Name",
           |      "last": "Cosmo"
           |    },
           |    "change": "2000-07-12"
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST",
           |    "addressValidated": true
           |  }
           |}
         """.stripMargin)

      val formerName = Name(first = Some("New"), middle = Some("Name"), last = "Cosmo")

      val applicantDetails = ApplicantDetails(
        entity = Some(testNetpSoleTrader),
        personalDetails = Some(testPersonalDetails.copy(nino = None, trn = Some(testTrn), identifiersMatch = true)),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(true),
        formerName = Some(formerName),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress))),
        roleInTheBusiness = Some(Director)
      )

      ApplicantDetails.reads(NETP).reads(json) mustBe JsSuccess(applicantDetails)
    }

    "return a correct full ApplicantDetails view model with min data" in {
      val json = Json.parse(
        s"""
           |{
           |  "personalDetail": {
           |    "name": {
           |      "first": "First",
           |      "last": "Last"
           |    },
           |    "dob": "1998-07-12",
           |    "nino": "AA123456Z"
           |  },
           |  "role": "Director",
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "changeOfName": {
           |    "hasFormerName": false
           |  }
           |}
         """.stripMargin)

      val applicantDetails = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(false),
        previousAddress = Some(PreviousAddressView(yesNo = true, None))
      )

      ApplicantDetails.reads(UkCompany).reads(json) mustBe JsSuccess(applicantDetails)
    }
  }

  "Calling apiWrites" should {
    "return a correct partial JsValue with data" in {
      val data = ApplicantDetails()
      val validJson = Json.obj()

      Json.toJson(data)(ApplicantDetails.writes) mustBe validJson
    }

    "return a correct full JsValue with maximum data" in {
      val data = ApplicantDetails(
        entity = Some(testLimitedCompany),
        personalDetails = Some(testPersonalDetails),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(true),
        formerName = Some(Name(Some("New"), Some("Name"), "Cosmo")),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "personalDetails": {
           |    "name": {
           |      "first": "testFirstName",
           |      "last": "testLastName"
           |    },
           |    "dateOfBirth": "2020-01-01",
           |    "nino": "AB123456C",
           |    "identifiersMatch": true
           |  },
           |  "entity": {
           |    "companyNumber": "testCrn",
           |    "companyName": "testCompanyName",
           |    "dateOfIncorporation": "2020-02-03",
           |    "countryOfIncorporation": "GB",
           |    "identifiersMatch": true,
           |    "businessVerification": "PASS",
           |    "registration": "REGISTERED",
           |    "bpSafeId": "testBpId",
           |    "ctutr": "testCtUtr"
           |  },
           |  "changeOfName": {
           |    "hasFormerName": true,
           |    "change": "2000-07-12",
           |    "name": {
           |      "middle": "Name",
           |      "last": "Cosmo",
           |      "first": "New"
           |    }
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST",
           |    "addressValidated": true
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  }
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.writes) mustBe validJson
    }

    "return a correct full JsValue with netp data" in {
      val data = ApplicantDetails(
        entity = Some(testNetpSoleTrader),
        personalDetails = Some(testPersonalDetails.copy(nino = None, trn = Some(testTrn), identifiersMatch = false)),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(true),
        formerName = Some(Name(Some("New"), Some("Name"), "Cosmo")),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress))),
        roleInTheBusiness = Some(OwnerProprietor)
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "personalDetails": {
           |    "name": {
           |      "first": "testFirstName",
           |      "last": "testLastName"
           |    },
           |    "dateOfBirth": "2020-01-01",
           |    "trn": "$testTrn",
           |    "identifiersMatch": false
           |  },
           |  "entity": {
           |    "firstName": "$testFirstName",
           |    "lastName": "$testLastName",
           |    "dateOfBirth": "$testApplicantDob",
           |    "sautr": "$testSautr",
           |    "trn": "$testTrn",
           |    "identifiersMatch": true,
           |    "businessVerification": "PASS",
           |    "registration": "REGISTERED",
           |    "bpSafeId": "$testSafeId"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "roleInTheBusiness": "01",
           |  "changeOfName": {
           |    "hasFormerName": true,
           |    "name": {
           |      "first": "New",
           |      "middle": "Name",
           |      "last": "Cosmo"
           |    },
           |    "change": "2000-07-12"
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST",
           |    "addressValidated": true
           |  }
           |}
         """.stripMargin)

      Json.toJson(data)(ApplicantDetails.writes) mustBe validJson
    }

    "return a correct full JsValue with minimum data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        emailAddress = Some(EmailAddress("test@t.test")),
        emailVerified = Some(EmailVerified(true)),
        telephoneNumber = Some(TelephoneNumber("1234")),
        hasFormerName = Some(false),
        previousAddress = Some(PreviousAddressView(yesNo = true, None))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "contact": {
           |    "email": "test@t.test",
           |    "emailVerified": true,
           |    "tel": "1234"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST",
           |    "addressValidated": true
           |  },
           |  "changeOfName": {
           |    "hasFormerName": false
           |  }
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.writes) mustBe validJson
    }
  }

}
