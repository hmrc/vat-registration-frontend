/*
 * Copyright 2024 HM Revenue & Customs
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

package fixtures

import common.enums.VatRegStatus
import models._
import models.api._
import models.api.vatapplication._
import models.external.{BvPass, PartnershipIdEntity}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

trait BaseFixture {
  //Test variables
  val testDate: LocalDate = LocalDate.of(2017, 3, 21)
  val testTradingName = "ACME INC"
  val testSortCode = "12-34-56"
  val testAccountNumber = "12345678"
  val validLabourSicCode: SicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance: SicCode = SicCode("12345678", "fooBar", "FooBar")
  val validExpectedOverTrue: Option[LocalDate] = Some(testDate)

  def generateThreshold(mandatory: Boolean = false,
                        thresholdPreviousThirtyDays: Option[LocalDate] = None,
                        thresholdInTwelveMonths: Option[LocalDate] = None): Threshold =
    (mandatory, thresholdPreviousThirtyDays, thresholdInTwelveMonths) match {
      case (false, None, None) => Threshold(mandatoryRegistration = false)
      case (_, ptd, itm) if List(ptd, itm).flatten.nonEmpty => Threshold(mandatoryRegistration = true, ptd, itm)
      case _ => Threshold(mandatoryRegistration = false)
    }

  val validVoluntaryRegistration: Threshold = generateThreshold()
  val validMandatoryRegistrationThirtyDays: Threshold = generateThreshold(thresholdPreviousThirtyDays = Some(testDate))
  val validMandatoryRegistrationBothDates: Threshold = generateThreshold(thresholdPreviousThirtyDays = Some(testDate), thresholdInTwelveMonths = Some(testDate))
  val validMandatoryRegistrationTwelve: Threshold = generateThreshold(thresholdInTwelveMonths = Some(testDate))
  val optVoluntaryRegistration: Option[Threshold] = Some(validVoluntaryRegistration)
  val optMandatoryRegistrationThirtyDays: Option[Threshold] = Some(validMandatoryRegistrationThirtyDays)
  val optMandatoryRegistrationBothDates: Option[Threshold] = Some(validMandatoryRegistrationBothDates)
  val optMandatoryRegistrationTwelve: Option[Threshold] = Some(validMandatoryRegistrationTwelve)
}

trait VatRegistrationFixture extends BaseFixture with FlatRateFixtures with ApplicantDetailsFixtures {

  val ukBankAccount: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("accountName", "SortCode", "AccountNumber")), None)

  val sicCode: SicCode = SicCode("88888", "description", "displayDetails")

  val currentThreshold = "50000"
  val formattedThreshold = "50,000"

  //Responses
  val forbidden: UpstreamErrorResponse = UpstreamErrorResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val noContent: HttpResponse = HttpResponse(204, "")
  val upstream4xx: UpstreamErrorResponse = UpstreamErrorResponse(IM_A_TEAPOT.toString, IM_A_TEAPOT, IM_A_TEAPOT)
  val upstream5xx: UpstreamErrorResponse = UpstreamErrorResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val validHttpResponse: HttpResponse = HttpResponse(OK, "{}")

  val validCacheMap: CacheMap = CacheMap("fooBarWizzBand", Map("foo" -> Json.toJson("wizz")))

  //Exceptions
  val badRequest = new BadRequestException(BAD_REQUEST.toString)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val internalServiceException = new InternalServerException(BAD_GATEWAY.toString)
  val runTimeException = new RuntimeException("tst")
  val internalServerError: UpstreamErrorResponse = UpstreamErrorResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val exception = new Exception(BAD_GATEWAY.toString)

  //Test variables
  val contextRoot = "/register-for-vat"
  val testNino: String = "AA 12 34 56 C"
  val testBusinessActivityDescription = "description"
  val testRegId = "VAT123456"
  val testHonestyDeclaration = true
  val validBankCheckJsonResponseString: String =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "yes",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val validBankCheckJsonResponse: JsValue = Json.parse(validBankCheckJsonResponseString)

  val invalidBankCheckJsonResponseString: String =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "no",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val invalidBankCheckJsonResponse: JsValue = Json.parse(invalidBankCheckJsonResponseString)

  val indeterminateBankCheckJsonResponseString: String =
    s"""
       |{
       |  "accountNumberIsWellFormatted": "indeterminate",
       |  "nonStandardAccountDetailsRequiredForBacs": "no"
       |}
     """.stripMargin

  val indeterminateBankCheckJsonResponse: JsValue = Json.parse(indeterminateBankCheckJsonResponseString)

  val complianceWithoutLabour: LabourCompliance = LabourCompliance(None, None, None)
  val complianceWithLabour: LabourCompliance = LabourCompliance(Some(12), Some(true), Some(true))

  lazy val validTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(PersonalDetails(
      firstName = "testFirstName",
      lastName = "testLastName",
      nino = Some("AB123456C"),
      trn = None,
      identifiersMatch = true,
      dateOfBirth = Some(LocalDate.of(2020, 1, 1))
    )),
    isPartOfOrganisation = Some(true),
    organisationName = Some("testCompanyName"),
    telephone = Some("1234"),
    email = Some("test@t.test"),
    emailVerified = Some(true),
    address = Some(testAddress),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  //Api models

  val testLine1 = "line1"
  val testLine2 = "line2"
  val testLine3 = "line3"
  val testLine4 = "line4"
  val testLine5 = "line5"
  val testCountry: Country = Country(Some("UK"), Some("United Kingdom"))
  val testAddress: Address = Address(testLine1, Some(testLine2), None, None, None, Some(testPostcode), Some(testCountry), addressValidated = true)

  val validCompRegProfileJson: JsObject = Json.parse(
    """
      |{
      |  "status":"submitted",
      |  "acknowledgementReferences":{
      |    "status":"04"
      |  }
      |}
    """.stripMargin).as[JsObject]

  val emptyVatScheme: VatScheme = VatScheme(testRegId, testDate, status = VatRegStatus.draft)

  val validPartnershipIdEntity: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    companyName = Some(testCompanyName),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    identifiersMatch = true
  )

  val validEntities: List[Entity] = List[Entity](
    Entity(
      details = Some(validPartnershipIdEntity),
      partyType = LtdLiabilityPartnership,
      isLeadPartner = Some(true),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    ),
    Entity(
      details = Some(validPartnershipIdEntity),
      partyType = LtdLiabilityPartnership,
      isLeadPartner = Some(false),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    )
  )

  val validNipCompliance: NIPTurnover = NIPTurnover(
    goodsToEU = Some(ConditionalValue(answer = true, Some(BigDecimal(1)))),
    goodsFromEU = Some(ConditionalValue(answer = true, Some(BigDecimal(1))))
  )

  val testTurnover = 100
  val testZeroRatedSupplies = 10000.5
  val validVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(testZeroRatedSupplies),
    claimVatRefunds = Some(false),
    appliedForExemption = None,
    startDate = Some(LocalDate.of(2017, 10, 10)),
    returnsFrequency = Some(Monthly),
    staggerStart = Some(MonthlyStagger)
  )

  val validAasDetails: AASDetails = AASDetails(Some(QuarterlyPayment), Some(BACS))

  val validUkBankAccount: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "12-34-56", "12345678")), None)

  val noUkBankAccount: BankAccount = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))

  val validEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    UkCompany,
    isTransactor = false,
    appliedForException = None,
    registrationReason = ForwardLook,
    fixedEstablishmentInManOrUk = true
  )

  val validSoleTraderEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    validMandatoryRegistrationThirtyDays,
    Individual,
    isTransactor = false,
    appliedForException = None,
    registrationReason = ForwardLook,
    fixedEstablishmentInManOrUk = true
  )

  val validBusiness: Business = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    welshLanguage = Some(false),
    labourCompliance = Some(complianceWithoutLabour),
    businessDescription = Some(testBusinessActivityDescription),
    mainBusinessActivity = Some(sicCode)
  )

  val validBusinessWithNoDescription: Business = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    labourCompliance = Some(complianceWithoutLabour)
  )

  val validBusinessWithNoDescriptionAndLabour: Business = Business(
    hasTradingName = Some(true),
    tradingName = Some(testTradingName),
    email = Some("test@foo.com"),
    telephoneNumber = Some("123"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(testAddress),
    contactPreference = Some(Email),
    mainBusinessActivity = Some(sicCode),
    businessActivities = Some(List(sicCode))
  )

  val otherBusinessInvolvementWithVrn: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(true),
    vrn = Some("123456782"),
    hasUtr = None,
    utr = None,
    stillTrading = Some(false))

  val otherBusinessInvolvementWithUtr: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(true),
    utr = Some("123456782"),
    stillTrading = Some(false))

  val otherBusinessInvolvementWithoutVrnUtr: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(false),
    utr = None,
    stillTrading = Some(false))

  val otherBusinessInvolvementWithPartialData: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = None,
    vrn = None,
    hasUtr = None,
    utr = None,
    stillTrading = None)

  val validVatScheme: VatScheme = VatScheme(
    registrationId = testRegId,
    createdDate = testDate,
    business = Some(validBusiness),
    applicantDetails = Some(completeApplicantDetails),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validUkBankAccount),
    vatApplication = Some(validVatApplication),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validEligibilitySubmissionData)
  )

  val validSoleTraderVatScheme: VatScheme = VatScheme(
    registrationId = testRegId,
    createdDate = testDate,
    business = Some(validBusiness),
    applicantDetails = Some(completeApplicantDetails),
    flatRateScheme = Some(validFlatRate),
    bankAccount = Some(validUkBankAccount),
    vatApplication = Some(validVatApplication),
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(validSoleTraderEligibilitySubmissionData)
  )

  // ICL
  val iclMultipleResults: JsObject = Json.parse(
    """
      | {
      |   "sicCodes": [
      |     {
      |       "code" : "12345",
      |       "desc" : "Whale farming",
      |       "indexes" : [
      |         {
      |         "desc" : "Fish farming",
      |         "desc" : "Fish-related activities"
      |         } ]
      |        },
      |     {
      |       "code" : "23456",
      |       "desc" : "Dog walking",
      |       "indexes" : [
      |         {
      |         "desc" : "Animal services"
      |         } ]
      |        }
      |   ]
      | }
    """.stripMargin).as[JsObject]

  val iclMultipleResultsSicCode1: SicCode = SicCode("12345", "Whale farming", "")
  val iclMultipleResultsSicCode2: SicCode = SicCode("23456", "Dog walking", "")

  val iclSingleResult: JsObject = Json.parse(
    """
      | {
      |   "sicCodes": [
      |     {
      |       "code" : "12345",
      |       "desc" : "Whale farming",
      |       "indexes" : [
      |         {
      |         "desc" : "Fish farming",
      |         "desc" : "Fish-related activities"
      |         } ]
      |        }
      |       ]
      |     }
    """.stripMargin).as[JsObject]

  val fullEligibilityDataJson: JsObject = Json.obj(
    "fixedEstablishment" -> true,
    "businessEntity" -> "50",
    "agriculturalFlatRateScheme" -> false,
    "internationalActivities" -> false,
    "registeringBusiness" -> "own",
    "registrationReason" -> "selling-goods-and-services",
    "thresholdPreviousThirtyDays" -> Json.obj(
      "value" -> true,
      "optionalData" -> "2017-05-23"
    ),
    "thresholdInTwelveMonths" -> Json.obj(
      "value" -> true,
      "optionalData" -> "2017-07-16"
    ),
    "vatRegistrationException" -> false
  )

  val testBankName = "testName"

  val testUkBankDetails: BankAccountDetails = BankAccountDetails(testBankName, testAccountNumber, testSortCode, Some(ValidStatus))

  val bankAccount: BankAccount = BankAccount(isProvided = true, Some(testUkBankDetails), None)

  val flatRateScheme: FlatRateScheme = FlatRateScheme(joinFrs = Some(false))

  val testCreatedDate: LocalDate = LocalDate.of(2021, 1, 1)

  val addressWithCountry: Address = Address(testLine1, Some(testLine2), None, None, None, Some("XX XX"), Some(testCountry), addressValidated = true)

  val testCalculatedDate: LocalDate = LocalDate.now()

  val threshold: Threshold = Threshold(
    mandatoryRegistration = true,
    thresholdPreviousThirtyDays = None,
    thresholdInTwelveMonths = Some(LocalDate.of(2018, 5, 30))
  )

  val fullVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(1234),
    northernIrelandProtocol = Some(NIPTurnover(goodsToEU = Some(ConditionalValue(answer = false, None)), goodsFromEU = Some(ConditionalValue(answer = false, None)))),
    claimVatRefunds = Some(true),
    appliedForExemption = None,
    overseasCompliance = None,
    startDate = Some(LocalDate.of(2020, 1, 1)),
    returnsFrequency = Some(Quarterly),
    staggerStart = Some(JanuaryStagger),
    annualAccountingDetails = Some(AASDetails(paymentFrequency = Some(QuarterlyPayment), paymentMethod = Some(StandingOrder))),
    hasTaxRepresentative = None
  )

  val testEligibilitySubmissionDataPartnership: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold = threshold,
    partyType = Partnership,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook,
    calculatedDate = Some(testCalculatedDate),
    fixedEstablishmentInManOrUk = true
  )

  val businessDetails: Business = Business(
    otherBusinessInvolvement = Some(false),
    hasLandAndProperty = Some(false),
    hasWebsite = Some(true),
    hasTradingName = Some(true),
    email = Some("test@foo.com"),
    telephoneNumber = Some("987654"),
    website = Some("/test/url"),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email),
    welshLanguage = Some(false),
    businessDescription = Some("test company desc"),
    mainBusinessActivity = Some(SicCode("AB123", "super business", "super business by super people")),
    businessActivities = Some(List(SicCode("AB123", "super business", "super business by super people")))
  )

  val otherBusinessInvolvement1: List[OtherBusinessInvolvement] = List(OtherBusinessInvolvement(
    businessName = Some("test business name"),
    hasVrn = Some(true),
    vrn = Some("test vrn"),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  ))

  val fullVatSchemeAttachment: VatScheme = VatScheme(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.submitted,
    attachments = Some(Attachments(
      method = Some(Upload),
      supplyVat1614a = Some(true), supplyVat1614h = Some(true), supplySupportingDocuments = Some(true), additionalPartnersDocuments = Some(false))),
    applicantDetails = Some(validFullApplicantDetailsPartnership),
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    vatApplication = Some(fullVatApplication),
    eligibilitySubmissionData = Some(testEligibilitySubmissionDataPartnership),
    entities = Some(validEntities),
    transactorDetails = Some(validTransactorDetails),
    otherBusinessInvolvements = Some(otherBusinessInvolvement1)
  )

}