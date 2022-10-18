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

package controllers

import common.enums.VatRegStatus
import config.FrontendAppConfig
import controllers.transactor.{routes => transactorRoutes}
import featureswitch.core.config._
import itutil.ControllerISpec
import models.api.trafficmanagement.{OTRS, VatReg}
import models.api.{EligibilitySubmissionData, VatSchemeHeader}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate

class JourneyControllerISpec extends ControllerISpec {

  lazy val controller: JourneyController = app.injector.instanceOf(classOf[JourneyController])
  lazy val appConfig: FrontendAppConfig = app.injector.instanceOf(classOf[FrontendAppConfig])

  val thresholdUrl = s"/vatreg/threshold/${LocalDate.now()}"
  val currentThreshold = "50000"

  val showUrl: String = routes.JourneyController.show.url
  val submitUrl: String = routes.JourneyController.submit.url
  val newJourneyUrl: String = routes.JourneyController.startNewJourney.url

  def continueJourneyUrl(regId: String): String =
    routes.JourneyController.continueJourney(Some(regId)).url

  def initJourneyUrl(regId: String): String =
    routes.JourneyController.initJourney(regId).url

  val vatSchemeHeader: VatSchemeHeader = VatSchemeHeader(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    requiresAttachments = false
  )
  val vatSchemeHeader2: VatSchemeHeader = VatSchemeHeader(
    registrationId = "2",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    requiresAttachments = false
  )

  s"GET $showUrl" when {
    "SaveAndContinueLater FS is disabled" must {
      s"return a redirect to $newJourneyUrl" when {
        "user is authenticated and authorised to access the app without profile" in new Setup {
          disable(SaveAndContinueLater)
          given()
            .user.isAuthorised()
            .registrationApi.registrationCreated()
            .registrationApi.getSection(Some(VatRegStatus.draft))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }

        "user is authenticated and authorised to access the app with profile" in new Setup {
          disable(SaveAndContinueLater)

          given()
            .user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }
    }

    "SaveAndContinueLater FS is enabled" when {
      "the registrations API returns no registrations" must {
        s"redirect to $newJourneyUrl" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised()
            .registrationApi.getAllRegistrations(Nil)

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }

      "the registratsions API returns 1 in flight registration" must {
        "return OK and display the select registration page" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised()
            .audit.writesAudit()
            .audit.writesAuditMerged()
            .registrationApi.getSection(Some(VatRegStatus.draft))
            .registrationApi.getAllRegistrations(List(vatSchemeHeader))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe OK
        }
      }

      "the registrations API returns more than 1 in flight registration" must {
        "return OK and display the select registration page" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised()
            .registrationApi.getSection(Some(VatRegStatus.draft), regId = vatSchemeHeader2.registrationId)
            .registrationApi.getAllRegistrations(List(vatSchemeHeader, vatSchemeHeader2))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe OK
        }
      }
    }

    "SaveAndContinueLater and MultipleRegistrations FS are enabled" when {
      "the registrations API returns in flight registrations" must {
        "redirect to manage registrations page" in new Setup {
          enable(SaveAndContinueLater)
          enable(MultipleRegistrations)

          given()
            .user.isAuthorised()
            .registrationApi.getSection(Some(VatRegStatus.draft))
            .registrationApi.getAllRegistrations(List(vatSchemeHeader, vatSchemeHeader2))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ManageRegistrationsController.show.url)
        }
      }

