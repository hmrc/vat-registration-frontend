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

package fixtures

import models.{FlatRateScheme, FrsBusinessType, FrsGroup}

import java.time.LocalDate

trait FlatRateFixtures {

  val flatRatePercentage: BigDecimal = BigDecimal(3.14)
  val frsDate: LocalDate = LocalDate.of(2017, 10, 10)

  val testBusinessTypeLabel = "test"
  val testBusinessTypeLabelCy = "test"
  val testBusinessCategory = "id"
  val testBusinessTypeDetails: FrsBusinessType = FrsBusinessType("id", testBusinessTypeLabel, testBusinessTypeLabelCy, BigDecimal(10))

  val validFlatRate: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(true),
    Some(5003),
    Some(true),
    Some(true),
    Some(frsDate),
    Some(testBusinessCategory),
    Some(flatRatePercentage),
    Some(false)
  )

  val defaultFlatRate: BigDecimal = 16.5

  val frs1KReg: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(true),
    Some(1000L),
    Some(true),
    Some(true),
    Some(frsDate),
    Some(testBusinessCategory),
    Some(defaultFlatRate),
    Some(false)
  )

  val frs1KNreg: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(true),
    None,
    None,
    Some(false),
    None,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerReg: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(true),
    Some(true),
    Some(frsDate),
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerNconf: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(false),
    Some(true),
    Some(frsDate),
    Some(""),
    Some(defaultFlatRate)
  )

  val frsPerNconfN: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(false),
    Some(1000000L),
    Some(false),
    Some(false),
    None,
    Some(""),
    Some(defaultFlatRate)
  )

  val frsNoJoin: FlatRateScheme = FlatRateScheme(
    Some(false)
  )

  val frsNoJoinWithDetails: FlatRateScheme = FlatRateScheme(
    Some(false),
    Some(true),
    Some(1000000L),
    Some(true),
    Some(false),
    None,
    Some(testBusinessCategory),
    Some(defaultFlatRate),
    Some(false)
  )

  val incompleteFlatRate: FlatRateScheme = FlatRateScheme(
    Some(true),
    Some(true)
  )

  val businessCategory: String = "019"

  val testFlatRate: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(30000L),
    overBusinessGoodsPercent = None,
    useThisRate = None,
    frsStart = None,
    categoryOfBusiness = Some(businessCategory),
    percent = None
  )

  val businessTypes: Seq[FrsGroup] = Seq(
    FrsGroup(
      label = "Test 1",
      labelCy = "Test 1",
      categories = List(
        FrsBusinessType(id = "020", label = "Hotel or accommodation", labelCy = "Hotel or accommodation", percentage = 10.5),
        FrsBusinessType(id = "019", label = "Test BusinessType", labelCy = "Hotel or accommodation", percentage = 3),
        FrsBusinessType(id = "038", label = "Pubs", labelCy = "Hotel or accommodation", percentage = 5)
      )
    ),
    FrsGroup(
      label = "Test 2",
      labelCy = "Test 2",
      categories = List(
        FrsBusinessType(id = "039", label = "Cafes", labelCy = "Cafes", percentage = 5)
      )
    )
  )

}
