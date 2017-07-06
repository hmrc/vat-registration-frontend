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

package controllers.frs

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import connectors.ConfigConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.view.frs.BusinessSectorView
import models.view.sicAndCompliance.MainBusinessActivityView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ConfirmBusinessSectorController @Inject()(ds: CommonPlayDependencies, configConnect: ConfigConnect)
                                               (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[BusinessSectorView]().getOrElseF(determineBusinessSector())
      .map(view => Ok(views.html.pages.frs.frs_confirm_business_sector(view))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => Ok("cool").pure)

  private def determineBusinessSector()(implicit hc: HeaderCarrier): Future[BusinessSectorView] =
    viewModel[MainBusinessActivityView]()
      .subflatMap(mbaView => mbaView.mainBusinessActivity)
      .map(sicCode => configConnect.getBusinessSectorDetails(sicCode.id))
      .getOrElse(throw new IllegalStateException("Can't determine main business activity"))

}
