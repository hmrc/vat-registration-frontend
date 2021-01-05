/*
 * Copyright 2021 HM Revenue & Customs
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

package mocks

import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.mockito.{ArgumentMatchers => Matchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future

trait HttpClientMock {
  this: MockitoSugar =>

  lazy val mockHttpClient = mock[HttpClient]

  def mockHttpGET[T](url: String, thenReturn: T): OngoingStubbing[Future[T]] = {
    when(mockHttpClient.GET[T](Matchers.anyString())(Matchers.any[HttpReads[T]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockHttpGET[T](url: String, thenReturn: Future[T]): OngoingStubbing[Future[T]] = {
    when(mockHttpClient.GET[T](Matchers.anyString())(Matchers.any[HttpReads[T]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(thenReturn)
  }

  def mockHttpDELETE[O](url: String, thenReturn: O): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.DELETE[O](Matchers.anyString(), Matchers.any())(Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockHttpPOST[I, O](url: String, thenReturn: O): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.POST[I, O](Matchers.any(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]],
      Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier](), Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockHttpPOSTEmpty[O](url: String, thenReturn: O): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.POSTEmpty[O](Matchers.anyString(), Matchers.any())
      (Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockHttpPUT[I, O](url: String, thenReturn: O): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.PUT(Matchers.anyString(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]],
      Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockHttpPATCH[I, O](url: String, thenReturn: O): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.PATCH(Matchers.anyString(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]],
      Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }


  def mockHttpFailedGET[T](url: String, exception: Exception): OngoingStubbing[Future[T]] = {
    when(mockHttpClient.GET[T](Matchers.anyString())(Matchers.any[HttpReads[T]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockHttpFailedPOST[I, O](url: String, exception: Exception): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.POST[I, O](Matchers.anyString(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]],
      Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

   def mockHttpFailedPOSTEmpty[O](url: String, exception: Exception): OngoingStubbing[Future[O]] = {
     when(mockHttpClient.POSTEmpty[O](Matchers.anyString(), Matchers.any())
       (Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockHttpFailedPATCH[I, O](url: String, exception: Exception): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.PATCH[I, O](Matchers.anyString(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]],
      Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockHttpFailedDELETE[O](url: String, exception: Exception): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.DELETE[O](Matchers.anyString(), Matchers.any())(Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockHttpFailedPUT[I, O](url: String, exception: Exception): OngoingStubbing[Future[O]] = {
    when(mockHttpClient.PUT[I, O](
        Matchers.anyString(), Matchers.any[I], Matchers.any())(Matchers.any[Writes[I]], Matchers.any[HttpReads[O]], Matchers.any[HeaderCarrier], Matchers.any()
      )).thenReturn(Future.failed(exception))
  }
}
