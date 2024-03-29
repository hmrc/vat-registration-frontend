/*
 * Copyright 2024 HM Revenue & Customs
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

import builders.AuthBuilder
import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax
import common.enums.VatRegStatus
import connectors.mocks.AuthMock
import fixtures.{LoginFixture, VatRegistrationFixture}
import models.CurrentProfile
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Assertion, BeforeAndAfterEach, Inside, Inspectors}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.FakeRequest
import services.mocks.BusinessServiceMock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable, Future}

class VatRegSpec extends PlaySpec with GuiceOneAppPerSuite with AuthMock with AuthBuilder
  with MockitoSugar with VatMocks with BusinessServiceMock with LoginFixture with Inside with Inspectors
  with ScalaFutures with ApplicativeSyntax with FutureInstances with BeforeAndAfterEach with FutureAssertions with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()
  implicit val cp = currentProfile

  def currentProfile =
    CurrentProfile(testRegId, VatRegStatus.draft)

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Seconds), interval = Span(50, Millis))

  implicit val duration = 5.seconds

  def await[T](future: Awaitable[T]): T = Await.result(future, duration)

  // Placeholder for custom configuration
  // Use this if you want to configure the app
  // implicit override lazy val app: Application = new GuiceApplicationBuilder().configure().build()

  def submitAuthorised(a: => Action[AnyContent], r: => FakeRequest[AnyContentAsFormUrlEncoded])
                      (test: Future[Result] => Assertion): Unit =
    submitWithAuthorisedUser(a, r)(test)

  def callAuthorised(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedUser(a)(test)

  override protected def beforeEach(): Unit = {
    resetMocks()
  }
}
