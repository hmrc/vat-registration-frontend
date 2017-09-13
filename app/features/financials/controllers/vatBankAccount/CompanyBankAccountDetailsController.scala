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

package models.view.vatFinancials.vatBankAccount {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.Json

  case class CompanyBankAccountDetails(accountName: String, accountNumber: String, sortCode: String)

  object CompanyBankAccountDetails {

    implicit val format = Json.format[CompanyBankAccountDetails]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.companyBankAccountDetails,
      updateF = (c: CompanyBankAccountDetails, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(companyBankAccountDetails = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
      vs.financials.flatMap(_.bankAccount)
        .map(account => CompanyBankAccountDetails(
          accountName = account.accountName,
          accountNumber = account.accountNumber,
          sortCode = account.accountSortCode
        ))
    }
  }
}

package controllers.vatFinancials.vatBankAccount {

  import javax.inject.Inject

  import cats.Show
  import common.ConditionalFlatMap._
  import controllers.vatFinancials.EstimateVatTurnoverKey.lastKnownValueKey
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.vatBankAccount.{CompanyBankAccountDetailsForm, SortCode}
  import models.S4LFlatRateScheme
  import models.view.vatFinancials.EstimateVatTurnover
  import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
  import play.api.mvc._
  import services.{CommonService, S4LService, VatRegistrationService}

  class CompanyBankAccountDetailsController @Inject()(ds: CommonPlayDependencies)
                                                     (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with CommonService {

    val joinThreshold: Long = conf.getLong("thresholds.frs.joinThreshold").get

    val form = CompanyBankAccountDetailsForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[CompanyBankAccountDetails]().map(vm =>
        CompanyBankAccountDetailsForm(
          accountName = vm.accountName.trim,
          accountNumber = "",
          sortCode = SortCode.parse(vm.sortCode).getOrElse(SortCode("", "", ""))))
        .fold(form)(form.fill)
        .map(frm => Ok(features.financials.views.html.vatBankAccount.company_bank_account_details(frm))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(features.financials.views.html.vatBankAccount.company_bank_account_details(badForm)).pure,
        view => for {
          originalTurnover <- keystoreConnector.fetchAndGet[Long](lastKnownValueKey)
          _ <- save(CompanyBankAccountDetails(
            accountName = view.accountName.trim,
            accountNumber = view.accountNumber,
            sortCode = Show[SortCode].show(view.sortCode)))
          _ <- vrs.submitVatFinancials()
          turnover <- viewModel[EstimateVatTurnover]().fold[Long](0)(_.vatTurnoverEstimate)
          _ <- s4l.save(S4LFlatRateScheme()).flatMap(
            _ => vrs.submitVatFlatRateScheme()) onlyIf originalTurnover.getOrElse(0) != turnover
        } yield if (turnover > joinThreshold) {
          Redirect(controllers.routes.SummaryController.show())
        } else {
          Redirect(controllers.frs.routes.JoinFrsController.show())
        }))
  }
}

package forms.vatFinancials.vatBankAccount {

  import cats.Show
  import forms.FormValidation.BankAccount._
  import play.api.data.Form
  import play.api.data.Forms._

  case class CompanyBankAccountDetailsForm(accountName: String, accountNumber: String, sortCode: SortCode)

  object CompanyBankAccountDetailsForm {

    private val ACCOUNT_TYPE = "companyBankAccount"

    val form = Form(
      mapping(
        "accountName" -> text.verifying(accountName(ACCOUNT_TYPE)),
        "accountNumber" -> text.verifying(accountNumber(ACCOUNT_TYPE)),
        "sortCode" -> mapping(
          "part1" -> text,
          "part2" -> text,
          "part3" -> text
        )(SortCode.apply)(SortCode.unapply).verifying(accountSortCode(ACCOUNT_TYPE))
      )(CompanyBankAccountDetailsForm.apply)(CompanyBankAccountDetailsForm.unapply)
    )

  }

  case class SortCode(part1: String, part2: String, part3: String)

  object SortCode {

    val Pattern = """^([0-9]{2})-([0-9]{2})-([0-9]{2})$""".r
    val PartPattern = """^[0-9]{2}$""".r

    def parse(sortCode: String): Option[SortCode] = sortCode match {
      case Pattern(p1, p2, p3) => Some(SortCode(p1, p2, p3))
      case _ => None
    }

    implicit val show: Show[SortCode] = Show.show(sc => {
      val str = Seq(sc.part1.trim, sc.part2.trim, sc.part3.trim).mkString("-")
      //to avoid producing a "--" for sort codes where all double-digits are blank (not entered)
      if (str == "--") "" else str
    })
  }
}