      "the registrations API returns no registrations" must {
        s"redirect to new journey page" in new Setup {
          enable(SaveAndContinueLater)
          enable(MultipleRegistrations)

          given()
            .user.isAuthorised()
            .registrationApi.getAllRegistrations(Nil)

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }
    }
  }

  s"POST $submitUrl" when {
    "the user wants to start a new registration" must {
      "redirect to the new journey url" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Json.obj("value" -> true)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.startNewJourney.url)
      }
    }
    "the user wants to continue their existing registration" must {
      "redirect to the continue journey url" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Json.obj("value" -> false)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.continueJourney(Some(testRegId)).url)
      }
    }
    "the user has not selected journey option" must {
      "return a BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(""))
        res.status mustBe BAD_REQUEST
      }
    }
  }

  s"GET $newJourneyUrl" when {
    "the MultipleRegistrations feature switch is disabled" when {
      "redirect to the Honesty Declaration page" when {
        "user is authenticated and authorised to access the app without profile" in new Setup {
          disable(MultipleRegistrations)

          given()
            .user.isAuthorised()
            .registrationApi.registrationCreated()
            .registrationApi.getSection(Some(VatRegStatus.draft))

          val res: WSResponse = await(buildClient(newJourneyUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
        }
      }
    }
    "the MultipleRegistrations feature switch is enabled" when {
      "redirect to the Application Reference page" when {
        "user is authenticated and authorised to access the app without profile" in new Setup {
          enable(MultipleRegistrations)

          given()
            .user.isAuthorised()
            .registrationApi.registrationCreated()
            .registrationApi.getSection(Some(VatRegStatus.draft))

          val res: WSResponse = await(buildClient(newJourneyUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationReferenceController.show.url)
        }
      }
    }
  }

  s"GET ${continueJourneyUrl(testRegId)}" when {
    "the registration is submitted" must {
      "redirect to the application submission page" in new Setup {
        enable(MultipleRegistrations)
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.submitted))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme.copy(status = VatRegStatus.submitted)))
          .trafficManagement.passes(VatReg)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationSubmissionController.show.url)
      }
    }
    "the registration requires attachments" must {
      "redirect to the documents required page" in new Setup {
        enable(MultipleRegistrations)
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme).as[JsObject] ++ Json.obj("attachments" -> Json.obj()))
          .trafficManagement.passes(VatReg)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "the Task List feature is enabled" must {
      "redirect to the documents required page" in new Setup {
        enable(MultipleRegistrations)
        enable(TaskList)
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme).as[JsObject] ++ Json.obj("attachments" -> Json.obj()))
          .trafficManagement.passes(VatReg)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "on failure with missing vat schema details for given reg-id" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        disable(TaskList)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "for missing journey id" must {
      "return BAD_REQUEST" in new Setup {
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(routes.JourneyController.continueJourney(None).url).get())
        res.status mustBe BAD_REQUEST
      }
    }
    "the channel for traffic management is VatReg" when {
      "the multiple registrations feature switch is enabled" when {
        "traffic management passes (VatReg)" must {
          "redirect to the Application Reference page" in new Setup {
            enable(MultipleRegistrations)
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
              .trafficManagement.passes(VatReg)

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationReferenceController.show.url)
          }
        }
        "when traffic management fails" must {
          "redirect to the Application Reference page" in new Setup {
            enable(MultipleRegistrations)
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
              .trafficManagement.fails

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationReferenceController.show.url)
          }

          "redirect to the Task List Controller page if TL FS enabled and submission data available" in new Setup {
            enable(TaskList)
            enable(MultipleRegistrations)
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
              .registrationApi.getSection(Some(testEligibilitySubmissionData))
              .trafficManagement.fails

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.TaskListController.show.url)
            disable(TaskList)
          }
        }
      }
      "the multiple registrations feature switch is disabled" when {
        "traffic management passes (VatReg)" must {
          "redirect to the Honesty Declaration page" in new Setup {
            disable(MultipleRegistrations)
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
              .trafficManagement.passes(VatReg)

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
          }
        }
        "traffic management fails" must {
          "redirect to the Honesty Declaration page" in new Setup {
            disable(MultipleRegistrations)
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
              .trafficManagement.fails

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
          }
        }
      }
    }
    "the channel for traffic management is OTRS" must {
      "redirect to OTRS" in new Setup {
        enable(TrafficManagementPredicate)
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))
          .trafficManagement.passes(OTRS)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.otrsRoute)
        disable(TrafficManagementPredicate)
      }
    }
    "the Traffic Management FS is disabled" must {
      "pass all users as VatReg users" in new Setup {
        enable(TrafficManagementPredicate)

        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)

        disable(TrafficManagementPredicate)
      }
    }
  }

  s"GET ${routes.JourneyController.initJourney(testRegId)}" when {
    "eligibility data exists for the user" when {
      "the current profile has been set up successfully" when {
        "the user isn't a transactor" must {
          "redirect to the business identification resolver" in new Setup {
            given()
              .user.isAuthorised()
              .registrationApi.getSection(Some(VatRegStatus.draft))
              .registrationApi.getSection(Some(testEligibilitySubmissionData))

            insertCurrentProfileIntoDb(currentProfile, sessionId)

            val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessIdentificationResolverController.resolve.url)
          }
        }
        "the user is a transactor" when {
          "the user is not an agent" must {
            "redirect to the Part of An Organisation page" in new Setup {
              given()
                .user.isAuthorised()
                .registrationApi.getSection(Some(VatRegStatus.draft))
                .registrationApi.getSection(Some(testEligibilitySubmissionData.copy(isTransactor = true)))

              insertCurrentProfileIntoDb(currentProfile, sessionId)

              val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

              res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.PartOfOrganisationController.show.url)
            }
          }
          "the user is an agent" must {
            "redirect to the Agent Name page when the FullAgentJourney FS is enabled" in new Setup {
              enable(FullAgentJourney)
              given()
                .user.isAuthorised(arn = Some(testArn))
                .registrationApi.getSection(Some(VatRegStatus.draft))
                .registrationApi.getSection(Some(testEligibilitySubmissionData.copy(isTransactor = true)))

              insertCurrentProfileIntoDb(currentProfile, sessionId)

              val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

              res.header(HeaderNames.LOCATION) mustBe Some(controllers.transactor.routes.AgentNameController.show.url)
            }
            "redirect to the Task List when the Task List FS is enabled" in new Setup {
              enable(TaskList)
              given()
                .user.isAuthorised(arn = Some(testArn))
                .registrationApi.getSection(Some(VatRegStatus.draft))
                .registrationApi.getSection(Some(testEligibilitySubmissionData.copy(isTransactor = true)))

              insertCurrentProfileIntoDb(currentProfile, sessionId)

              val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
            }
            "redirect to the Business Identification resolver when the FullAgentJourney FS and TaskList FS are disnabled" in new Setup {
              disable(FullAgentJourney)
              disable(TaskList)
              given()
                .user.isAuthorised(arn = Some(testArn))
                .registrationApi.getSection(Some(VatRegStatus.draft))
                .registrationApi.getSection(Some(testEligibilitySubmissionData.copy(isTransactor = true)))

              insertCurrentProfileIntoDb(currentProfile, sessionId)

              val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessIdentificationResolverController.resolve.url)
            }
          }
        }
      }
    }
    "eligibility data doesn't exist for the user" must {
      "redirect to the start of the journey" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](None)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.show.url)
      }
    }
  }

}
