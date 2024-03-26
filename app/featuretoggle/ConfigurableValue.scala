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

package featuretoggle

object ConfigurableValue {

  val prefix = "feature-switch"

  val configurableValues: Seq[ConfigurableValue] = Seq(
  )

  def apply(str: String): ConfigurableValue =
    configurableValues find (_.name == str) match {
      case Some(config) => config
      case None => throw new IllegalArgumentException("Invalid configurable value: " + str)
    }

  def get(string: String): Option[ConfigurableValue] = configurableValues find (_.name == string)

  sealed trait ConfigurableValue {
    val name: String
    val displayText: String
    val hint: Option[String] = None
  }


}
