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

import connectors.ConfigConnect
import controllers.CommonPlayDependencies
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

class ConfirmBusinessSectorController @Inject()(ds: CommonPlayDependencies, configConnector: ConfigConnect)
                                               (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends BusinessSectorAwareController(ds, configConnector) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    businessSectorView().map(view => Ok(features.frs.views.html.frs_confirm_business_sector(view))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    businessSectorView().flatMap(save(_).map(_ => Redirect(controllers.frs.routes.RegisterForFrsWithSectorController.show()))))

}
