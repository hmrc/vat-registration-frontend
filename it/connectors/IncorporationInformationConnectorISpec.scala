
package connectors

import java.util.Base64

import common.enums.VatRegStatus
import models.external.{CoHoRegisteredOfficeAddress, Name, Officer, OfficerList}
import play.api.libs.json.Json
import support.AppAndStubs
import uk.gov.hmrc.play.test.UnitSpec

class IncorporationInformationConnectorISpec extends UnitSpec with AppAndStubs {

  val iiConnector               = app.injector.instanceOf[IncorporationInformationConnector]
  val currentProfileWhitelisted = models.CurrentProfile(
    "fooBar","99","dummy",VatRegStatus.draft,None,None
  )
  val companyNameRaw            =
    """
      |{"company_name": "Normal User LTD"}
    """.stripMargin
  val nonWhitelistedRegId         = "normalUser"
  val transactionID               = "000-431-TEST"
  val registeredOfficerAddressRaw =
    """
      |{
      | "registered_office_address":{
      |  "premises":"some-premises",
      |  "address_line_1":"test address line 1",
      |  "locality":"test locality",
      |  "address_line_2":"test address line 2",
      |  "postal_code":"XX1 1XX",
      |  "po_box":"No Thanks",
      |  "country":"UK",
      |  "region":"Test"
      | }
      |}
    """.stripMargin
  val officerListRaw              =
    """
      |{
      |  "officers":[
      |  {
      |    "name_elements": {
      |      "forename": "forenameone",
      |      "other_forenames": "middleone",
      |      "surname": "surnameone"
      |    },
      |    "officer_role": "director",
      |    "dob": "2000-12-25",
      |    "nino": "JW778877A",
      |    "details": {
      |      "currentAddress": {
      |        "line1": "Test Line 1",
      |        "line2": "Test Line 2",
      |        "postcode": "ZZ1 1ZZ"
      |      },
      |      "contact": {
      |        "email": "me@foo.com",
      |        "tel": "112233445566",
      |        "mobile": "0112233445566"
      |      },
      |      "changeOfName": {
      |        "name": {
      |          "first": "Old",
      |          "middle": "Name",
      |          "last": "Cosmopoliton"
      |        },
      |        "change": "2000-07-12"
      |      }
      |    }
      |  },
      |  {
      |  "name_elements": {
      |    "forename": "forenametwo",
      |    "other_forenames": "middletwo",
      |    "surname": "surnametwo"
      |  },
      |  "officer_role": "secretary",
      |  "dob": "1930-10-02",
      |  "nino": "JW778876A",
      |  "details": {
      |    "currentAddress": {
      |      "line1": "Another Test Line 1",
      |      "line2": "Another Test Line 2",
      |      "postcode": "ZZ1 1ZZ"
      |    },
      |    "contact": {
      |      "email": "foo@barwizzbangbuzz.com",
      |      "tel": "012233445566",
      |      "mobile": "0112233445560"
      |      }
      |    }
      |  }
      |  ]
      |}
    """.stripMargin

  val registeredOfficerAddressFromII  = CoHoRegisteredOfficeAddress(
    premises = "testPremises - 1", addressLine1 = "ii address line 1", locality = "some locality ii"
  )
  val nonWhitelistedOfficersList  = Seq(
    Officer(Name(forename = Some("forenameoneNOT"), otherForenames = Some("middleoneNOT") , surname = "surnameoneNOT"), "director"),
    Officer(Name(forename = Some("forenametwoNOT"), otherForenames = Some("middletwoNOT") , surname = "surnametwoNOT"), "secretary")
  )

