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

package models

import enums.CacheKeys

trait CacheKey[T] {

  def cacheKey: String

}

object CacheKey {

  def apply[T: CacheKey]: CacheKey[T] = implicitly

  def apply[T](key: => CacheKeys.Value): CacheKey[T] = new CacheKey[T]() {
    override def cacheKey: String = key.toString
  }

}
