/*
 * Copyright 2022 HM Revenue & Customs
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
import common.enums.VatRegStatus
import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.mocks.AuthMock
import models.CurrentProfile
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HeaderNames, Status}
import play.api.mvc._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait ControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with AuthMock with AuthBuilder with BeforeAndAfterEach
  with Status with FutureAwaits with DefaultAwaitTimeout with ResultExtractors with HeaderNames with VatMocks {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val messagesControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val baseControllerComponents: BaseControllerComponents = new BaseControllerComponents(messagesControllerComponents, mockTrafficManagementService)
  val regId = "VAT123456"

  implicit val currentProfile: CurrentProfile = CurrentProfile(
    registrationId = regId,
    vatRegistrationStatus = VatRegStatus.draft
  )

  def submitAuthorised(a: => Action[AnyContent], r: => FakeRequest[AnyContentAsFormUrlEncoded])
                      (test: Future[Result] => Assertion): Unit =
    submitWithAuthorisedUser(a, r)(test)

  def callAuthorised(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedUser(a)(test)

  def callAuthorisedOrg(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedOrgUser(a)(test)

  def mockWithCurrentProfile(currentProfile: Option[CurrentProfile]): OngoingStubbing[Future[Option[CurrentProfile]]] = {
    when(mockSessionService.fetchAndGet[CurrentProfile](any())(any(), any()))
      .thenReturn(Future.successful(currentProfile))
  }

  override protected def beforeEach(): Unit = resetMocks()
}

