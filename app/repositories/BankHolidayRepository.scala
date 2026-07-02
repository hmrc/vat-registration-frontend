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
import services.BankHolidaysService.bankHolidaySetFormat
import uk.gov.hmrc.mongo.cache.{CacheIdType, CacheItem, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import utils.workingdays.BankHolidaySet

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankHolidayRepository @Inject() (
                                        mongoComponent: MongoComponent,
                                        configuration: FrontendAppConfig,
                                        timestampSupport: TimestampSupport
                                    )(implicit ec: ExecutionContext)
  extends MongoCacheRepository[String](
    mongoComponent = mongoComponent,
    collectionName = configuration.appNameAsBankHolidayCacheDbCollection,
    ttl = configuration.mongoDbBankHolidayCacheExpireAfterMinutes,
    timestampSupport = timestampSupport,
    cacheIdType = CacheIdType.SimpleCacheId
  ) {

  private val CacheId = "all_users"

  def saveBankHolidaysDataOnCache(data: BankHolidaySet): Future[CacheItem] =
    put[BankHolidaySet](CacheId)(DataKey("bank_holidays"), data)(bankHolidaySetFormat)

  def getBankHolidaysFromCache: Future[Option[BankHolidaySet]] =
    get[BankHolidaySet](CacheId)(DataKey("bank_holidays"))(bankHolidaySetFormat)
}
