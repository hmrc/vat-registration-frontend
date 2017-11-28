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

package controllers.test

import javax.inject.{Inject, Singleton}

import connectors.test.TestRegistrationConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class TestVatRegistrationAdminController @Inject()(vatRegConnector: TestRegistrationConnector,
                                                   val authConnector: AuthConnector,
                                                   ds: CommonPlayDependencies) extends VatRegistrationController(ds) {
  def dropCollection(): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        vatRegConnector.dropCollection.map(_ => Ok("DB cleared"))
  }
}
