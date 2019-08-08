/*
 * Copyright 2019 HM Revenue & Customs
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
import common.enums.VatRegStatus
import mocks.{AuthMock, VatMocks}
import models.CurrentProfile
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.{HeaderNames, Status}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait ControllerSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with AuthMock with AuthBuilder with BeforeAndAfterEach
  with Status with FutureAwaits with DefaultAwaitTimeout with ResultExtractors with HeaderNames with VatMocks {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val regId = "VAT123456"

  implicit val currentProfile: CurrentProfile = CurrentProfile(
    companyName = "Test Company",
    registrationId = regId,
    transactionId = "000-434-1",
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate = Some(LocalDate.of(2017, 12, 21)),
    ivPassed = Some(true)
  )

  def submitAuthorised(a: => Action[AnyContent], r: => FakeRequest[AnyContentAsFormUrlEncoded])
                      (test: Future[Result] => Assertion): Unit =
    submitWithAuthorisedUser(a, r)(test)

  def callAuthorised(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedUser(a)(test)

  def callAuthorisedOrg(a: Action[AnyContent])(test: Future[Result] => Assertion): Unit =
    withAuthorisedOrgUser(a)(test)

  def mockWithCurrentProfile(currentProfile: Option[CurrentProfile]): OngoingStubbing[Future[Option[CurrentProfile]]] = {
    when(mockKeystoreConnector.fetchAndGet[CurrentProfile](any())(any(), any()))
      .thenReturn(Future.successful(currentProfile))
  }

  override protected def beforeEach(): Unit = resetMocks()
}

trait MockMessages {

  val mockMessagesAPI: MessagesApi

  val lang = Lang("en")
  val messages = Messages(lang, mockMessagesAPI)

  val MOCKED_MESSAGE = "mocked message"

  def mockAllMessages: OngoingStubbing[String] = {
    when(mockMessagesAPI.preferred(any[RequestHeader]()))
      .thenReturn(messages)

    when(mockMessagesAPI.apply(any[String](), any())(any()))
      .thenReturn(MOCKED_MESSAGE)
  }
}
