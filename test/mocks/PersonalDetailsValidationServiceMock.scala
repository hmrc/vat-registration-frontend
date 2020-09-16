/*
 * Copyright 2020 HM Revenue & Customs
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

import models.TransactorDetails
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import services.PersonalDetailsValidationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait PersonalDetailsValidationServiceMock {
  this: MockitoSugar =>

  val mockPersonalDetailsValidationService: PersonalDetailsValidationService = mock[PersonalDetailsValidationService]

  def mockRetrieveValidationResult(validationId: String)(response: Future[TransactorDetails]): Unit =
    when(mockPersonalDetailsValidationService.retrieveValidationResult(ArgumentMatchers.eq(validationId))(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)
}
