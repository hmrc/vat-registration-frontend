/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{CurrentProfile, TransactorDetails}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import services.TransactorDetailsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockTransactorDetailsService extends MockitoSugar {
  self: Suite =>

  val mockTransactorDetailsService = mock[TransactorDetailsService]

  def mockGetTransactorDetails(profile: CurrentProfile)(response: TransactorDetails): OngoingStubbing[Future[TransactorDetails]] =
    when(mockTransactorDetailsService.getTransactorDetails(
      ArgumentMatchers.eq(profile),
      ArgumentMatchers.any[HeaderCarrier], any[Request[_]])
    ) thenReturn Future.successful(response)

  def mockSaveTransactorDetails[T](data: T)(response: TransactorDetails): OngoingStubbing[Future[TransactorDetails]] =
    when(mockTransactorDetailsService.saveTransactorDetails(
      ArgumentMatchers.eq(data)
    )(
      ArgumentMatchers.any[CurrentProfile],
      ArgumentMatchers.any[HeaderCarrier], any[Request[_]]
    )) thenReturn Future.successful(response)

}
