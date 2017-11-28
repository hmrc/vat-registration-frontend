
package controllers

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import controllers.vatFinancials.vatBankAccount.CompanyBankAccountDetailsController
import models.{S4LFlatRateScheme, S4LVatFinancials}
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, VatChargeExpectancy, ZeroRatedSales}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class CompanyBankAccountDetailsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  def controller: CompanyBankAccountDetailsController = app.injector.instanceOf(classOf[CompanyBankAccountDetailsController])
  val testName = "testName"
  val testAccountNumber = "12345678"
  val testSortCode = "123456"

  val bankAccountDetails = CompanyBankAccountDetails("testName","12345678","123456")

  val testformData = Map (
    "accountName" -> testName,
    "accountNumber" -> testAccountNumber,
    "sortCode.part1" -> "12",
    "sortCode.part2" -> "34",
    "sortCode.part3" -> "56"
  )

  val s4lVatFinancialsNoBankDetails = S4LVatFinancials(
    estimateVatTurnover = Some(EstimateVatTurnover(vatTurnoverEstimate = 120000L)),
    zeroRatedTurnover = Some(ZeroRatedSales(yesNo = "yes")),
    zeroRatedTurnoverEstimate = Some(EstimateZeroRatedSales(zeroRatedTurnoverEstimate = 12000L)),
    vatChargeExpectancy = Some(VatChargeExpectancy(yesNo = "no")),
    vatReturnFrequency = Some(VatReturnFrequency(frequencyType = "monthly")),
    accountingPeriod = Some(AccountingPeriod(accountingPeriod =  "jan_apr_jul_oct")),
    companyBankAccount = Some(CompanyBankAccount(yesNo = "yes"))
  )

  "Submit compamnyBankAccountDetails" should {

    val updatedS4lVatFinancials = s4lVatFinancialsNoBankDetails.copy(companyBankAccountDetails = Some(bankAccountDetails))

    "redirect if bank account details are valid" in {
      given()
        .postRequest(testformData)
        .user.isAuthorised
        .currentProfile.withProfile()
        .bankAccountReputation.passes
        .s4lContainerInScenario[S4LVatFinancials].contains(s4lVatFinancialsNoBankDetails, Some(STARTED))
        .s4lContainerInScenario[S4LVatFinancials].isUpdatedWith(updatedS4lVatFinancials, Some(STARTED), Some("Vat Financials Updated"))
        .vatScheme.isBlank
        .s4lContainerInScenario[S4LVatFinancials].contains(updatedS4lVatFinancials, Some("Vat Financials Updated"), Some("Flat Rate Scheme Updated"))
        .s4lContainerInScenario[S4LFlatRateScheme].isUpdatedWith(S4LFlatRateScheme(), Some("Flat Rate Scheme Updated"))
        .vatScheme.isUpdatedWith[VatFinancials](S4LVatFinancials.apiT.toApi(updatedS4lVatFinancials))

      whenReady(controller.submit(request))(res => res.header.status mustBe 303)
    }

    "return a badrequest if bank account details aren't valid" in {
      given()
        .postRequest(testformData)
        .user.isAuthorised
        .currentProfile.withProfile()
        .bankAccountReputation.fails

      whenReady(controller.submit(request))(res => res.header.status mustBe 400)
    }
  }
}
