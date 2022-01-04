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

package connectors.mocks

import connectors.RegistrationApiConnector
import models.ApiKey
import org.mockito.ArgumentMatchers.{any, eq => matches}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockRegistrationApiConnector {
  self: MockitoSugar =>

  val mockRegistrationApiConnector: RegistrationApiConnector = mock[RegistrationApiConnector]

  def mockReplaceSection[T](regId: String, section: T): OngoingStubbing[Future[T]] =
    when(mockRegistrationApiConnector.replaceSection(
      regId = matches(regId),
      section = matches(section)
    )(
      any[ApiKey[T]],
      any[HeaderCarrier],
      any[Format[T]]
    )).thenReturn(Future.successful(section))

  def mockGetSection[T](regId: String, section: Option[T]): OngoingStubbing[Future[Option[T]]] =
    when(mockRegistrationApiConnector.getSection[T](
      regId = matches(regId)
    )(
      any[ApiKey[T]],
      any[HeaderCarrier],
      any[Format[T]]
    )).thenReturn(Future.successful(section))

}
