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
  import play.api.libs.json.Json

  case class VatReturnFrequency(frequencyType: String)

  object VatReturnFrequency {

    val MONTHLY = "monthly"
    val QUARTERLY = "quarterly"

    val monthly = VatReturnFrequency(MONTHLY)
    val quarterly = VatReturnFrequency(QUARTERLY)

    val valid = (item: String) => List(MONTHLY, QUARTERLY).contains(item.toLowerCase)

    implicit val format = Json.format[VatReturnFrequency]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.vatReturnFrequency,
      updateF = (c: VatReturnFrequency, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(vatReturnFrequency = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
      vs.financials map (vf => VatReturnFrequency(vf.accountingPeriods.frequency))
    }
  }
}

package controllers.vatFinancials.vatAccountingPeriod {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.vatAccountingPeriod.VatReturnFrequencyForm
  import models.S4LVatFinancials
  import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
  import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency.MONTHLY
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
  import play.api.mvc._
  import services.{CommonService, S4LService, VatRegistrationService}

  class VatReturnFrequencyController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with CommonService with FlatMapSyntax {

    val joinThreshold: Long = conf.getLong("thresholds.frs.joinThreshold").get

    val form = VatReturnFrequencyForm.form

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[VatReturnFrequency]().fold(form)(form.fill)
        .map(frm => Ok(features.financials.views.html.vatAccountingPeriod.vat_return_frequency(frm))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(features.financials.views.html.vatAccountingPeriod.vat_return_frequency(badForm)).pure,
        view => save(view).map(_ => view.frequencyType == MONTHLY).ifM(
          ifTrue = for {
            container <- s4lContainer[S4LVatFinancials]()
            _ <- s4l.save(container.copy(accountingPeriod = None))
            voluntaryReg <- viewModel[VoluntaryRegistration]().fold(true)(reg => reg == VoluntaryRegistration.yes)
          } yield if (voluntaryReg) {
            controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()
          } else {
            controllers.vatTradingDetails.vatChoice.routes.MandatoryStartDateController.show()
          },
          ifFalse = controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show().pure
        ).map(Redirect)))
  }
}

package forms.vatFinancials.vatAccountingPeriod {

  import forms.FormValidation.textMapping
  import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
  import play.api.data.Form
  import play.api.data.Forms._

  object VatReturnFrequencyForm {

    val RADIO_FREQUENCY: String = "vatReturnFrequencyRadio"

    val form = Form(
      mapping(
        RADIO_FREQUENCY -> textMapping()("vat.return.frequency").verifying(VatReturnFrequency.valid)
      )(VatReturnFrequency.apply)(VatReturnFrequency.unapply)
    )
  }
}