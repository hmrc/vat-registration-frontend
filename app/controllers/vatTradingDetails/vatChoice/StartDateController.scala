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

package controllers.vatTradingDetails.vatChoice

import java.time.LocalDate
import javax.inject.Inject

import common.Now
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatChoice.StartDateFormFactory
import models.view.vatTradingDetails.vatChoice.StartDateView
import play.api.data.Form
import play.api.mvc._
import services.{IIService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.pages.vatTradingDetails.vatChoice.start_date

import scala.concurrent.Future

class StartDateController @Inject()(startDateFormFactory: StartDateFormFactory, iis: IIService, ds: CommonPlayDependencies)
                                   (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  val form: Form[StartDateView] = startDateFormFactory.form()

  protected[controllers]
  def populateCtActiveDate(vm: StartDateView)(implicit headerCarrier: HeaderCarrier, today: Now[LocalDate]): Future[StartDateView] =
    iis.getCTActiveDate().filter(today().plusMonths(3).isAfter).fold(vm)(vm.withCtActiveDateOption)


  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[StartDateView].getOrElse(StartDateView())
      .flatMap(populateCtActiveDate).map(f => Ok(start_date(form.fill(f))))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    startDateFormFactory.form().bindFromRequest().fold(
      badForm => BadRequest(start_date(badForm)).pure,
      goodForm => populateCtActiveDate(goodForm).flatMap(vm => s4LService.saveForm(vm)).map { _ =>
        Redirect(controllers.vatTradingDetails.routes.TradingNameController.show())
      }
    )
  })
}
