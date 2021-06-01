/*
 * Copyright 2021 HM Revenue & Customs
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

package viewmodels.checkyouranswers

import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import fixtures.VatRegistrationFixture
import models._
import models.api._
import models.api.returns._
import models.view.SummaryRow
import testHelpers.VatRegSpec
import viewmodels.SummaryCheckYourAnswersBuilder

import java.time.LocalDate

class SummaryCheckYourAnswersBuilderSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  def returnsWithStartDate(startDate: Option[LocalDate] = Some(LocalDate.now())): Option[Returns] =
    Some(Returns(None, None, None, None, startDate))

  val serviceName = "vat-registration-eligibility-frontend"
  val accountName = "testName"
  val accountNumber = "12345678"
  val sortCode = "12-34-56"
  val bankDetails = BankAccountDetails(accountName, sortCode, accountNumber)
  val bankAccountNotProvided = BankAccount(isProvided = false, None, None)

  val bankAccountIsProvided = BankAccount(
    isProvided = true,
    Some(bankDetails),
    None
  )

  val hasCompanyBankAccountUrl = controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView()
  val enterCompanyBankAccountDetailsUrl = controllers.routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()

  val sectionBuilder = SummaryCheckYourAnswersBuilder(validVatScheme,
    ApplicantDetails(),
    Some(5000L),
    Some("Foo Bar Wizz Bang"),
    Some(TurnoverEstimates(100L)),
    threshold = optVoluntaryRegistration,
    returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))
    )
  )

  "The applicant/transactor personal details rows" when {
    "the UseSoleTraderIdentification feature switch is enabled" must {
      "show the correct rows with the STI change link" in {
        enable(UseSoleTraderIdentification)

        val sectionBuilder = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeEmptySicAndCompliance,
          ApplicantDetails(transactor = Some(testTransactorDetails)),
          Some(5000L),
          Some("Foo Bar Wizz Bang"),
          Some(TurnoverEstimates(100L)),
          threshold = optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
        )

        sectionBuilder.firstName mustBe SummaryRow(
          id = "directorDetails.firstName",
          answerMessageKey = testTransactorDetails.firstName,
          changeLink = Some(controllers.registration.applicant.routes.SoleTraderIdentificationController.startJourney())
        )

        sectionBuilder.lastName mustBe SummaryRow(
          id = "directorDetails.lastName",
          answerMessageKey = testTransactorDetails.lastName,
          changeLink = Some(controllers.registration.applicant.routes.SoleTraderIdentificationController.startJourney())
        )

        sectionBuilder.nino mustBe SummaryRow(
          id = "directorDetails.nino",
          answerMessageKey = testTransactorDetails.nino,
          changeLink = Some(controllers.registration.applicant.routes.SoleTraderIdentificationController.startJourney())
        )

        sectionBuilder.dob mustBe SummaryRow(
          id = "directorDetails.dob",
          answerMessageKey = "1 January 2020",
          changeLink = Some(controllers.registration.applicant.routes.SoleTraderIdentificationController.startJourney())
        )
      }
    }
    "the UseSoleTraderIdentification feature switch is disabled" must {
      "show the correct rows with the PDV change link" in {
        disable(UseSoleTraderIdentification)

        val sectionBuilder = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeEmptySicAndCompliance,
          ApplicantDetails(transactor = Some(testTransactorDetails)),
          Some(5000L),
          Some("Foo Bar Wizz Bang"),
          Some(TurnoverEstimates(100L)),
          threshold = optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
        )

        sectionBuilder.firstName mustBe SummaryRow(
          id = "directorDetails.firstName",
          answerMessageKey = testTransactorDetails.firstName,
          changeLink = Some(controllers.registration.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
        )

        sectionBuilder.lastName mustBe SummaryRow(
          id = "directorDetails.lastName",
          answerMessageKey = testTransactorDetails.lastName,
          changeLink = Some(controllers.registration.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
        )

        sectionBuilder.nino mustBe SummaryRow(
          id = "directorDetails.nino",
          answerMessageKey = testTransactorDetails.nino,
          changeLink = Some(controllers.registration.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
        )

        sectionBuilder.dob mustBe SummaryRow(
          id = "directorDetails.dob",
          answerMessageKey = "1 January 2020",
          changeLink = Some(controllers.registration.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
        )
      }
    }
  }

  "The section builder composing a company details section" must {
    val sectionBuilder = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeEmptySicAndCompliance,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
    )

    val sectionBuilderFilledSicAndCompliance = viewmodels.SummaryCheckYourAnswersBuilder(validVatScheme,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
    )

    "with companyBusinessDescriptionRow render" must {
      "a 'No' value should be returned with an empty description in sic and compliance" in {
        sectionBuilder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "directorDetails.businessDescription",
            "app.common.no",
            Some(controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show())
          )
      }
      "a business activity description in sic and compliance should be shown when one is entered by the user" in {
        sectionBuilderFilledSicAndCompliance.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "directorDetails.businessDescription",
            testBusinessActivityDescription,
            Some(controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show())
          )
      }
      "a main business activity in sic and compliance should be shown when one is entered by the user" in {
        sectionBuilderFilledSicAndCompliance.mainActivityRow mustBe
          SummaryRow(
            "directorDetails.mainSicCode",
            sicCode.description,
            Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
          )
      }
    }
    "with section generate" must {
      "a valid summary section" in {
        sectionBuilder.section.id mustBe "directorDetails"
        sectionBuilder.section.rows.length mustEqual 45
      }
    }
  }

  "The section builder composing the company contact details section" must {
    val sectionBuilder = viewmodels.SummaryCheckYourAnswersBuilder(validVatScheme,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))
      )
    )
    "render the trading name row" in {
      sectionBuilder.tradingNameRow mustBe SummaryRow(
        "directorDetails.tradingName",
        "ACME Ltd.",
        Some(controllers.registration.business.routes.TradingNameController.show())
      )
    }
    "render the Principal place of business address row" in {
      sectionBuilder.ppobRow mustBe SummaryRow(
        "directorDetails.ppob",
        Address.normalisedSeq(testAddress),
        Some(controllers.registration.business.routes.PpobAddressController.startJourney())
      )
    }
    "render the phone number row" in {
      sectionBuilder.businessDaytimePhoneNumberRow mustBe SummaryRow(
        "directorDetails.daytimePhoneBusiness",
        "123",
        Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
      )
    }
    "render the mobile number row" in {
      sectionBuilder.businessMobilePhoneNumberRow mustBe SummaryRow(
        "directorDetails.mobileBusiness",
        "987654",
        Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
      )
    }
    "render the email address row" in {
      sectionBuilder.businessEmailRow mustBe SummaryRow(
        "directorDetails.emailBusiness",
        "test@foo.com",
        Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
      )
    }
    "render the website row" in {
      sectionBuilder.businessWebsiteRow mustBe SummaryRow(
        "directorDetails.website",
        "/test/url",
        Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
      )
    }
    "render the contact preference row" in {
      sectionBuilder.contactPreferenceRow mustBe SummaryRow(
        "directorDetails.contactPreference",
        Email.toString,
        Some(controllers.routes.ContactPreferenceController.showContactPreference())
      )
    }
    "render the business description row" in {
      sectionBuilder.companyBusinessDescriptionRow mustBe SummaryRow(
        "directorDetails.businessDescription",
        testBusinessActivityDescription,
        Some(controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show())
      )
    }
    "render the main sic code row" in {
      sectionBuilder.mainActivityRow mustBe SummaryRow(
        "directorDetails.mainSicCode",
        sicCode.description,
        Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
      )
    }
    "render the sic codes row" in {
      sectionBuilder.sicCodesRow mustBe SummaryRow(
        "directorDetails.sicCodes",
        sicCode.description,
        Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
      )
    }
    "render the zero rated value row" in {
      sectionBuilder.zeroRatedRow mustBe SummaryRow(
        "directorDetails.zeroRated",
        "£10,000.50",
        Some(controllers.registration.returns.routes.ZeroRatedSuppliesController.show())
      )
    }
    "render the expect claim refunds row" in {
      sectionBuilder.expectClaimRefundsRow mustBe SummaryRow(
        "directorDetails.claimRefunds",
        "app.common.no",
        Some(controllers.registration.returns.routes.ClaimRefundsController.show())
      )
    }
    "render the EU goods row" in {
      sectionBuilder.applyForEoriRow mustBe SummaryRow(
        "directorDetails.applyForEori",
        "app.common.yes",
        Some(controllers.registration.business.routes.ApplyForEoriController.show())
      )
    }
  }
  "accountIsProvidedRow" must {
    val sectionBuilderNoBank = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeNoBank,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))
      )
    )

    "return a isProvided row when isProvided is true" in {
      sectionBuilder.accountIsProvidedRow mustBe
        SummaryRow(
          "directorDetails.companyBankAccount",
          "app.common.yes",
          Some(hasCompanyBankAccountUrl)
        )
    }

    "return a isProvided row when isProvided is false" in {
      sectionBuilderNoBank.accountIsProvidedRow mustBe
        SummaryRow(
          "directorDetails.companyBankAccount",
          "app.common.no",
          Some(hasCompanyBankAccountUrl)
        )
    }

    "companyBankAccountNameRow" must {

      "return an account name row when one is provided in the details block" in {
        sectionBuilder.companyBankAccountDetails mustBe
          SummaryRow(
            "directorDetails.companyBankAccount.details",
            BankAccountDetails.bankSeq(bankDetails),
            Some(enterCompanyBankAccountDetailsUrl)
          )
      }

      "return an account details row when the details block is empty" in {
        sectionBuilderNoBank.companyBankAccountDetails mustBe
          SummaryRow(
            "directorDetails.companyBankAccount.details",
            Seq.empty,
            Some(enterCompanyBankAccountDetailsUrl)
          )
      }
    }
  }
  "Correct compliance section should be rendered" when {

    "labour questions have been answered by user" in {
      sectionBuilder.section.id mustBe "directorDetails"
    }
  }

  "summary builder should build frs summary with data if frs present" in {

    sectionBuilder.joinFrsRow.answerMessageKeys.head mustBe "app.common.yes"
    sectionBuilder.costsInclusiveRow.answerMessageKeys.head mustBe "app.common.yes"
    sectionBuilder.estimateTotalSalesRow.answerMessageKeys.head mustBe "£5,003"
    sectionBuilder.costsLimitedRow.answerMessageKeys.head mustBe "app.common.yes"
    sectionBuilder.flatRatePercentageRow.answerMessageKeys.head mustBe "app.common.yes"
    sectionBuilder.businessSectorRow.answerMessageKeys.head mustBe "Foo Bar Wizz Bang"
  }

  "The section builder composing a labour details section" must {
    val sectionBuilderWithLabour = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeWithLabour,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))
      )
    )

    "providingWorkersRow render" must {
      "'Yes' selected providingWorkersRow " in {
        sectionBuilderWithLabour.providingWorkersRow mustBe
          SummaryRow(
            "directorDetails.supplyWorkers",
            "app.common.yes",
            Some(controllers.registration.sicandcompliance.routes.SupplyWorkersController.show())
          )
      }
      "'No' selected for providingWorkersRow" in {
        sectionBuilder.providingWorkersRow mustBe
          SummaryRow(
            "directorDetails.supplyWorkers",
            "app.common.no",
            Some(controllers.registration.sicandcompliance.routes.SupplyWorkersController.show())
          )
      }
    }

    "numberOfWorkers render" must {
      "render a row" in {
        sectionBuilderWithLabour.numberOfWorkersRow mustBe
          SummaryRow(
            "directorDetails.numberOfWorkers",
            "12",
            Some(controllers.registration.sicandcompliance.routes.WorkersController.show())
          )
      }
    }

    "intermediaryArrangementRow render" must {
      " 'No' selected intermediaryArrangementRow " in {
        sectionBuilder.intermediarySupplyRow mustBe
          SummaryRow(
            "directorDetails.intermediarySupply",
            "app.common.no",
            Some(controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show())
          )
      }
      " 'YES' selected for intermediaryArrangementRow" in {
        sectionBuilderWithLabour.intermediarySupplyRow mustBe
          SummaryRow(
            "directorDetails.intermediarySupply",
            "app.common.yes",
            Some(controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show())
          )
      }
    }
  }

  "The section builder composing a vat details section" must {
    val sectionBuilderNoStartDate = viewmodels.SummaryCheckYourAnswersBuilder(validVatScheme,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optMandatoryRegistrationBothDates,
      returnsBlock = returnsWithStartDate(None)
    )

    val sectionBuilderNoTradingDetails = viewmodels.SummaryCheckYourAnswersBuilder(validVatSchemeNoTradingDetails,
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optVoluntaryRegistration,
      returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))
      )
    )

    "with startDateRow render" must {
      "a date with format 'd MMMM y' if it's a voluntary registration where they are not incorped" in {
        val expectedRow = SummaryRow(
          "directorDetails.startDate",
          "21 March 2017",
          Some(controllers.registration.returns.routes.ReturnsController.voluntaryStartPage())
        )

        sectionBuilder.startDateRow mustBe expectedRow
      }
      "a Companies House incorporation date message, if it's a mandatory registration and they are not incorped" in {
        sectionBuilderNoStartDate.startDateRow mustBe SummaryRow("directorDetails.startDate", "pages.summary.directorDetails.mandatoryStartDate", None)
      }
    }
    "with tradingNameRow render" must {
      "a trading name if there's one" in {
        sectionBuilder.tradingNameRow mustBe SummaryRow(
          "directorDetails.tradingName",
          "ACME Ltd.",
          Some(controllers.registration.business.routes.TradingNameController.show())
        )
      }
      "a 'No' if there isn't a trading name" in {
        sectionBuilderNoTradingDetails.tradingNameRow mustBe SummaryRow("directorDetails.tradingName", "app.common.no", Some(controllers.registration.business.routes.TradingNameController.show()))
      }
    }
  }

  "The annual accounting rows" must {
    val sectionWithAnnualAccounting = viewmodels.SummaryCheckYourAnswersBuilder(
      validVatScheme.copy(returns = Some(Returns(
        zeroRatedSupplies = Some(1000),
        reclaimVatOnMostReturns = Some(true),
        returnsFrequency = Some(Annual),
        staggerStart = Some(FebJanStagger),
        annualAccountingDetails = Some(AASDetails(
          Some(MonthlyPayment),
          Some(BankGIRO)
        ))
      ))),
      ApplicantDetails(),
      Some(5000L),
      Some("Foo Bar Wizz Bang"),
      Some(TurnoverEstimates(100L)),
      threshold = optMandatoryRegistrationBothDates,
      returnsBlock = returnsWithStartDate(None)
    )

    "be rendered when the returns frequency is Annual" in {
      sectionWithAnnualAccounting.accountingPeriodRow mustBe SummaryRow(
        "directorDetails.accountingPeriod",
        "pages.summary.directorDetails.accountingPeriod.annual",
        Some(controllers.registration.returns.routes.ReturnsController.accountPeriodsPage())
      )

      sectionWithAnnualAccounting.lastMonthOfAccountingYearRow mustBe SummaryRow(
        "directorDetails.lastMonthOfAccountingYear",
        "pages.summary.directorDetails.lastMonthOfAccountingYear.FebJanStagger",
        Some(controllers.registration.returns.routes.LastMonthOfAccountingYearController.show())
      )

      sectionWithAnnualAccounting.paymentFrequencyRow mustBe SummaryRow(
        "directorDetails.paymentFrequency",
        "pages.summary.directorDetails.paymentFrequency.MonthlyPayment",
        Some(controllers.registration.returns.routes.PaymentFrequencyController.show())
      )

      sectionWithAnnualAccounting.paymentMethodRow mustBe SummaryRow(
        "directorDetails.paymentMethod",
        "pages.summary.directorDetails.paymentMethod.BankGIRO",
        Some(controllers.registration.returns.routes.PaymentMethodController.show())
      )
    }
  }

}
