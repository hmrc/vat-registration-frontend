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

package itFixtures

import common.enums.VatRegStatus
import models._
import models.api.SicCode.SIC_CODES_KEY
import models.api._
import models.api.vatapplication._
import models.external._
import models.external.soletraderid.OverseasIdentifierDetails
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import java.time.LocalDate

trait ITRegistrationFixtures extends ApplicantDetailsFixture {
  val testRegId = "1"
  val testArn = "testArn"
  val testCreatedDate: LocalDate = LocalDate.of(2021, 1, 1)

  val voluntaryThreshold: Threshold = Threshold(
    mandatoryRegistration = false
  )

  val threshold: Threshold = Threshold(
    mandatoryRegistration = true,
    thresholdPreviousThirtyDays = None,
    thresholdInTwelveMonths = Some(LocalDate.of(2018, 5, 30))
  )

  val flatRateScheme: FlatRateScheme = FlatRateScheme(joinFrs = Some(false))
  val testBankName = "testName"
  val testSortCode = "123456"
  val testAccountNumber = "12345678"
  val testUkBankDetails: BankAccountDetails = BankAccountDetails(testBankName, testAccountNumber, testSortCode, Some(ValidStatus))
  val bankAccount: BankAccount = BankAccount(isProvided = true, Some(testUkBankDetails), None)
  val emptyBankAccount: BankAccount = BankAccount(isProvided = true, None, None)
  val bankAccountNotProvidedNoReason: BankAccount = BankAccount(isProvided = false, None, None)
  val bankAccountNotProvided: BankAccount = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))
  val testTurnover = 30000
  val fullVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(1234),
    northernIrelandProtocol = Some(NIPTurnover(goodsToEU = Some(ConditionalValue(answer = false, None)), goodsFromEU = Some(ConditionalValue(answer = false, None)))),
    claimVatRefunds = Some(true),
    appliedForExemption = None,
    overseasCompliance = None,
    startDate = Some(LocalDate.of(2020,1,1)),
    returnsFrequency = Some(Quarterly),
    staggerStart = Some(JanuaryStagger),
    annualAccountingDetails = Some(AASDetails(paymentFrequency = Some(QuarterlyPayment), paymentMethod = Some(StandingOrder))),
    hasTaxRepresentative = None
  )
  val testCalculatedDate: LocalDate = LocalDate.now()
  val testLine1 = "line1"
  val testLine2 = "line2"
  val testCountry: Country = Country(Some("UK"), Some("United Kingdom"))
  val address: Address = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE1 1ST"), addressValidated = true)
  val addressWithCountry: Address = Address(testLine1, Some(testLine2), None, None, None, Some("XX XX"), Some(testCountry), addressValidated = true)

  val testWarehouseNumber: String = "tst123456789012"
  val testWarehouseName: String = "testWarehouseName"
  val testFullOverseasCompliance: OverseasCompliance = OverseasCompliance(
    goodsToOverseas = Some(true),
    goodsToEu = Some(true),
    storingGoodsForDispatch = Some(StoringWithinUk),
    usingWarehouse = Some(true),
    fulfilmentWarehouseNumber = Some(testWarehouseNumber),
    fulfilmentWarehouseName = Some(testWarehouseName)
  )

  val testEligibilitySubmissionData: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    UkCompany,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook,
    calculatedDate = Some(testCalculatedDate),
    fixedEstablishmentInManOrUk = true
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

  val testEligibilitySubmissionDataNETP: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold = threshold,
    partyType = NETP,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook,
    calculatedDate = Some(testCalculatedDate),
    fixedEstablishmentInManOrUk = true
  )

  val testEligibilitySubmissionDataPartner: EligibilitySubmissionData = EligibilitySubmissionData(
    threshold,
    Partnership,
    isTransactor = false,
    appliedForException = Some(false),
    registrationReason = ForwardLook,
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

  val validBusinessContactDetailsJson: JsValue = Json.parse(
    """
      |{
      |"ppob" : {
      |   "line1"    : "line1",
      |   "line2"    : "line2",
      |   "postcode" : "XX XX",
      |   "country"  : {
      |     "code": "UK",
      |     "name": "United Kingdom"
      |   },
      |   "addressValidated" : true
      | },
      | "digitalContact" : {
      |   "email"    : "test@foo.com",
      |   "tel"      : "123",
      |   "mobile"   : "987654"
      | },
      | "website"   :"/test/url",
      | "contactPreference": "Email"
      |}
    """.stripMargin
  )

  val emptyUkCompanyVatScheme: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  val emptyVatSchemeNetp: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(
      partyType = NETP,
      fixedEstablishmentInManOrUk = false,
      registrationReason = NonUk
    ))
  )

  val testPostcode = "AA11AA"
  val testLine3 = "line3"
  val testLine4 = "line4"
  val testLine5 = "line5"
  val testAddress: Address = Address(testLine1, Some(testLine2), None, None, None, Some(testPostcode), Some(testCountry), addressValidated = true)

  val emptyVatSchemeNonUkCompany: VatScheme = VatScheme(
    testRegId,
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(
      partyType = NonUkNonEstablished,
      fixedEstablishmentInManOrUk = false,
      registrationReason = NonUk
    ))
  )

  val fullVatScheme: VatScheme = VatScheme(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    applicantDetails = Some(validFullApplicantDetails),
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    vatApplication = Some(fullVatApplication),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )

  lazy val fullNetpVatScheme: VatScheme = fullVatScheme.copy(
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(
      partyType = NETP,
      fixedEstablishmentInManOrUk = false,
      registrationReason = NonUk
    )),
    applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testNetpSoleTrader), personalDetails = Some(testNetpPersonalDetails))),
    business = Some(businessDetails.copy(tradingName = Some(testCompanyName))),
    vatApplication = Some(fullVatApplication.copy(overseasCompliance = Some(testFullOverseasCompliance))),
    bankAccount = None
  )

  val vatRegIncorporated: VatScheme = VatScheme(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    applicantDetails = None,
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    eligibilitySubmissionData = Some(testEligibilitySubmissionData)
  )
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

  val testCrn = "testCrn"
  val testChrn = "testChrn"
  val testCasc = "testCasc"
  val testCompanyName = "testCompanyName"
  val testCtUtr = "testCtUtr"
  val testIncorpDate: Option[LocalDate] = Some(LocalDate.of(2020, 2, 3))

  val testIncorpDetails: IncorporatedEntity = IncorporatedEntity(testCrn, Some(testCompanyName), Some(testCtUtr), None, testIncorpDate, "GB", identifiersMatch = true, RegisteredStatus, Some(BvPass), Some(testBpSafeId))

  val testSautr = "1234567890"
  val testRegistration: BusinessRegistrationStatus = RegisteredStatus
  val testSafeId = "X00000123456789"
  val testSoleTrader: SoleTraderIdEntity = SoleTraderIdEntity(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = Some(testApplicantNino),
    sautr = Some(testSautr),
    trn = None,
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId))

  val testNetpSoleTrader: SoleTraderIdEntity = SoleTraderIdEntity(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = None,
    sautr = Some(testSautr),
    trn = Some(testTrn),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testSafeId),
    identifiersMatch = false
  )

  lazy val validTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(testPersonalDetailsArn),
    isPartOfOrganisation = Some(true),
    organisationName = Some("testCompanyName"),
    telephone = Some("1234"),
    email = Some("test@test.com"),
    emailVerified = Some(true),
    address = Some(address),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  lazy val testBusinessName = "testBusinessName"
  lazy val testHasVrn = true
  lazy val testVrn = "123456782"
  lazy val fullOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(testHasVrn),
    vrn = Some(testVrn),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  )

  val sicCodeId = "81300003"
  val sicCodeDesc = "test2 desc"
  val sicCodeDescCy = "test2 desc cy"
  val businessActivityDescription = "test business desc"

  val validPartnershipIdEntity: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some(testSautr),
    companyName = Some(testCompanyName),
    postCode = Some(testPostcode),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    identifiersMatch = true
  )

  val validSoleTraderIdEntity: SoleTraderIdEntity = SoleTraderIdEntity(
    firstName = testFirstName,
    lastName = testLastName,
    dateOfBirth = testApplicantDob,
    nino = Some(testApplicantNino),
    sautr = Some(testSautr),
    trn = Some(testTrn),
    registration = testRegistration,
    businessVerification = Some(BvPass),
    bpSafeId = Some(testBpSafeId),
    overseas = Some(OverseasIdentifierDetails("1234567890", "UK"))
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

  val validEntitiesNETP: List[Entity] = List[Entity](
    Entity(
      details = Some(validSoleTraderIdEntity),
      partyType = Individual,
      isLeadPartner = Some(true),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    ),
    Entity(
      details = Some(validSoleTraderIdEntity),
      partyType = Individual,
      isLeadPartner = Some(false),
      optScottishPartnershipName = None,
      address = Some(testAddress),
      email = Some("test@foo.com"),
      telephoneNumber = Some("1234567890")
    )
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
      supplyVat1614a = Some(true),
      supplyVat1614h = Some(true),
      supplySupportingDocuments = Some(true),
      additionalPartnersDocuments = Some(false)
    )),
    applicantDetails = Some(validFullApplicantDetailsPartnership),
    business = Some(businessDetails),
    flatRateScheme = Some(flatRateScheme),
    bankAccount = Some(bankAccount),
    vatApplication = Some(fullVatApplication),
    eligibilitySubmissionData = Some(testEligibilitySubmissionDataPartnership),
    entities = Some(validEntities),
    transactorDetails = Some(validTransactorDetails),
    otherBusinessInvolvements = Some(otherBusinessInvolvement1),
    eligibilityJson = Some(fullEligibilityDataJson)
  )

  val fullVatSchemeAttachmentNETP: VatScheme = fullVatSchemeAttachment.copy(
    eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(
      partyType = Individual,
      fixedEstablishmentInManOrUk = false,
      registrationReason = NonUk
    )),
    applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testNetpSoleTrader), personalDetails = Some(testNetpPersonalDetails))),
    business = Some(businessDetails.copy(tradingName = Some(testCompanyName))),
    vatApplication = Some(fullVatApplication.copy(
      overseasCompliance = Some(testFullOverseasCompliance),
      staggerStart = Some(JanDecStagger),
      hasTaxRepresentative = Some(false)
    )),
    bankAccount = None,
    entities = Some(validEntitiesNETP),
  )

  val jsonListSicCode: String =
    s"""
       |  [
       |    {
       |      "code": "01110004",
       |      "desc": "gdfgdg d",
       |      "descCy": "gdfgdg d cy"
       |    },
       |    {
       |      "code": "$sicCodeId",
       |      "desc": "$sicCodeDesc",
       |      "descCy": "$sicCodeDescCy"
       |    },
       |    {
       |      "code": "82190004",
       |      "desc": "ry rty try rty ",
       |      "descCy": "ry rty try rty "
       |    }
       |  ]
        """.stripMargin

  val sicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(models.CurrentProfile("1", VatRegStatus.draft)),
    SIC_CODES_KEY -> Json.parse(jsonListSicCode)
  )

  val iclSicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(models.CurrentProfile("1", VatRegStatus.draft)),
    "ICLFetchResultsUri" -> JsString("/fetch-results")
  )

  val mainBusinessActivity: SicCode = SicCode(sicCodeId, sicCodeDesc, sicCodeDescCy)

  val labourCompliance: LabourCompliance = LabourCompliance(
    supplyWorkers = Some(true),
    numOfWorkersSupplied = Some(200),
    intermediaryArrangement = Some(true),
  )

  val fullModel: Business = Business(
    email = Some("test@foo.com"),
    telephoneNumber = Some("987654"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email),
    welshLanguage = Some(false),
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity),
    businessActivities = Some(List(mainBusinessActivity)),
    labourCompliance = Some(labourCompliance)
  )

  val modelWithoutCompliance: Business = Business(
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity)
  )

}
