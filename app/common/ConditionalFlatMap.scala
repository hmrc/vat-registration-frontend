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

package common

import cats.Applicative

import scala.language.higherKinds

/**
  * This class is used to allow conditional executions in for-comprehensions
  * e.g.
  * <pre>
  * for {
  * foo <- something()
  * _ <- somethingElse() onlyIf condition
  * } yield (foo)
  * </pre>
  *
  * Here somethingElse() only executes if condition is true
  *
  * Also, note that this can be removed once we upgrade cats library to 1.0
  * new version of cats includes .whenA syntax for doing this kind of stuff
  */
object ConditionalFlatMap {

  implicit class CustomApplicativeOps[F[_], A](fa: => F[A])(implicit F: Applicative[F]) {

    def onlyIf(condition: Boolean): F[Unit] =
      if (condition) F.map(fa)(_ => ()) else F.pure(())

  }

}