  override lazy val additionalConfig: Map[String, String] =
    Map(
      "default-coho-registered-office-address" -> "ew0KICJyZWdpc3RlcmVkX29mZmljZV9hZGRyZXNzIjp7DQogICJwcmVtaXNlcyI6InNvbWUtcHJlbWlzZXMiLA0KICAiYWRkcmVzc19saW5lXzEiOiJ0ZXN0IGFkZHJlc3MgbGluZSAxIiwNCiAgImxvY2FsaXR5IjoidGVzdCBsb2NhbGl0eSIsDQogICJhZGRyZXNzX2xpbmVfMiI6InRlc3QgYWRkcmVzcyBsaW5lIDIiLA0KICAicG9zdGFsX2NvZGUiOiJYWDEgMVhYIiwNCiAgInBvX2JveCI6Ik5vIFRoYW5rcyIsDQogICJjb3VudHJ5IjoiVUsiLA0KICAicmVnaW9uIjoiVGVzdCINCiB9DQp9",
      "default-company-name" -> "eyJjb21wYW55X25hbWUiOiAiTm9ybWFsIFVzZXIgTFREIn0=",
      "default-officer-list" -> "ew0KICAib2ZmaWNlcnMiOlsNCiAgew0KICAgICJuYW1lX2VsZW1lbnRzIjogew0KICAgICAgImZvcmVuYW1lIjogImZvcmVuYW1lb25lIiwNCiAgICAgICJvdGhlcl9mb3JlbmFtZXMiOiAibWlkZGxlb25lIiwNCiAgICAgICJzdXJuYW1lIjogInN1cm5hbWVvbmUiDQogICAgfSwNCiAgICAib2ZmaWNlcl9yb2xlIjogImRpcmVjdG9yIiwNCiAgICAiZG9iIjogIjIwMDAtMTItMjUiLA0KICAgICJuaW5vIjogIkpXNzc4ODc3QSIsDQogICAgImRldGFpbHMiOiB7DQogICAgICAiY3VycmVudEFkZHJlc3MiOiB7DQogICAgICAgICJsaW5lMSI6ICJUZXN0IExpbmUgMSIsDQogICAgICAgICJsaW5lMiI6ICJUZXN0IExpbmUgMiIsDQogICAgICAgICJwb3N0Y29kZSI6ICJaWjEgMVpaIg0KICAgICAgfSwNCiAgICAgICJjb250YWN0Ijogew0KICAgICAgICAiZW1haWwiOiAibWVAZm9vLmNvbSIsDQogICAgICAgICJ0ZWwiOiAiMTEyMjMzNDQ1NTY2IiwNCiAgICAgICAgIm1vYmlsZSI6ICIwMTEyMjMzNDQ1NTY2Ig0KICAgICAgfSwNCiAgICAgICJjaGFuZ2VPZk5hbWUiOiB7DQogICAgICAgICJuYW1lIjogew0KICAgICAgICAgICJmaXJzdCI6ICJPbGQiLA0KICAgICAgICAgICJtaWRkbGUiOiAiTmFtZSIsDQogICAgICAgICAgImxhc3QiOiAiQ29zbW9wb2xpdG9uIg0KICAgICAgICB9LA0KICAgICAgICAiY2hhbmdlIjogIjIwMDAtMDctMTIiDQogICAgICB9DQogICAgfQ0KICB9LA0KICB7DQogICJuYW1lX2VsZW1lbnRzIjogew0KICAgICJmb3JlbmFtZSI6ICJmb3JlbmFtZXR3byIsDQogICAgIm90aGVyX2ZvcmVuYW1lcyI6ICJtaWRkbGV0d28iLA0KICAgICJzdXJuYW1lIjogInN1cm5hbWV0d28iDQogIH0sDQogICJvZmZpY2VyX3JvbGUiOiAic2VjcmV0YXJ5IiwNCiAgImRvYiI6ICIxOTMwLTEwLTAyIiwNCiAgIm5pbm8iOiAiSlc3Nzg4NzZBIiwNCiAgImRldGFpbHMiOiB7DQogICAgImN1cnJlbnRBZGRyZXNzIjogew0KICAgICAgImxpbmUxIjogIkFub3RoZXIgVGVzdCBMaW5lIDEiLA0KICAgICAgImxpbmUyIjogIkFub3RoZXIgVGVzdCBMaW5lIDIiLA0KICAgICAgInBvc3Rjb2RlIjogIlpaMSAxWloiDQogICAgfSwNCiAgICAiY29udGFjdCI6IHsNCiAgICAgICJlbWFpbCI6ICJmb29AYmFyd2l6emJhbmdidXp6LmNvbSIsDQogICAgICAidGVsIjogIjAxMjIzMzQ0NTU2NiIsDQogICAgICAibW9iaWxlIjogIjAxMTIyMzM0NDU1NjAiDQogICAgICB9DQogICAgfQ0KICB9DQogIF0NCn0=",
      "regIdPostIncorpWhitelist" -> "OTgsOTk="
    )

  "getRegisteredOfficeAddress" should {
    "return default data from config for a whitelisted regId" in {
      val res = await(iiConnector.getRegisteredOfficeAddress(transactionID)(hc,currentProfileWhitelisted))
        res shouldBe Json.fromJson[CoHoRegisteredOfficeAddress](Json.parse(registeredOfficerAddressRaw)).asOpt
    }
    "return data from II when the regId is not whitelisted" in {
      given()
        .company.hasROAddress(registeredOfficerAddressFromII)

      val res = await(iiConnector.getRegisteredOfficeAddress(transactionID)(hc,currentProfileWhitelisted.copy(registrationId = nonWhitelistedRegId)))
      res shouldBe Some(registeredOfficerAddressFromII)
    }
  }

  "getCompanyName" should {
    "return default data from config for a whitelisted regId" in {
      val res = await(iiConnector.getCompanyName("99",transactionID)(hc))
      res shouldBe Json.parse(companyNameRaw)

    }

    "return data from II when the regId is not whitelisted" in {
      given()
        .company.nameIs("Foo Bar Wizz Bang")

      val res = await(iiConnector.getCompanyName(nonWhitelistedRegId, transactionID)(hc))
      res shouldBe Json.parse("""{"company_name": "Foo Bar Wizz Bang"}""")
    }
  }

//  "getIncorpUpdate" should {
//    "return an incorporation update" in {
//      val res = await(iiConnector.get("99",transactionID)(hc))
//      res shouldBe Json.parse(companyNameRaw)
//
//    }
//
//    "return data from II when the regId is not whitelisted" in {
//      given()
//        .company.nameIs("Foo Bar Wizz Bang")
//
//      val res = await(iiConnector.getCompanyName(nonWhitelistedRegId, transactionID)(hc))
//      res shouldBe Json.parse("""{"company_name": "Foo Bar Wizz Bang"}""")
//    }
//  }

  "getOfficerList" should {
    "return default data from config for a whitelisted regId" in {
      val res = await(iiConnector.getOfficerList(transactionID)(hc, currentProfileWhitelisted))
      res shouldBe Json.fromJson[OfficerList](Json.parse(officerListRaw)).asOpt
    }

    "return data from II when the regId is not whitelisted" in {
      given()
        .company.hasOfficerList(nonWhitelistedOfficersList)

      val res = await(iiConnector.getOfficerList(transactionID)(hc, currentProfileWhitelisted.copy(registrationId = nonWhitelistedRegId)))
      res shouldBe Some(OfficerList(nonWhitelistedOfficersList))
    }
  }
}
