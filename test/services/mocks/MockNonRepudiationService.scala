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

package services.mocks

import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.Html
import services.NonRepudiationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockNonRepudiationService extends MockitoSugar {
  self: Suite =>

  val mockNonRepuidiationService = mock[NonRepudiationService]

  def mockStoreEncodedUserAnswers(regId: String)
                                 (response: Future[StoreNrsPayloadSuccess.type]): OngoingStubbing[Future[StoreNrsPayloadSuccess.type]] =
    when(mockNonRepuidiationService.storeEncodedUserAnswers(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.any[Html]
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

}
