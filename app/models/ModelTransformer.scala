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

import models.api.VatScheme

trait ViewModelTransformer[L] {

  // Upserts (selectively converts) a View model object to its API model counterpart
  def toApi(logicalGroup: L): L
}

trait ApiModelTransformer[V] {

  // Returns a view model for a specific part of a given VatScheme API model
  def apply(vatScheme: VatScheme): V
}