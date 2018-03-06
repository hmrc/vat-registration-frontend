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

package helpers

import java.time.LocalDate

import builders.AuthBuilder
import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax
import common.enums.VatRegStatus
import fixtures.{LoginFixture, VatRegistrationFixture}
import mocks.{AuthMock, VatMocks}
import models.CurrentProfile
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Assertion, BeforeAndAfterEach, Inside, Inspectors}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import utils.{BooleanFeatureSwitch, FeatureManager, VATRegFeatureSwitch}

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable, Future}

class VatRegSpec extends PlaySpec with OneAppPerSuite with AuthMock with AuthBuilder
  with MockitoSugar with VatMocks with LoginFixture with Inside with Inspectors
  with ScalaFutures with ApplicativeSyntax with FutureInstances with BeforeAndAfterEach with FutureAssertions with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()
  implicit val cp = currentProfile()

  def currentProfile(ivPassed: Option[Boolean] = Some(true)): CurrentProfile =
    CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft,Some(LocalDate.of(2017, 12, 21)),ivPassed)

  val currentNonincorpProfile: CurrentProfile = currentProfile().copy(incorporationDate = None)

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Seconds), interval = Span(50, Millis))

  implicit val duration = 5.seconds

  def await[T](future : Awaitable[T]) : T = Await.result(future, duration)

  // Placeholder for custom configuration
  // Use this if you want to configure the app
  // implicit override lazy val app: Application = new GuiceApplicationBuilder().configure().build()

  val mockVATFeatureSwitch = mock[VATRegFeatureSwitch]
  val mockFeatureManager = mock[FeatureManager]

  val disabledFeatureSwitch = BooleanFeatureSwitch("test",false)
  val enabledFeatureSwitch = BooleanFeatureSwitch("test",true)

  def submitAuthorised(a: => Action[AnyContent], r: => FakeRequest[AnyContentAsFormUrlEncoded])
                      (test: Future[Result] => Assertion): Unit =
    submitWithAuthorisedUser(a, r)(test)

  def callAuthorised(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedUser(a)(test)

  override protected def beforeEach(): Unit = {
    resetMocks()
  }
}
