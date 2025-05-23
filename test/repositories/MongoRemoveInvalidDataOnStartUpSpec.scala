/*
 * Copyright 2024 HM Revenue & Customs
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

import com.mongodb.client.result.DeleteResult
import com.typesafe.config.ConfigFactory
import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.{DeleteAllInvalidTimestampData, DeleteSomeInvalidTimestampData, LimitForDeleteSomeData}
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoRemoveInvalidDataOnStartUpSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private val mockSessionRepository: SessionRepository  = mock[SessionRepository]
  private val mockServicesConfig: ServicesConfig        = mock[ServicesConfig]
  private val testActorSystem: ActorSystem              = ActorSystem("testActorSystem", ConfigFactory.load())

  class TestStartUpJob extends MongoRemoveInvalidDataOnStartUp(testActorSystem, mockSessionRepository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockServicesConfig)
    when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)
  }

  "deleteInvalidData" should {
    "not start deletion process" when {
      "'DeleteAllInvalidTimestampData' and 'DeleteSomeInvalidTimestampData' switches are disabled" in {
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("false")
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("false")

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
      }
      "both 'DeleteAllInvalidTimestampData' and 'DeleteSomeInvalidTimestampData' switches are enabled, causing conflict" in {
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("true")

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
      }
      "'DeleteSomeInvalidTimestampData' switch is enabled but there is no configured deletion limit" in {
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("false")

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
      }
      "'DeleteSomeInvalidTimestampData' switch is enabled but the configured deletion limit is not a valid Int" in {
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("false")
        when(mockServicesConfig.getString(LimitForDeleteSomeData.name)).thenReturn("invalidInt")

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
      }
      "'DeleteSomeInvalidTimestampData' switch is enabled but the configured deletion limit is not a positive Int" in {
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("false")
        when(mockServicesConfig.getString(LimitForDeleteSomeData.name)).thenReturn("0")

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
      }
    }

    "make a call to the database to delete the invalid data up to the config limit" when {
      "'DeleteSomeInvalidTimestampData' (and not 'DeleteAllInvalidTimestampData') switch is enabled with a configured deletion limit" in {
        when(mockServicesConfig.getString(LimitForDeleteSomeData.name)).thenReturn("2")
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("false")
        when(mockSessionRepository.deleteNDataWithLastUpdatedStringType(2)).thenReturn(Future.successful(DeleteResult.acknowledged(2)))

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, times(1)).deleteNDataWithLastUpdatedStringType(2)
        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
      }
    }

    "make a call to the database to delete all the invalid data" when {
      "'DeleteAllInvalidTimestampData' switch is enabled (and 'DeleteSomeInvalidTimestampData' is disabled)" in {
        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.name)).thenReturn("true")
        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.name)).thenReturn("false")
        when(mockServicesConfig.getString(LimitForDeleteSomeData.name)).thenReturn("")

        when(mockSessionRepository.deleteAllDataWithLastUpdatedStringType()).thenReturn(Future.successful(DeleteResult.acknowledged(5)))

        new TestStartUpJob().deleteInvalidData()

        verify(mockSessionRepository, times(1)).deleteAllDataWithLastUpdatedStringType()
        verify(mockSessionRepository, never).deleteNDataWithLastUpdatedStringType(anyInt())
      }
    }
  }
}
