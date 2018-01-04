/*
 * Copyright 2018 HM Revenue & Customs
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

//TODO: Remove once the aux refactor has taken place

package models.view.vatTradingDetails.vatChoice {

 
  import java.time.LocalDate

  import models._
  import models.api.{VatEligibilityChoice, VatScheme, VatStartDate}
  import play.api.libs.json.Json

  import scala.util.Try

  @deprecated
  case class StartDateView(dateType: String = "", date: Option[LocalDate] = None, ctActiveDate: Option[LocalDate] = None) {
    def withCtActiveDateOption(d: LocalDate): StartDateView = this.copy(ctActiveDate = Some(d))
  }

  @deprecated
  object StartDateView {

    def bind(dateType: String, dateModel: Option[DateModel])(implicit defaultDate : Option[LocalDate] = None): StartDateView =
      StartDateView(dateType, if (dateType == COMPANY_REGISTRATION_DATE) defaultDate else dateModel.flatMap(_.toLocalDate))

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
      vs.tradingDetails.map(_.vatChoice.vatStartDate) match {
        case Some(VatStartDate(dateType, d@_)) => Some(StartDateView(dateType, d))
        case None if vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice.map(_.necessity)).contains(VatEligibilityChoice.NECESSITY_OBLIGATORY) =>
          Some(StartDateView(COMPANY_REGISTRATION_DATE))
        case _ => None
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import java.time.LocalDate
  import javax.inject.{Inject, Singleton}

  import common.Now
  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import features.tradingDetails.views.html.vatChoice.{start_date, start_date_incorp}
  import forms.vatTradingDetails.vatChoice.StartDateFormFactory
  import models.CurrentProfile
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import play.api.data.Form
  import play.api.mvc._
  import services._
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  @deprecated
  @Singleton
  class StartDateController @Inject()(startDateFormFactory: StartDateFormFactory,
                                      prepopService: PrePopService,
                                      playDep: CommonPlayDependencies,
                                      val keystoreConnector: KeystoreConnect,
                                      val authConnector: AuthConnector,
                                      implicit val s4LService: S4LService,
                                      implicit val vrs: RegistrationService) extends VatRegistrationController(playDep) with SessionProfile {

    val form: Form[StartDateView] = startDateFormFactory.form()

    @deprecated
    protected[controllers]
    def populateCtActiveDate(vm: StartDateView)(implicit hc: HeaderCarrier, profile: CurrentProfile, today: Now[LocalDate]): Future[StartDateView] =
      prepopService.getCTActiveDate.filter(today().plusMonths(3).isAfter).fold(vm)(vm.withCtActiveDateOption)

    @deprecated
    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[StartDateView]().getOrElse(StartDateView()).map(viewModel =>
                (profile.incorporationDate, viewModel.date) match {
                  case (Some(incorpDate), Some(date)) if incorpDate != date => viewModel.copy(dateType = StartDateView.SPECIFIC_DATE)
                  case _ => viewModel
                }
              ).flatMap(populateCtActiveDate).map(f =>
                Ok(profile.incorporationDate.fold(start_date(form.fill(f))) { date => start_date_incorp(form.fill(f), date) })
              )
            }
          }
    }

    @deprecated
    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              startDateFormFactory.form(profile.incorporationDate).bindFromRequest().fold(
                badForm => profile.incorporationDate.fold(BadRequest(start_date(badForm)).pure) { date =>
                  BadRequest(start_date_incorp(badForm, date)).pure
                },
                goodForm => populateCtActiveDate(goodForm).flatMap(vm => save(vm)).map(_ =>
                  vrs.submitTradingDetails()).map(_ =>
                  Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())))
            }
          }
    }
  }
}

package forms.vatTradingDetails.vatChoice {

  import java.time.LocalDate
  import javax.inject.Inject

  import common.Now
  import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
  import forms.FormValidation.{ErrorCode, textMapping}
  import models.DateModel
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import play.api.Logger
  import play.api.data.Form
  import play.api.data.Forms._
  import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
  import services.DateService
  import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

  @deprecated
  trait MinimumDateValidation {
    val date : LocalDate
  }
  @deprecated
  case class ToIncorpMinimumFutureDate(date : LocalDate) extends MinimumDateValidation
  @deprecated
  case class FourYearsSinceIncorporatedDate(date : LocalDate) extends MinimumDateValidation
  @deprecated
  case class StandardIncorporatedDate(date : LocalDate) extends MinimumDateValidation

  @deprecated
  class StartDateFormFactory @Inject()(dateService: DateService, today: Now[LocalDate]) {

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val RADIO_INPUT_NAME = "startDateRadio"

    def form(incorpDate : Option[LocalDate] = None): Form[StartDateView] = {

      val minDateValidation = incorpDate.fold[MinimumDateValidation](ToIncorpMinimumFutureDate(dateService.addWorkingDays(today(), 2))) {date =>
        val fouryears = today().minusYears(4)
        if (fouryears.isAfter(date)) FourYearsSinceIncorporatedDate(fouryears) else StandardIncorporatedDate(date)
      }
      val maxDate: LocalDate = today().plusMonths(3)
      implicit val specificErrorCode: String = "startDate"
      implicit val incDate = incorpDate

      def inRangeCustom()(implicit ordering: Ordering[LocalDate], e: ErrorCode): Constraint[LocalDate] =
        Constraint[LocalDate] { (t: LocalDate) =>
          val minValue: LocalDate = minDateValidation.date

          Logger.info(s"Checking constraint for value $t in the range of [$minValue, $maxDate]")
          (ordering.compare(t, minValue).signum, ordering.compare(t, maxDate).signum) match {
            case (1, -1) | (0, _) | (_, 0) => Valid
            case (_, 1) => Invalid(ValidationError(s"validation.$e.range.above", maxDate))
            case (-1, _) =>
              Invalid(minDateValidation match {
                case ToIncorpMinimumFutureDate(_) => ValidationError(s"validation.$e.range.below", minValue)
                case FourYearsSinceIncorporatedDate(_) => ValidationError(s"validation.$e.range.below4y", minValue)
                case StandardIncorporatedDate(_) => ValidationError(s"validation.$e.range.belowIncorp", minValue)
              }
            )
          }
        }

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
              nonEmptyDateModel(validDateModel(inRangeCustom())))
          )
        )(StartDateView.bind)(StartDateView.unbind)
      )
    }
  }
}
