
package controllers.otherbusinessinvolvements

import itutil.ControllerISpec
import models.OtherBusinessInvolvement
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._


class ObiSummaryControllerISpec extends ControllerISpec {

  def pageUrl(): String = routes.ObiSummaryController.show.url

  val testObis = List(
    OtherBusinessInvolvement(
      businessName = Some(testCompanyName),
      hasVrn = Some(true),
      vrn = Some(testVrn),
      hasUtr = None,
      utr = None,
      stillTrading = Some(true)
    )
  )

  "GET" when {
    "the user has no OBIs" must {
      "redirect to the 'Do you have OBIs' page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(Nil))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl()).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
      }
    }

    "the user has 1 or more OBIs" must {
      "return INTERNAL_SERVER_ERROR if any of the OBI has business name missing" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis.map(_.copy(businessName = None))))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl()).get)

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST" when {
    "the user doesn't select an answer" when {
      "the user has no OBIs" must {
        "redirect to the 'Do you have OBIs' page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(Nil))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
        }
      }
      "the user has 1 or more OBIs" must {
        "return BAD_REQUEST with the view" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "")))

          res.status mustBe BAD_REQUEST
        }
      }
    }
    "the user selects an answer" when {
      "the answer is 'Yes" must {
        "redirect to the 'Other Business Name' page for idx + 1" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessNameController.show(2).url)
        }
      }
      "the answer is 'No" must {
        "redirect to the 'Trading Name Resolver'" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(testObis))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(pageUrl()).post(Json.obj("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
        }
      }
    }
  }

}
