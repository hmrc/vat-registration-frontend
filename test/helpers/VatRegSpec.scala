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

import javax.inject.Inject

import controllers.CommonPlayDependencies
import fixtures.LoginFixture
import mocks.VatRegMocks
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}

class VatRegSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with VatRegMocks with LoginFixture {
  // Placeholder for custom configuration
  // Use this if you want to configure the app
  // implicit override lazy val app: Application = new GuiceApplicationBuilder().configure().build()

  @Inject
  var ds: CommonPlayDependencies = app.injector.instanceOf[CommonPlayDependencies]

}
