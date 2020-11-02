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

package models

import java.time.LocalDate

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._

import scala.language.implicitConversions

object Frequency extends Enumeration {
  type Frequency = Value
  val monthly   = Value
  val quarterly = Value

  implicit def toString(f : Frequency.Value) : String = f.toString
  implicit val format: Format[Frequency] = Format(Reads.enumNameReads(Frequency), Writes.enumNameWrites)

  implicit def formatter : Formatter[Frequency] = new Formatter[Frequency] {
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case "monthly"              => Right(Frequency.monthly)
        case "quarterly"            => Right(Frequency.quarterly)
        case _                      => Left(Seq(FormError(key, "error.required", Nil)))
      }
    }
    def unbind(key: String, value: Frequency) = Map(key -> value.toString)
  }
}

object Stagger extends Enumeration {
  type Stagger = Value
  val jan = Value
  val feb = Value
  val mar = Value

  implicit def toString(f : Stagger.Value) : String = f.toString
  implicit val format: Format[Stagger] = Format(Reads.enumNameReads(Stagger), Writes.enumNameWrites)

  implicit def formatter : Formatter[Stagger] = new Formatter[Stagger] {
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case "jan"              => Right(Stagger.jan)
        case "feb"              => Right(Stagger.feb)
        case "mar"              => Right(Stagger.mar)
        case _                  => Left(Seq(FormError(key, "error.required", Nil)))
      }
    }
    def unbind(key: String, value: Stagger) = Map(key -> value.toString)
  }
}

object DateSelection extends Enumeration {
  val company_registration_date = Value
  val business_start_date = Value
  val specific_date = Value
  val calculated_date = Value

  implicit def toString(f : DateSelection.Value) : String = f.toString
}

case class Start(date: Option[LocalDate])
object Start {
  implicit val format = Json.format[Start]
}

case class Returns(zeroRatedSupplies: Option[BigDecimal],
                   reclaimVatOnMostReturns: Option[Boolean],
                   frequency: Option[Frequency.Value],
                   staggerStart: Option[Stagger.Value],
                   start: Option[Start])

object Returns {
  implicit val format: OFormat[Returns] = Json.format[Returns]

  def empty = Returns(None, None, None, None, None)
}
