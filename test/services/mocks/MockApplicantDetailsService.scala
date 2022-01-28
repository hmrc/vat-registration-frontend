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

import models.{ApplicantDetails, CurrentProfile}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import services.ApplicantDetailsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockApplicantDetailsService extends MockitoSugar {
  self: Suite =>

  val mockApplicantDetailsService = mock[ApplicantDetailsService]

  def mockGetApplicantDetails(profile: CurrentProfile)(response: ApplicantDetails): OngoingStubbing[Future[ApplicantDetails]] =
    when(mockApplicantDetailsService.getApplicantDetails(
      ArgumentMatchers.eq(profile),
      ArgumentMatchers.any[HeaderCarrier])
    ) thenReturn Future.successful(response)

  def mockSaveApplicantDetails[T](data: T)(response: ApplicantDetails) =
    when(mockApplicantDetailsService.saveApplicantDetails(
      ArgumentMatchers.eq(data)
    )(
      ArgumentMatchers.any[CurrentProfile],
      ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

  def mockGetTransactorApplicantName(profile: CurrentProfile)(response: Option[String]): OngoingStubbing[Future[Option[String]]] =
    when(mockApplicantDetailsService.getTransactorApplicantName(
      ArgumentMatchers.eq(profile),
      ArgumentMatchers.any[HeaderCarrier])
    ) thenReturn Future.successful(response)

}
