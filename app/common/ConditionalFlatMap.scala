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

package common

import cats.Applicative
import cats.kernel.Monoid

import scala.language.higherKinds

object ConditionalFlatMap {

  implicit class CustomApplicativeOps[F[_] : Applicative, A](fa: => F[A]) {

    def onlyIf(condition: Boolean)(implicit F: Applicative[F]): F[Unit] =
      if (condition) F.map(fa)(_ => ()) else F.pure(())

    def onlyIfM(condition: Boolean)(implicit F: Applicative[F], A: Monoid[A]): F[A] =
      if (condition) fa else F.pure(A.empty)

  }

}
