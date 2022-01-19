package support

trait RegistrationsApiStubs {

  private val apiBaseUri = "/vatreg/registrations"

  val registrationsApi =
    new APIStub(apiBaseUri)
      with CanGet
      with CanPost
      with CanDelete

  val specificRegistrationApi = (regId: String) =>
    new APIStub(s"$apiBaseUri/$regId")
      with CanGet
      with CanPut
      with CanPost
      with CanDelete

  val sectionsApi = (regId: String, sectionId: String) =>
    new APIStub(s"$apiBaseUri/$regId/sections/$sectionId")
      with CanGet
      with CanPut
      with CanPatch
      with CanDelete

}
