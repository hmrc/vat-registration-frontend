
package controllers.otherbusinessinvolvements

import itutil.ControllerISpec
import models.OtherBusinessInvolvement
import play.api.http.HeaderNames
import play.api.test.Helpers._

class OtherBusinessCheckAnswersControllerISpec extends ControllerISpec {

  def url(index: Int) = s"/other-business-involvement/$index/check-answers"

  val testIndexBelow1 = -1
  val testIndex1 = 1
  val testIndex2 = 2
  val testIndex3 = 3
  val testIndex4 = 4
  val testIndexMax = 10
  val testIndexOverMax = 12

  val testVatNumber = "testVatNumber"
  val testOtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testCompanyName),
    hasVrn = Some(true),
    vrn = Some(testVatNumber),
    stillTrading = Some(true)
  )

  "GET /other-business-involvements/check-answers/:index" when {
    "the section is in S4L" must {
      "return OK when the user has a VRN" in new Setup {
        given
          .user.isAuthorised()
          .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndex1)).contains(testOtherBusinessInvolvement)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url(testIndex1)).get)

        res.status mustBe OK
      }
    }
    "the section is complete and stored in the back end" must {
      "return OK when the user has a VRN" in new Setup {
        given
          .user.isAuthorised()
          .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndex1)).isEmpty
          .registrationApi.getSection[OtherBusinessInvolvement](Some(testOtherBusinessInvolvement), idx = Some(testIndex1))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url(testIndex1)).get)

        res.status mustBe OK
      }
    }
    "no data exists for the index" when {
      "the index is valid" must {
        "redirect to the index" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndex2)).isEmpty
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex2))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List()))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url(testIndex2)).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex1).url)
        }
      }
      "the index is higher than the highest possible index (n + 1)" must {
        "redirect to the first index without data" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndex3)).isEmpty
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex3))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url(testIndex3)).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex2).url)
        }
        "redirect to the highest possible index" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndex4)).isEmpty
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndex4))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url(testIndex4)).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex2).url)
        }
      }
      "the index is higher than the allowed maximum (10)" must {
        "redirect to index 10" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndexOverMax)).isEmpty
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndexOverMax))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
              testOtherBusinessInvolvement,
            )))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url(testIndexOverMax)).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndexMax).url)
        }
      }
      "the index is less than 1" must {
        "redirect to index 1" in new Setup {
          given
            .user.isAuthorised()
            .s4lContainer[OtherBusinessInvolvement](OtherBusinessInvolvement.s4lKey(testIndexBelow1)).isEmpty
            .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(testIndexBelow1))
            .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(testOtherBusinessInvolvement)))
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url(testIndexBelow1)).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessCheckAnswersController.show(testIndex1).url)
        }
      }
    }
  }

  "POST /other-business-involvements/check-answers" must {
    "redirect to the summary page" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(routes.OtherBusinessCheckAnswersController.submit.url).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
    }
  }

}
