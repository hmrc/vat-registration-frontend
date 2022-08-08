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

package fixtures

import java.time.LocalDate
import models.{FlatRateScheme, Start}
import play.api.libs.json.{JsObject, Json}

trait FlatRateFixtures {

  private val flatRatePercentage = BigDecimal(3.14)
  val frsDate = Some(Start(Some(LocalDate.of(2017, 10, 10))))

  val validBusinessSectorView = ("test business sector", flatRatePercentage)

  val validFlatRate = FlatRateScheme(
    Some(true),
    Some(true),
    Some(5003),
    Some(true),
    Some(true),
    frsDate,
    Some("frsId"),
    Some(flatRatePercentage),
    Some(false)
  )

  val defaultFlatRate: BigDecimal = 16.5

  val frs1KReg = FlatRateScheme(
    Some(true),
    Some(true),
    Some(1000L),
    Some(true),
    Some(true),
    frsDate,
    Some("frsId"),
    Some(defaultFlatRate),
    Some(false)
  )

  val frs1KNreg = FlatRateScheme(
    Some(true),
    Some(true),
    None,
    None,
    Some(false),
    None,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerReg = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(true),
    Some(true),
    frsDate,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerNconf = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(false),
    Some(true),
    frsDate,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerNconfN = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(false),
    Some(false),
    None,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsNoJoin = FlatRateScheme(
    Some(false)
  )

  val frsNoJoinWithDetails = FlatRateScheme(
    Some(false),
    Some(true),
    Some(1000000L),
    Some(true),
    Some(false),
    None,
    Some("frsId"),
    Some(defaultFlatRate),
    Some(false)
  )

  val incompleteS4l = FlatRateScheme(
    Some(true),
    Some(true)
  )

  val businessCategory = "019"

  val testFlatRate = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(30000L),
    overBusinessGoodsPercent = None,
    useThisRate = None,
    frsStart = None,
    categoryOfBusiness = Some(businessCategory),
    percent = None
  )

  val jsonBusinessTypes = Json.parse(
    s"""
       |[
       |  {
       |    "groupLabel": "Test 1",
       |    "categories": [
       |      {"id": "020", "businessType": "Hotel or accommodation", "currentFRSPercent": 10.5},
       |      {"id": "019", "businessType": "Test BusinessType", "currentFRSPercent": 3},
       |      {"id": "038", "businessType": "Pubs", "currentFRSPercent": "5"}
       |    ]
       |  },
       |  {
       |    "groupLabel": "Test 2",
       |    "categories": [
       |      {"id": "039", "businessType": "Cafes", "currentFRSPercent": "5"}
       |    ]
       |  }
       |]
        """.stripMargin).as[Seq[JsObject]]

  val testsector = ("id", "test", BigDecimal(10))
}
