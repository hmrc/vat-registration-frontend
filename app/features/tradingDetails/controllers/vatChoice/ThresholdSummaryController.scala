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
  import models.api.{VatScheme, VatThresholdPostIncorp}
  import play.api.libs.json.Json

  import scala.util.Try

  case class OverThresholdView(selection: Boolean, date: Option[LocalDate] = None)

  object OverThresholdView {

    def bind(selection: Boolean, dateModel: Option[MonthYearModel]): OverThresholdView =
      OverThresholdView(selection, dateModel.flatMap(_.toLocalDate))

    def unbind(overThreshold: OverThresholdView): Option[(Boolean, Option[MonthYearModel])] =
      Try {
        overThreshold.date.fold((overThreshold.selection, Option.empty[MonthYearModel])) {
          d => (overThreshold.selection, Some(MonthYearModel.fromLocalDate(d)))
        }
      }.toOption

    implicit val format = Json.format[OverThresholdView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatEligibilityChoice) => group.overThreshold,
      updateF = (c: OverThresholdView, g: Option[S4LVatEligibilityChoice]) =>
        g.getOrElse(S4LVatEligibilityChoice()).copy(overThreshold = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[OverThresholdView] { vs: VatScheme =>
      vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice.map(_.vatThresholdPostIncorp)).collect {
        case Some(VatThresholdPostIncorp(selection, d@_)) => OverThresholdView(selection, d)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.builders._
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import models.api._
  import models.view._
  import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
  import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration}
  import models.{CurrentProfile, MonthYearModel, S4LVatEligibilityChoice}
  import play.api.mvc._
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  @Singleton
  class ThresholdSummaryController @Inject()(ds: CommonPlayDependencies,
                                             val keystoreConnector: KeystoreConnect,
                                             val authConnector: AuthConnector,
                                             implicit val s4LService: S4LService,
                                             implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            val dateOfIncorporation = profile.incorporationDate
              .getOrElse(throw new IllegalStateException("Date of Incorporation data expected to be found in Incorporation"))

            getThresholdSummary map {
              thresholdSummary => Ok(features.tradingDetails.views.html.vatChoice.threshold_summary(
                thresholdSummary,
                MonthYearModel.FORMAT_DD_MMMM_Y.format(dateOfIncorporation))
              )
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request => {
          withCurrentProfile { implicit profile =>
            getVatThresholdPostIncorp.map {
              case VatThresholdPostIncorp(true, _) =>
                save(VoluntaryRegistration(REGISTER_NO))
                save(StartDateView(COMPANY_REGISTRATION_DATE))
                Redirect(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
              case _ => Redirect(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show())
            }
          }
      }
    }

    def getThresholdSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
      for {
        vatThresholdPostIncorp <- getVatThresholdPostIncorp
      } yield thresholdToSummary(vatThresholdPostIncorp)
    }

    def thresholdToSummary(vatThresholdPostIncorp: VatThresholdPostIncorp): Summary = {
      Summary(Seq(
        SummaryVatThresholdBuilder(Some(vatThresholdPostIncorp)).section
      ))
    }

    def getVatThresholdPostIncorp(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatThresholdPostIncorp] = {
      for {
        eligibilityChoice <- s4LService.fetchAndGet[S4LVatEligibilityChoice]
        overThreshold <- eligibilityChoice.flatMap(_.overThreshold).pure
      } yield overThreshold.map(o => VatThresholdPostIncorp(o.selection, o.date)).get
    }
  }
}

package forms.vatTradingDetails.vatChoice {

  import java.time.LocalDate
  import javax.inject.Inject

  import common.Now
  import forms.FormValidation.Dates.{nonEmptyMonthYearModel, validPartialMonthYearModel}
  import forms.FormValidation.{missingBooleanFieldMappingArgs, _}
  import models.MonthYearModel
  import models.MonthYearModel.FORMAT_DD_MMMM_Y
  import models.view.vatTradingDetails.vatChoice.OverThresholdView
  import play.api.data.Form
  import play.api.data.Forms._
  import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

  class OverThresholdFormFactory @Inject()(today: Now[LocalDate]) {

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val RADIO_YES_NO = "overThresholdRadio"

    def form(dateOfIncorporation: LocalDate): Form[OverThresholdView] = {

      val minDate: LocalDate = dateOfIncorporation
      val maxDate: LocalDate = LocalDate.now()
      implicit val specificErrorCode: String = "overThreshold.date"

      Form(
        mapping(
          RADIO_YES_NO -> missingBooleanFieldMappingArgs()(Seq(dateOfIncorporation.format(FORMAT_DD_MMMM_Y)))("overThreshold.selection"),
          "overThreshold" -> mandatoryIf(
            isEqual(RADIO_YES_NO, "true"),
            mapping(
              "month" -> text,
              "year" -> text
            )(MonthYearModel.apply)(MonthYearModel.unapply).verifying(
              nonEmptyMonthYearModel(validPartialMonthYearModel(inRangeWithArgs(minDate, maxDate)(Seq(dateOfIncorporation.format(FORMAT_DD_MMMM_Y))))))
          )
        )(OverThresholdView.bind)(OverThresholdView.unbind)
      )
    }
  }
}
