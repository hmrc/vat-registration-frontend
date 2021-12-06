/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.test.TestVatRegistrationConnector
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeleteAllRegistrationsController @Inject()(mcc: MessagesControllerComponents,
                                                testVatRegistrationConnector: TestVatRegistrationConnector)
                                                (implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def deleteAllRegistrations: Action[AnyContent] = Action.async { implicit request =>
    testVatRegistrationConnector.deleteAllRegistrations.map { registrationsDeleted =>
      if(registrationsDeleted) {
        Ok(Html("""
          <html>
            <h1>OK</h1>
            <p>All registrations have been deleted successfully</p>
          </html>
        """))
      } else {
        InternalServerError(Html("""
          <html>
            <h1>Something went wrong</h1>
            <p>Unable to remove registrations</p>
          </html>
        """))
      }
    }
  }

}
