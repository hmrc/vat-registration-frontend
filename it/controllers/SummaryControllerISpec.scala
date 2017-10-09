package controllers

import fixtures.VatRegistrationFixture
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class SummaryControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with VatRegistrationFixture {
  "GET Summary page" should {
    "display the summary page correctly" when {
      "the company is NOT incorporated" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfile
          .vatScheme.contains
      }
    }
  }
}
