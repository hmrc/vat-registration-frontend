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

package controllers.vatLodgingOfficer

import javax.inject.Inject

import cats.data.OptionT
import connectors.AddressLookupConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.CompleteCapacityForm
import models.external.Officer
import models.view.vatLodgingOfficer.CompleteCapacityView
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, PrePopulationService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

class CompletionCapacityController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService,
                                             prePopService: PrePopulationService,
                                             val alfConnector: AddressLookupConnect)
  extends VatRegistrationController(ds) with CommonService {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  private val form = CompleteCapacityForm.form
  private val officerListKey = "OfficerList"

  private def fetchOfficerList()(implicit headerCarrier: HeaderCarrier) =
     OptionT(keystoreConnector.fetchAndGet[Seq[Officer]](officerListKey))

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      officerList <- prePopService.getOfficerList()
      _ <- keystoreConnector.cache[Seq[Officer]](officerListKey, officerList)
      res <- viewModel[CompleteCapacityView].fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.completion_capacity(res, officerList))
  )

  def submit: Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      form.bindFromRequest().fold(
        badForm => fetchOfficerList().getOrElse(Seq()).map(
          officerList => BadRequest(views.html.pages.vatLodgingOfficer.completion_capacity(badForm, officerList))),
        (form: CompleteCapacityView) =>
          (form.id == "other").pure.ifM(
            Ok(views.html.pages.vatEligibility.ineligible("completionCapacity")).pure
            ,
            for {
              officerSeq <- fetchOfficerList().getOrElse(Seq())
              officer = officerSeq.find(_.name.id == form.id)
              _ <- s4l.saveForm(CompleteCapacityView(form.id, officer))
            } yield Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          )
      )
  }

}
