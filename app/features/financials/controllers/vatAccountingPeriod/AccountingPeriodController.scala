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

package models.view.vatFinancials.vatAccountingPeriod {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class AccountingPeriod(accountingPeriod: String)

  object AccountingPeriod {

    val JAN_APR_JUL_OCT = "JAN_APR_JUL_OCT"
    val FEB_MAY_AUG_NOV = "FEB_MAY_AUG_NOV"
    val MAR_JUN_SEP_DEC = "MAR_JUN_SEP_DEC"

    val valid = (item: String) => List(JAN_APR_JUL_OCT, FEB_MAY_AUG_NOV, MAR_JUN_SEP_DEC).contains(item.toUpperCase)

    implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.accountingPeriod,
      updateF = (c: AccountingPeriod, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(accountingPeriod = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
      for {
        f <- vs.financials
        ps <- f.accountingPeriods.periodStart
      } yield AccountingPeriod(ps.toUpperCase())
    }
  }
}

package controllers.vatFinancials.vatAccountingPeriod {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.vatAccountingPeriod.AccountingPeriodForm
  import models.api.{VatEligibilityChoice, VatScheme}
  import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class AccountingPeriodController @Inject()(ds: CommonPlayDependencies,
                                             val keystoreConnector: KeystoreConnect,
                                             val authConnector: AuthConnector,
                                             implicit val s4LService: S4LService,
                                             implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form = AccountingPeriodForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[AccountingPeriod]().fold(form)(form.fill)
                .map(f => Ok(features.financials.views.html.vatAccountingPeriod.accounting_period(f)))
            }
          }
    }

    private[controllers] def extractEligiblityChoice(vs : VatScheme) : Boolean = {
      vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice).map(_.necessity.contains(VatEligibilityChoice.NECESSITY_VOLUNTARY))
        .fold(throw new RuntimeException(""))(contains => contains)
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.financials.views.html.vatAccountingPeriod.accounting_period(badForm)).pure,
                data => for {
                  _ <- save(data)
                  vs <- vrs.getVatScheme
                } yield Redirect(if (extractEligiblityChoice(vs)) {
                  controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()
                } else {
                  controllers.vatTradingDetails.vatChoice.routes.MandatoryStartDateController.show()
                }))
            }
          }
    }
  }
}
package forms.vatFinancials.vatAccountingPeriod {

  import forms.FormValidation._
  import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod
  import play.api.data.Form
  import play.api.data.Forms._

  object AccountingPeriodForm {
    val RADIO_ACCOUNTING_PERIOD: String = "accountingPeriodRadio"

    val form = Form(
      mapping(
        RADIO_ACCOUNTING_PERIOD -> textMapping()("accounting.period").verifying(AccountingPeriod.valid)
      )(AccountingPeriod.apply)(AccountingPeriod.unapply)
    )
  }
}
