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

package helpers

import builders.AuthBuilder
import controllers.CommonPlayDependencies
import fixtures.LoginFixture
import mocks.VatMocks
import org.scalatest.Inside
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.{Action, AnyContent, Result}
import services.VatRegistrationServiceImpl
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class VatRegSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with VatMocks with LoginFixture with Inside {

  // Placeholder for custom configuration
  // Use this if you want to configure the app
  // implicit override lazy val app: Application = new GuiceApplicationBuilder().configure().build()
  var ds: CommonPlayDependencies = app.injector.instanceOf[CommonPlayDependencies]
  var vatRegistrationService = app.injector.instanceOf[VatRegistrationServiceImpl]

  def callAuthorised(a: Action[AnyContent], ac: AuthConnector)(test: Future[Result] => Any): Unit =
    AuthBuilder.withAuthorisedUser(a, ac)(test)

}
