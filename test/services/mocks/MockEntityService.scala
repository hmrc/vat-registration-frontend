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

import models.Entity
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import services.EntityService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockEntityService extends MockitoSugar {
  self: Suite =>

  val mockEntityService: EntityService = mock[EntityService]

  def mockGetEntity(regId: String, idx: Int)(response: Option[Entity]): OngoingStubbing[Future[Option[Entity]]] = {
    when(mockEntityService.getEntity(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(idx)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)
  }

  def mockUpsertEntity[T](regId: String, index: Int, data: T)(response: Entity): OngoingStubbing[Future[Entity]] =
    when(mockEntityService.upsertEntity(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(index),
      ArgumentMatchers.eq(data)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

}
