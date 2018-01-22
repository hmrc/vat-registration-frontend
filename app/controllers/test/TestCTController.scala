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

package controllers.test

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import connectors.KeystoreConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.Html
import services.{PrePopService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class TestCTController @Inject()(iis: PrePopService,
                                 ds: CommonPlayDependencies,
                                 val authConnector: AuthConnector,
                                 val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def show(): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit req =>
        withCurrentProfile { implicit profile =>
//          iis.getCTActiveDate.map(DateTimeFormatter.ISO_LOCAL_DATE.format).getOrElse("NONE").map(s => Ok(Html(s)))
          iis.getCTActiveDate.map(_.fold("NONE")(x => DateTimeFormatter.ISO_LOCAL_DATE.format(x))).map(x => Ok(Html(x)))
        }
  }
}
