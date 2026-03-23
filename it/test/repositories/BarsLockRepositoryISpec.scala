/*
 * Copyright 2026 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import models.Lock
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class BarsLockRepositoryISpec extends AnyWordSpec with Matchers with DefaultPlayMongoRepositorySupport[Lock] {

  private val testRegistrationId    = "test-registration-id"
  private val anotherRegistrationId = "another-registration-id"

  private val mockAppConfig: FrontendAppConfig   = mock[FrontendAppConfig]
  private val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)

  override protected val repository: BarsLockRepository =
    new BarsLockRepository(mongoComponent, mockAppConfig)

  "getAttemptsUsed" should {

    "return 0 when no record exists for the identifier" in {
      await(repository.getAttemptsUsed(testRegistrationId)) mustBe 0
    }

    "return the number of failed attempts recorded against the identifier" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.getAttemptsUsed(testRegistrationId)) mustBe 2
    }

    "return only the attempts for the given identifier, not those of others" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(anotherRegistrationId))
      await(repository.recordFailedAttempt(anotherRegistrationId))

      await(repository.getAttemptsUsed(testRegistrationId)) mustBe 1
      await(repository.getAttemptsUsed(anotherRegistrationId)) mustBe 2
    }
  }

  "isLocked" should {

    "return false when no record exists for the identifier" in {
      await(repository.isLocked(testRegistrationId)) mustBe false
    }

    "return false when the number of attempts is below the limit" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.isLocked(testRegistrationId)) mustBe false
    }

    "return true when the number of attempts has reached the limit" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.isLocked(testRegistrationId)) mustBe true
    }

    "return true when the number of attempts exceeds the limit" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.isLocked(testRegistrationId)) mustBe true
    }

    "not be affected by attempts recorded against a different identifier" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.isLocked(anotherRegistrationId)) mustBe false
    }
  }

  "recordFailedAttempt" should {

    "insert a new record and return 1 on the first attempt" in {
      val result = await(repository.recordFailedAttempt(testRegistrationId))

      result mustBe 1
    }

    "increment the failed attempts count on each subsequent call and return the updated count" in {
      await(repository.recordFailedAttempt(testRegistrationId)) mustBe 1
      await(repository.recordFailedAttempt(testRegistrationId)) mustBe 2
      await(repository.recordFailedAttempt(testRegistrationId)) mustBe 3
    }

    "persist the incremented count so it is visible via getAttemptsUsed" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.getAttemptsUsed(testRegistrationId)) mustBe 3
    }

    "not affect records stored against a different identifier" in {
      await(repository.recordFailedAttempt(testRegistrationId))
      await(repository.recordFailedAttempt(testRegistrationId))

      await(repository.recordFailedAttempt(anotherRegistrationId)) mustBe 1

      await(repository.getAttemptsUsed(testRegistrationId)) mustBe 2
      await(repository.getAttemptsUsed(anotherRegistrationId)) mustBe 1
    }
  }
}
