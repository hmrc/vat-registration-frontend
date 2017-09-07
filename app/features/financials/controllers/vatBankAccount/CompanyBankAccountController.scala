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

  case class CompanyBankAccount(yesNo: String)

  object CompanyBankAccount {

    val COMPANY_BANK_ACCOUNT_YES = "COMPANY_BANK_ACCOUNT_YES"
    val COMPANY_BANK_ACCOUNT_NO = "COMPANY_BANK_ACCOUNT_NO"

    val yes = CompanyBankAccount(COMPANY_BANK_ACCOUNT_YES)
    val no = CompanyBankAccount(COMPANY_BANK_ACCOUNT_NO)

    val valid = (item: String) => List(COMPANY_BANK_ACCOUNT_YES, COMPANY_BANK_ACCOUNT_NO).contains(item.toUpperCase)

    implicit val format = Json.format[CompanyBankAccount]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.companyBankAccount,
      updateF = (c: CompanyBankAccount, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(companyBankAccount = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
      vs.financials.map(_.bankAccount.fold(CompanyBankAccount.no)(_ => CompanyBankAccount.yes))
    }
  }
}

package controllers.vatFinancials.vatBankAccount {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import cats.syntax.cartesian._
  import common.ConditionalFlatMap._
  import controllers.vatFinancials.EstimateVatTurnoverKey.lastKnownValueKey
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.vatBankAccount.CompanyBankAccountForm
  import models._
  import models.view.vatFinancials.EstimateVatTurnover
  import models.view.vatFinancials.vatBankAccount.CompanyBankAccount
  import play.api.mvc._
  import services.{CommonService, S4LService, VatRegistrationService}

  class CompanyBankAccountController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax with CommonService {

    val joinThreshold: Long = conf.getLong("thresholds.frs.joinThreshold").get

    val form = CompanyBankAccountForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[CompanyBankAccount]().fold(form)(form.fill)
        .map(frm => Ok(features.financials.views.html.vatBankAccount.company_bank_account(frm))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(features.financials.views.html.vatBankAccount.company_bank_account(badForm)).pure,
        data => save(data).map(_ => data.yesNo == CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES).ifM(
          ifTrue = controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show().pure,
          ifFalse = for {
            originalTurnover <- keystoreConnector.fetchAndGet[Long](lastKnownValueKey)
            container <- s4lContainer[S4LVatFinancials]()
            _ <- s4l.save(container.copy(companyBankAccountDetails = None))
            _ <- vrs.submitVatFinancials()
            turnover <- viewModel[EstimateVatTurnover]().fold[Long](0)(_.vatTurnoverEstimate)
            _ <- s4l.save(S4LFlatRateScheme()).flatMap(
              _ => vrs.submitVatFlatRateScheme()) onlyIf originalTurnover.getOrElse(0) != turnover
          } yield if (turnover > joinThreshold) {
            controllers.routes.SummaryController.show()
          } else {
            controllers.frs.routes.JoinFrsController.show()
          }
        ).map(Redirect)))
  }
}

package forms.vatFinancials.vatBankAccount {

  import forms.FormValidation.textMapping
  import models.view.vatFinancials.vatBankAccount.CompanyBankAccount
  import play.api.data.Form
  import play.api.data.Forms._

  object CompanyBankAccountForm {
    val RADIO_YES_NO: String = "companyBankAccountRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("company.bank.account").verifying(CompanyBankAccount.valid)
      )(CompanyBankAccount.apply)(CompanyBankAccount.unapply)
    )
  }
}
