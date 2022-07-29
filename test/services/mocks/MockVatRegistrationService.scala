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

package services.mocks

import models.{ApiKey, CurrentProfile}
import models.api.{PartyType, VatScheme}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Format, JsValue}
import services.VatRegistrationService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

trait MockVatRegistrationService extends MockitoSugar {
  self: Suite =>

  val vatRegistrationServiceMock: VatRegistrationService = mock[VatRegistrationService]

  def mockGetVatScheme(response: Future[VatScheme]): OngoingStubbing[Future[VatScheme]] =
    when(vatRegistrationServiceMock.getVatScheme(
      any[CurrentProfile],
      any[HeaderCarrier]
    )) thenReturn response

  def mockGetVatSchemeJson(regId: String)(response: Future[JsValue]): OngoingStubbing[Future[JsValue]] =
    when(vatRegistrationServiceMock.getVatSchemeJson(
      ArgumentMatchers.eq(regId)
    )(any[HeaderCarrier])) thenReturn response

  def mockUpsertSection[T](regId: String, data: T)(response: Future[T]): OngoingStubbing[Future[T]] =
    when(vatRegistrationServiceMock.upsertSection(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(data)
    )(
      any[HeaderCarrier],
      any[Format[T]],
      any[ApiKey[T]]
    )) thenReturn response

  def mockPartyType(response: Future[PartyType]): OngoingStubbing[Future[PartyType]] =
    when(vatRegistrationServiceMock.partyType(
      any[CurrentProfile],
      any[HeaderCarrier]
    )) thenReturn response

  def mockIsTransactor(response: Future[Boolean]): OngoingStubbing[Future[Boolean]] =
    when(vatRegistrationServiceMock.isTransactor(
      any[CurrentProfile],
      any[HeaderCarrier]
    )) thenReturn response
}
