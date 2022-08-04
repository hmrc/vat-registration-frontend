
package controllers.transactor

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.{AccountantAgent, DeclarationCapacityAnswer, PersonalDetails, TransactorDetails}
import org.jsoup.Jsoup
import play.api.test.Helpers._


class AgentNameControllerISpec extends ControllerISpec {

  val url = "/agent-name"

  val firstNameField = "firstName"
  val lastNameField = "lastName"

  def fieldValue(body: String, fieldName: String): String =
    Jsoup.parse(body).getElementById(fieldName).`val`()

  "GET /agent-name" must {
    "return OK with an empty form" in new Setup {
      given()
        .user.isAuthorised(arn = Some(testArn))
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get)

      res.status mustBe OK
      fieldValue(res.body, firstNameField) mustBe ""
      fieldValue(res.body, lastNameField) mustBe ""
    }
    "return OK with a filled form" in new Setup {
      given()
        .user.isAuthorised(arn = Some(testArn))
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(TransactorDetails(personalDetails = Some(testPersonalDetails))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get)

      res.status mustBe OK
      fieldValue(res.body, firstNameField) mustBe testFirstName
      fieldValue(res.body, lastNameField) mustBe testLastName
    }
  }

  "POST /agent-name" when {
    "the name is valid" when {
      "the task list is enabled" must {
        "redirect to the task list" in new Setup {
          enable(TaskList)
          val firstUpdateDetails = TransactorDetails(personalDetails = Some(PersonalDetails(
            firstName = testFirstName,
            lastName = testLastName,
            identifiersMatch = true,
            arn = Some(testArn)
          )))

          val secondUpdateDetails = firstUpdateDetails.copy(declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent)))

          given()
            .user.isAuthorised(arn = Some(testArn))
            .s4lContainer[TransactorDetails].isEmpty
            .s4lContainer[TransactorDetails].isUpdatedWith(firstUpdateDetails)
            .s4lContainer[TransactorDetails].isUpdatedWith(secondUpdateDetails)
            .registrationApi.getSection[TransactorDetails](None)
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(transactorDetails = Some(validTransactorDetails)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Map(firstNameField -> testFirstName, lastNameField -> testLastName)))

          res.status mustBe SEE_OTHER
          res.headers(LOCATION) must contain(controllers.routes.TaskListController.show.url)
        }
      }
      "the task list is disabled" must {
        "store the name in S4L, ARN and declaration capacity, then redirect to the transactor telephone number page" in new Setup {
          disable(TaskList)
          val firstUpdateDetails = TransactorDetails(personalDetails = Some(PersonalDetails(
            firstName = testFirstName,
            lastName = testLastName,
            identifiersMatch = true,
            arn = Some(testArn)
          )))

          val secondUpdateDetails = firstUpdateDetails.copy(declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent)))

          given()
            .user.isAuthorised(arn = Some(testArn))
            .s4lContainer[TransactorDetails].isEmpty
            .s4lContainer[TransactorDetails].isUpdatedWith(firstUpdateDetails)
            .s4lContainer[TransactorDetails].isUpdatedWith(secondUpdateDetails)
            .registrationApi.getSection[TransactorDetails](None)
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(transactorDetails = Some(validTransactorDetails)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).post(Map(firstNameField -> testFirstName, lastNameField -> testLastName)))

          res.status mustBe SEE_OTHER
          res.headers(LOCATION) must contain(routes.TelephoneNumberController.show.url)
        }
      }
    }
    "the name is invalid" must {
      "return BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised(arn = Some(testArn))
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(transactorDetails = Some(validTransactorDetails)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(firstNameField -> "%%%", lastNameField -> testLastName)))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
