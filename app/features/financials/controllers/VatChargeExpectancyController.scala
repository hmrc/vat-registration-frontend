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

package models.view.vatFinancials {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class VatChargeExpectancy(yesNo: String)

  object VatChargeExpectancy {

    val VAT_CHARGE_YES = "VAT_CHARGE_YES"
    val VAT_CHARGE_NO = "VAT_CHARGE_NO"

    val yes = VatChargeExpectancy(VAT_CHARGE_YES)
    val no = VatChargeExpectancy(VAT_CHARGE_NO)

    val valid = (item: String) => List(VAT_CHARGE_YES, VAT_CHARGE_NO).contains(item.toUpperCase)

    implicit val format: OFormat[VatChargeExpectancy] = Json.format[VatChargeExpectancy]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.vatChargeExpectancy,
      updateF = (c: VatChargeExpectancy, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(vatChargeExpectancy = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
      vs.financials.map(_.reclaimVatOnMostReturns).collect {
        case true => VatChargeExpectancy(VAT_CHARGE_YES)
        case false => VatChargeExpectancy(VAT_CHARGE_NO)
      }
    }
  }
}

package controllers.vatFinancials {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.VatChargeExpectancyForm
  import models.view.vatFinancials.VatChargeExpectancy
  import models.view.vatFinancials.VatChargeExpectancy.VAT_CHARGE_YES
  import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, VatRegistrationService}


  class VatChargeExpectancyController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4LService: S4LService,
                                                vatRegistrationService: VatRegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax {

    val form = VatChargeExpectancyForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[VatChargeExpectancy]().fold(form)(form.fill)
        .map(f => Ok(features.financials.views.html.vat_charge_expectancy(f))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(features.financials.views.html.vat_charge_expectancy(badForm)).pure,
        view => save(view).map(_ => view.yesNo == VAT_CHARGE_YES).ifM(
          ifTrue = controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show().pure,
          ifFalse = save(VatReturnFrequency(VatReturnFrequency.QUARTERLY))
            .map(_ => controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show())
        ).map(Redirect)))
  }
}

package forms.vatFinancials {

  import forms.FormValidation.textMapping
  import models.view.vatFinancials.VatChargeExpectancy
  import play.api.data.Form
  import play.api.data.Forms._

  object VatChargeExpectancyForm {

    val RADIO_YES_NO: String = "vatChargeRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping()("vat.charge.expectancy").verifying(VatChargeExpectancy.valid)
      )(VatChargeExpectancy.apply)(VatChargeExpectancy.unapply)
    )
  }
}
