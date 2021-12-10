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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import services.SessionService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait SessionServiceMock {
  this: MockitoSugar =>

  lazy val mockSessionService = mock[SessionService]

  import cats.syntax.applicative._

  def mockSessionFetchAndGet[T](key: String, model: Option[T]): OngoingStubbing[Future[Option[T]]] =
    when(mockSessionService.fetchAndGet[T](ArgumentMatchers.contains(key))(any(), any())).thenReturn(Future.successful(model.pure))

  def mockSessionCache[T](key: String, data: T): OngoingStubbing[Future[CacheMap]] =
    when(mockSessionService.cache(ArgumentMatchers.contains(key), ArgumentMatchers.eq[T](data))(any(), any[Format[T]]())).thenReturn(Future.successful(CacheMap("", Map())))

  def mockSessionCacheError[T](key: String, err: Exception): OngoingStubbing[Future[CacheMap]] =
    when(mockSessionService.cache(ArgumentMatchers.contains(key), any[T]())(any(), any())).thenReturn(Future.failed(err))

  def mockSessionClear(): OngoingStubbing[Future[Boolean]] = when(mockSessionService.remove(any())) thenReturn Future.successful(true)

  def mockFetchRegId(regID: String = "12345"): OngoingStubbing[Future[Option[String]]] =
    when(mockSessionService.fetchAndGet[String](any())(any(), any())).thenReturn(Future.successful(Some(regID).pure))

}
