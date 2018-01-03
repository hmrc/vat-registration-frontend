/*
 * Copyright 2018 HM Revenue & Customs
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

trait ViewModelFormat[T] {

  type Group

  def read(group: Group): Option[T]

  def update(viewModel: T, group: Option[Group]): Group

}

object ViewModelFormat {

  type Aux[T, Group0] = ViewModelFormat[T] {type Group = Group0}

  def apply[T, G](readF: G => Option[T], updateF: (T, Option[G]) => G) = new ViewModelFormat[T] {
    override type Group = G

    def read(group: Group): Option[T] = readF(group)

    def update(viewModel: T, group: Option[Group]): Group = updateF(viewModel, group)

  }

}
