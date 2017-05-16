/*
 * Copyright 2017 HM Revenue & Customs
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

import connectors._
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeystoreMock {
  this: MockitoSugar =>

  lazy val mockKeystoreConnector = mock[KeystoreConnector]

  import cats.instances.future._
  import cats.syntax.applicative._

  def mockKeystoreFetchAndGet[T](key: String, model: Option[T]): OngoingStubbing[Future[Option[T]]] =
    when(mockKeystoreConnector.fetchAndGet[T](Matchers.contains(key))(any(), any())).thenReturn(model.pure)

  def mockKeystoreCache[T](key: String, cacheMap: CacheMap): OngoingStubbing[Future[CacheMap]] =
    when(mockKeystoreConnector.cache(Matchers.contains(key), any[T]())(any(), any[Format[T]]())).thenReturn(cacheMap.pure)

  def mockKeystoreCacheError[T](key: String, err: Exception): OngoingStubbing[Future[CacheMap]] =
    when(mockKeystoreConnector.cache(Matchers.contains(key), any[T]())(any(), any())).thenReturn(Future.failed(err))

  def mockKeystoreClear(): OngoingStubbing[Future[HttpResponse]] =
    when(mockKeystoreConnector.remove()(any())).thenReturn(HttpResponse(200).pure)

  def mockFetchRegId(regID: String = "12345"): OngoingStubbing[Future[Option[String]]] =
    when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any())).thenReturn(Some(regID).pure)

}
