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
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.CompletionCapacityForm
import models.ModelKeys._
import models.api.{CompletionCapacity, Officer}
import models.view.vatLodgingOfficer.CompletionCapacityView
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, PrePopulationService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

class CompletionCapacityController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService,
                                             prePopService: PrePopulationService)
  extends VatRegistrationController(ds) with CommonService {

  import cats.syntax.flatMap._

  private val form = CompletionCapacityForm.form

  private def fetchOfficerList()(implicit hc: HeaderCarrier) =
    OptionT(keystoreConnector.fetchAndGet[Seq[Officer]](OFFICER_LIST_KEY))

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      officerList <- prePopService.getOfficerList()
      _ <- keystoreConnector.cache(OFFICER_LIST_KEY, officerList)
      res <- viewModel[CompletionCapacityView]().fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.completion_capacity(res, officerList)))

  def submit: Action[AnyContent] = authorised.async(implicit user =>
    implicit request =>
      form.bindFromRequest().fold(
        badForm => fetchOfficerList().getOrElse(Seq()).map(
          officerList => BadRequest(views.html.pages.vatLodgingOfficer.completion_capacity(badForm, officerList))),
        view => (view.id == "other").pure.ifM(
          ifTrue = Ok(views.html.pages.vatEligibility.ineligible("completionCapacity")).pure,
          ifFalse = for {
            officerSeq <- fetchOfficerList().getOrElse(Seq())
            officer = officerSeq.find(_.name.id == view.id).getOrElse(Officer.empty)
            _ <- save(CompletionCapacityView(view.id, Some(CompletionCapacity(officer.name, officer.role))))
            _ <- keystoreConnector.cache(REGISTERING_OFFICER_KEY, officer)
          } yield Redirect(controllers.vatLodgingOfficer.routes.FormerNameController.show()))))

}
