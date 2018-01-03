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

/**
  * Created by Chris Poole on 15/12/17.
  */

package object services {

  type Completion[T] = Either[T, T]
  val Incomplete   = scala.util.Left
  val Complete     = scala.util.Right


//  sealed trait CompletedBlock[T]{
//    def fold[X](complete: T => X, incomplete: T => X): X = this match {
//      case Complete(a) => complete(a)
//      case Incomplete(b) => incomplete(b)
//    }
//  }
//  case class Complete[T](a: T) extends CompletedBlock[T]
//  case class Incomplete[T](a: T) extends CompletedBlock[T]

  type CompletedBlock[T]     = Either[T, T]
}
