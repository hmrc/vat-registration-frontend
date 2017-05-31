/*
 * Copyright 2017 HM Revenue & Customs
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

package models.api

import play.api.libs.json.Json

case class CompletionCapacity(
                    name: Name,
                    role: String
                  ){

  override def equals(obj: Any): Boolean = obj match {
    case CompletionCapacity(nameObj, roleObj)
      if role.equalsIgnoreCase(roleObj) && (nameObj == name) => true
    case _ => false
  }

  override def hashCode: Int = 1 // TODO temporary fix
}

object CompletionCapacity {

  implicit val format = Json.format[CompletionCapacity]

  val empty = CompletionCapacity(Name.empty, "")

}
