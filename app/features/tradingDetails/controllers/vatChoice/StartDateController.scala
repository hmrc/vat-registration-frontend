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

package models.view.vatTradingDetails.vatChoice {

  import java.time.LocalDate

  import models._
  import models.api.{VatScheme, VatStartDate}
  import play.api.libs.json.Json

  import scala.util.Try

  case class StartDateView(dateType: String = "", date: Option[LocalDate] = None, ctActiveDate: Option[LocalDate] = None) {

    def withCtActiveDateOption(d: LocalDate): StartDateView = this.copy(ctActiveDate = Some(d))

  }

  object StartDateView {

    def bind(dateType: String, dateModel: Option[DateModel]): StartDateView =
      StartDateView(dateType, dateModel.flatMap(_.toLocalDate))

    def unbind(startDate: StartDateView): Option[(String, Option[DateModel])] =
      Try {
        startDate.date.fold((startDate.dateType, Option.empty[DateModel])) {
          d => (startDate.dateType, Some(DateModel.fromLocalDate(d)))
        }
      }.toOption

    val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
    val BUSINESS_START_DATE = "BUSINESS_START_DATE"
    val SPECIFIC_DATE = "SPECIFIC_DATE"

    val validSelection: String => Boolean = Seq(COMPANY_REGISTRATION_DATE, BUSINESS_START_DATE, SPECIFIC_DATE).contains

    implicit val format = Json.format[StartDateView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LTradingDetails) => group.startDate,
      updateF = (c: StartDateView, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(startDate = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[StartDateView] { vs: VatScheme =>
      vs.tradingDetails.map(_.vatChoice.vatStartDate).collect {
        case VatStartDate(dateType, d@_) => StartDateView(dateType, d)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import java.time.LocalDate
  import javax.inject.Inject

  import common.Now
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatTradingDetails.vatChoice.StartDateFormFactory
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import play.api.data.Form
  import play.api.mvc._
  import services.{PrePopService, S4LService, VatRegistrationService}
  import uk.gov.hmrc.play.http.HeaderCarrier
  import features.tradingDetails.views.html.vatChoice.start_date

  import scala.concurrent.Future

  class StartDateController @Inject()(startDateFormFactory: StartDateFormFactory, iis: PrePopService, ds: CommonPlayDependencies)
                                     (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

    val form: Form[StartDateView] = startDateFormFactory.form()

    protected[controllers]
    def populateCtActiveDate(vm: StartDateView)(implicit hc: HeaderCarrier, today: Now[LocalDate]): Future[StartDateView] =
      iis.getCTActiveDate().filter(today().plusMonths(3).isAfter).fold(vm)(vm.withCtActiveDateOption)

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      viewModel[StartDateView]().getOrElse(StartDateView())
        .flatMap(populateCtActiveDate).map(f => Ok(start_date(form.fill(f)))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      startDateFormFactory.form().bindFromRequest().fold(
        badForm => BadRequest(start_date(badForm)).pure,
        goodForm => populateCtActiveDate(goodForm).flatMap(vm => save(vm)).map(_ =>
          vrs.submitTradingDetails()).map(_ =>
          Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()))))
  }
}

package forms.vatTradingDetails.vatChoice {

  import java.time.LocalDate
  import javax.inject.Inject

  import common.Now
  import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
  import forms.FormValidation.{inRange, textMapping}
  import models.DateModel
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import play.api.data.Form
  import play.api.data.Forms._
  import services.DateService
  import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

  class StartDateFormFactory @Inject()(dateService: DateService, today: Now[LocalDate]) {

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val RADIO_INPUT_NAME = "startDateRadio"

    def form(): Form[StartDateView] = {

      val minDate: LocalDate = dateService.addWorkingDays(today(), 2)
      val maxDate: LocalDate = today().plusMonths(3)
      implicit val specificErrorCode: String = "startDate"

      Form(
        mapping(
          RADIO_INPUT_NAME -> textMapping()("startDate.choice").verifying(StartDateView.validSelection),
          "startDate" -> mandatoryIf(
            isEqual(RADIO_INPUT_NAME, StartDateView.SPECIFIC_DATE),
            mapping(
              "day" -> text,
              "month" -> text,
              "year" -> text
            )(DateModel.apply)(DateModel.unapply).verifying(
              nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
          )
        )(StartDateView.bind)(StartDateView.unbind)
      )
    }
  }
}
