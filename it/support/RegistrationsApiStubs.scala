package support

trait RegistrationsApiStubs {

  private val apiBaseUri = "/vatreg/registrations"

  val registrationsApi =
    new APIStub(apiBaseUri)
      with CanGet
      with CanPost
      with CanDelete

  val sectionsApi = (sectionId: String) =>
    new APIStub(s"$apiBaseUri/sections/$sectionId")
      with CanGet
      with CanPut
      with CanPatch
      with CanDelete

}
