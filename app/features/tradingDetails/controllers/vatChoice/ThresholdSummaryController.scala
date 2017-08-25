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
      readF = (group: S4LTradingDetails) => group.overThreshold,
      updateF = (c: OverThresholdView, g: Option[S4LTradingDetails]) =>
        g.getOrElse(S4LTradingDetails()).copy(overThreshold = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[OverThresholdView] { vs: VatScheme =>
      vs.tradingDetails.map(_.vatChoice.vatThresholdPostIncorp).collect {
        case Some(VatThresholdPostIncorp(selection, d@_)) => OverThresholdView(selection, d)
      }
    }
  }
}

package controllers.vatTradingDetails.vatChoice {

  import javax.inject.Inject

  import controllers.builders._
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import models.api._
  import models.view._
  import models.view.vatTradingDetails.vatChoice.StartDateView.COMPANY_REGISTRATION_DATE
  import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration.REGISTER_NO
  import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration}
  import models.{MonthYearModel, S4LTradingDetails}
  import play.api.mvc._
  import services.{CommonService, S4LService, VatRegistrationService}
  import uk.gov.hmrc.play.http.HeaderCarrier

  import scala.concurrent.Future

  class ThresholdSummaryController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4LService: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with CommonService {

    def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      for {
        thresholdSummary <- getThresholdSummary()
        dateOfIncorporation <- fetchDateOfIncorporation()
      } yield Ok(features.tradingDetails.views.html.vatChoice.threshold_summary(
        thresholdSummary,
        MonthYearModel.FORMAT_DD_MMMM_Y.format(dateOfIncorporation))))

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
      getVatThresholdPostIncorp().map(vatThresholdPostIncorp => vatThresholdPostIncorp match {
        case VatThresholdPostIncorp(true, _) =>
          save(VoluntaryRegistration(REGISTER_NO))
          save(StartDateView(COMPANY_REGISTRATION_DATE))
          Redirect(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
        case _ => Redirect(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show())
      })
    })

    def getThresholdSummary()(implicit hc: HeaderCarrier): Future[Summary] = {
      for {
        vatThresholdPostIncorp <- getVatThresholdPostIncorp()
      } yield thresholdToSummary(vatThresholdPostIncorp)
    }

    def thresholdToSummary(vatThresholdPostIncorp: VatThresholdPostIncorp): Summary = {
      Summary(Seq(
        SummaryVatThresholdBuilder(Some(vatThresholdPostIncorp)).section
      ))
    }

    def getVatThresholdPostIncorp()(implicit hc: HeaderCarrier): Future[VatThresholdPostIncorp] = {
      for {
        vatTradingDetails <- s4LService.fetchAndGet[S4LTradingDetails]()
        overThreshold <- vatTradingDetails.flatMap(_.overThreshold).pure
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
