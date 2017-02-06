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

package testHelpers

import controllers.CommonPlayDependencies
import mocks.VatMocks
import org.scalatest.mockito.MockitoSugar
import services.VatRegistrationService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

trait VatRegSpec extends UnitSpec with WithFakeApplication with MockitoSugar with VatMocks {

  // Placeholder for custom configuration
  // Use this if you want to configure the app
  // implicit override lazy val app: Application = new GuiceApplicationBuilder().configure().build()
  var ds: CommonPlayDependencies = fakeApplication.injector.instanceOf[CommonPlayDependencies]
  var vatRegistrationService = fakeApplication.injector.instanceOf[VatRegistrationService]
}
