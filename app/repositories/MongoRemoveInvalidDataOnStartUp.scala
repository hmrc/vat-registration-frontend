/*
 * Copyright 2025 HM Revenue & Customs
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
import featuretoggle.FeatureSwitch._
import featuretoggle.FeatureToggleSupport._
import org.apache.pekko.actor.ActorSystem
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Try

@Singleton
class MongoRemoveInvalidDataOnStartUp @Inject() (actorSystem: ActorSystem, sessionRepository: SessionRepository)(implicit
    appConfig: FrontendAppConfig,
    ec: ExecutionContext)
    extends LoggingUtil {

  protected def jitterDelay: FiniteDuration = (10 + scala.util.Random.nextInt(5)).seconds

  actorSystem.scheduler.scheduleOnce(jitterDelay) {
    logger.warn(s"[MongoRemoveInvalidDataOnStartUp] Start up job has started after delay of $jitterDelay.")
    deleteInvalidData()
    logger.warn(s"[MongoRemoveInvalidDataOnStartUp] Start up job has ended.")
  }

  def deleteInvalidData(): Unit = {
    val deleteAllDocuments: Boolean      = isEnabled(DeleteAllInvalidTimestampData)
    val deleteNDocuments: Boolean        = isEnabled(DeleteSomeInvalidTimestampData)
    val deleteDocumentLimit: Option[Int] = Try(getValue(LimitForDeleteSomeData).toInt).toOption

    (deleteNDocuments, deleteDocumentLimit, deleteAllDocuments) match {
      case (true, Some(limit), false) if limit > 0 =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp] 'DeleteSomeInvalidTimestampData' switch is set to true - starting deleteNDataWithLastUpdatedString process.")
        sessionRepository.deleteNDataWithLastUpdatedStringType(limit)
      case (false, _, true) =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp] 'DeleteAllInvalidTimestampData' switch is set to true - starting deleteAllDataWithLastUpdatedString process.")
        sessionRepository.deleteAllDataWithLastUpdatedStringType()
      case (true, None, false) =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp] 'DeleteSomeInvalidTimestampData' switch is on but limit config is invalid" +
            s" - no action taken.")
      case (false, _, false) =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp] 'DeleteAllInvalidTimestampData' and 'DeleteSomeInvalidTimestampData'" +
            s" switches are set to false - no action taken.")
      case _ =>
        logger.warn(
          s"[MongoRemoveInvalidDataOnStartUp] Conflicting 'DeleteInvalidTimestampData' / 'DeleteSomeInvalidTimestampData'" +
            s" switches or invalid deletion limit, - no action taken.")
    }

  }

}
