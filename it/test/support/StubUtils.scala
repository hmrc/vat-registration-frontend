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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import common.enums.VatRegStatus
import models.api._
import models.external.upscan.UpscanDetails
import models.ApiKey
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

trait StubUtils {

  final class RequestHolder(var request: FakeRequest[AnyContentAsFormUrlEncoded])

  class PreconditionBuilder {

    implicit val builder: PreconditionBuilder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String): AddressStub =
      AddressStub(id, line1, line2, country, postcode)

    def user: UserStub = UserStub()

    def alfeJourney: JourneyStub = JourneyStub()

    def vatRegistration: VatRegistrationStub = VatRegistrationStub()

    def icl: ICL = ICL()

    def bankAccountReputation: BankAccountReputationServiceStub = BankAccountReputationServiceStub()

    def audit: AuditStub = AuditStub()

    def registrationApi: RegistrationApiStub = RegistrationApiStub()

    def upscanApi: UpscanApiStub = UpscanApiStub()

    def attachmentsApi: AttachmentsApiStub = AttachmentsApiStub()
  }

  def given(): PreconditionBuilder = {
    new PreconditionBuilder()
      .audit.writesAudit()
      .audit.writesAuditMerged()
  }

  case class VatRegistrationStub()(implicit builder: PreconditionBuilder) {
    def submit(url: String, status: Int): PreconditionBuilder = {
      stubFor(
        put(urlPathEqualTo(url))
          .willReturn(aResponse.withStatus(status))
      )
      builder
    }
  }

  case class ICL()(implicit builder: PreconditionBuilder) {
    def setup(): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/internal/initialise-journey"))
          .willReturn(ok(
            s"""
               |{
               |  "fetchResultsUri" : "fetch",
               |  "journeyStartUri" : "journeyStart"
               |}
             """.stripMargin
          )))

      builder
    }

    def fetchResults(sicCodeList: List[SicCode]): PreconditionBuilder = {
      val sicJsArray = Json.toJson(sicCodeList).as[JsArray]

      stubFor(
        get(urlPathMatching("/fetch-results"))
          .willReturn(ok(
            s"""
               |{
               |  "sicCodes": $sicJsArray
               |}
             """.stripMargin
          )))

      builder
    }
  }

  case class UserStub()(implicit builder: PreconditionBuilder) {

    def authoriseData(arn: Option[String]): JsValue =
      Json.obj(
        "internalId" -> "1",
        "affinityGroup" -> Organisation.toString,
        "allEnrolments" -> arn.fold(Json.arr())(ref =>
          Json.arr(
            Json.obj(
              "key" -> "HMRC-AS-AGENT",
              "identifiers" -> Json.arr(
                Json.obj(
                  "key" -> "AgentReferenceNumber",
                  "value" -> ref
                )
              )
            )
          )
        )
      )

    def isAuthorised(arn: Option[String] = None)(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      requestHolder.request = SessionCookieBaker.requestWithSession(requestHolder.request, "anyUserId")

      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(ok(authoriseData(arn).toString())))

      builder
    }

    def isNotAuthorised: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(unauthorized()))

      builder
    }
  }

  case class JourneyStub()(implicit builder: PreconditionBuilder) {

    val journeyInitUrl: UrlPathPattern = urlPathMatching(s".*/api/v2/init")

    def initialisedSuccessfully(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse.withStatus(202).withHeader("Location", "continueUrl")))
      builder
    }

    def notInitialisedAsExpected(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse().withStatus(202))) // a 202 _without_ Location header
      builder
    }

    def failedToInitialise(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(serverError()))
      builder
    }

  }

  case class AddressStub
  (id: String, line1: String, line2: String, country: String, postcode: String)
  (implicit builder: PreconditionBuilder) {

    val confirmedAddressPath = s""".*/api/v2/confirmed[?]id=$id"""

    def isFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(ok(
            s"""
               |{
               |  "auditRef": "$id",
               |  "id": "GB990091234520",
               |  "address": {
               |    "country": {
               |      "code": "$country",
               |      "name": "United Kingdom"
               |    },
               |    "lines": [
               |      "$line1",
               |      "$line2"
               |    ],
               |    "postcode": "$postcode"
               |  }
               |}
         """.stripMargin
          )))
      builder
    }

    def isNotFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(notFound()))
      builder
    }
  }

  case class AuditStub()(implicit builder: PreconditionBuilder) {
    def writesAudit(status: Int = 204): PreconditionBuilder = {
      stubFor(post(urlMatching("/write/audit"))
        .willReturn(
          aResponse().
            withStatus(status).
            withBody("""{"x":2}""")
        )
      )
      builder
    }

    def writesAuditMerged(status: Int = 204): PreconditionBuilder = {
      stubFor(post(urlMatching("/write/audit/merged"))
        .willReturn(
          aResponse().
            withStatus(status).
            withBody("""{"x":2}""")
        )
      )
      builder
    }

    def failsToWriteAudit(): PreconditionBuilder = {
      writesAudit(404)
    }
  }

  case class BankAccountReputationServiceStub()(implicit builder: PreconditionBuilder) {
    def passes: PreconditionBuilder = {
      stubFor(post(urlMatching("/validate/bank-details"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            s"""
               |{
               |  "accountNumberIsWellFormatted": "yes",
               |  "nonStandardAccountDetailsRequiredForBacs": "no"
               |}
              """.stripMargin)
        ))

      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(post(urlMatching("/validate/bank-details"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            s"""
               |{
               |  "accountNumberIsWellFormatted": "no",
               |  "nonStandardAccountDetailsRequiredForBacs": "no"
               |}
              """.stripMargin)
        ))
      builder
    }

    def isDown: PreconditionBuilder = {
      stubFor(post(urlMatching("/validate/bank-details"))
        .willReturn(
          serverError()
        ))
      builder
    }
  }

  case class RegistrationApiStub()(implicit builder: PreconditionBuilder) {

    def registrationCreated(status: VatRegStatus.Value = VatRegStatus.draft): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/registrations"))
          .willReturn(ok(
            Json.stringify(Json.obj(
              "registrationId" -> "1",
              "status" -> status.toString,
              "createdDate" -> "2021-01-01"
            ))
          ))
      )
      builder
    }

    def registrationCreationFailed: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/registrations"))
          .willReturn(serverError()))

      builder
    }

    def getRegistration(vatScheme: VatScheme)(implicit format: Format[VatScheme]): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/registrations/${vatScheme.registrationId}"))
        .willReturn(ok(Json.stringify(Json.toJson(vatScheme)))
        ))
      builder
    }

    def getRegistration(vatScheme: JsValue, regId: String = "1"): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/registrations/$regId"))
        .willReturn(ok(Json.stringify(vatScheme))
        ))
      builder
    }

    def getAllRegistrations(list: List[VatSchemeHeader]): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/registrations"))
        .willReturn(ok(Json.stringify(Json.toJson(list))))
      )
      builder
    }

    def getSection[T: ApiKey](optSection: Option[T], regId: String = "1", idx: Option[Int] = None)(implicit format: Format[T]): PreconditionBuilder = {
      val url = idx match {
        case Some(index) => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}/$index"
        case None => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"
      }
      stubFor(
        get(urlPathEqualTo(url))
          .willReturn(
            optSection match {
              case Some(section) => ok(
                Json.toJson[T](section).toString()
              )
              case None => notFound()
            }))
      builder
    }

    def getListSection[T: ApiKey](optSections: Option[List[T]], regId: String = "1")(implicit format: Format[List[T]]): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"))
          .willReturn(
            optSections match {
              case Some(sections) => ok(
                Json.toJson[List[T]](sections).toString()
              )
              case None => notFound()
            }))
      builder
    }

    def getSectionFails[T: ApiKey](regId: String = "1"): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"))
          .willReturn(badRequest())
      )
      builder
    }

    def replaceSection[T: ApiKey](data: T, regId: String = "1", idx: Option[Int] = None)(implicit format: Format[T]): PreconditionBuilder = {
      val url = idx match {
        case Some(index) => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}/$index"
        case None => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"
      }
      stubFor(
        put(urlPathEqualTo(url))
          .withRequestBody(equalToJson(Json.toJson[T](data).toString()))
          .willReturn(ok(
            Json.toJson[T](data).toString()
          )))
      builder
    }

    def replaceListSection[T: ApiKey](data: List[T], regId: String = "1")(implicit format: Format[T]): PreconditionBuilder = {
      stubFor(
        put(urlPathEqualTo(s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"))
          .withRequestBody(equalToJson(Json.toJson[List[T]](data).toString()))
          .willReturn(ok(
            Json.toJson[List[T]](data).toString()
          )))
      builder
    }

    def replaceSectionWithoutCheckingData[T: ApiKey](data: T, regId: String = "1", idx: Option[Int] = None)(implicit format: Format[T]): PreconditionBuilder = {
      val url = idx match {
        case Some(index) => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}/$index"
        case None => s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"
      }
      stubFor(
        put(urlPathEqualTo(url))
          .willReturn(ok(
            Json.toJson[T](data).toString()
          )))
      builder
    }

    def deleteSection[T: ApiKey](regId: String = "1", optIdx: Option[Int] = None): PreconditionBuilder = {
      val url = s"/vatreg/registrations/$regId/sections/${ApiKey[T]}${optIdx.fold("")(idx => s"/$idx")}"
      stubFor(
        delete(urlPathEqualTo(url))
          .willReturn(aResponse.withStatus(NO_CONTENT)))
      builder
    }

    def replaceSectionFails[T: ApiKey](regId: String = "1"): PreconditionBuilder = {
      stubFor(
        put(urlPathEqualTo(s"/vatreg/registrations/$regId/sections/${ApiKey[T]}"))
          .willReturn(badRequest())
      )
      builder
    }
  }

  case class UpscanApiStub()(implicit builder: PreconditionBuilder) {

    def upscanInitiate(reference: String): PreconditionBuilder = {
      stubFor(post(urlPathEqualTo("/upscan/v2/initiate"))
        .willReturn(ok(Json.stringify(Json.obj(
          "reference" -> reference,
          "uploadRequest" -> Json.obj(
            "href" -> "testHref",
            "fields" -> Json.obj(
              "testField1" -> "test1",
              "testField2" -> "test2"
            )
          )
        ))))
      )
      builder
    }

    def storeUpscanReference(reference: String, attachmentType: AttachmentType, regId: String = "1"): PreconditionBuilder = {
      stubFor(post(urlPathEqualTo(s"/vatreg/$regId/upscan-reference"))
        .withRequestBody(equalToJson(Json.stringify(Json.obj(
          "reference" -> reference,
          "attachmentType" -> attachmentType
        ))))
        .willReturn(ok())
      )
      builder
    }

    def fetchUpscanFileDetails(upscanDetails: UpscanDetails, regId: String = "1", reference: String): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/$regId/upscan-file-details/$reference"))
        .willReturn(ok(Json.stringify(Json.toJson[UpscanDetails](upscanDetails)))
        ))
      builder
    }

    def fetchAllUpscanDetails(upscanDetails: List[UpscanDetails], regId: String = "1")(implicit format: Format[List[UpscanDetails]]): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/$regId/upscan-file-details"))
        .willReturn(ok(Json.stringify(Json.toJson[List[UpscanDetails]](upscanDetails)))
        ))
      builder
    }

    def deleteUpscanDetails(regId: String = "1", reference: String = "test-reference"): PreconditionBuilder = {
      stubFor(
        delete(urlPathEqualTo(s"/vatreg/$regId/upscan-file-details/$reference"))
          .willReturn(noContent())
      )
      builder
    }

    def deleteAttachments(regId: String = "1"): PreconditionBuilder = {
      stubFor(
        delete(urlPathMatching(s"/vatreg/$regId/upscan-file-details"))
          .willReturn(aResponse().withStatus(NO_CONTENT))
      )
      builder
    }
  }

  case class AttachmentsApiStub()(implicit builder: PreconditionBuilder) {
    def getAttachments(attachments: List[AttachmentType], regId: String = "1"): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/$regId/attachments"))
        .willReturn(ok(Json.stringify(Json.toJson[List[AttachmentType]](attachments)))
        ))
      builder
    }

    def getIncompleteAttachments(attachments: List[AttachmentType], regId: String = "1"): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/$regId/incomplete-attachments"))
        .willReturn(ok(Json.stringify(Json.toJson[List[AttachmentType]](attachments)))
        ))
      builder
    }
  }
}