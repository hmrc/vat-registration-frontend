`GET         /current-profile-setup`  
calls our backend which will try to GET current profile from Business Registration (BR), if it's not found our backend will POST to Business Registration to create a profile entry; this only needs to be called once per user to "setup" that user

`GET         /s4l-teardown                controllers.test.TestCacheController.tearDownS4L`  
clears all data stored in SAVE4LATER mongo collection for the current user

`GET         /db-teardown                 controllers.test.TestVatRegistrationAdminController.dropCollection()`  
clears all data for currently logged on user from our backend mongo collection

`GET         /test-setup                  controllers.test.TestSetupController.show()`  
`POST        /test-setup                  controllers.test.TestSetupController.submit()`  
for the test setup page used by acceptance tests

`GET         /sic-stub                    controllers.test.SicStubController.show()`  
`POST        /sic-stub                    controllers.test.SicStubController.submit()`  
temporary solution for capturing SIC codes

`GET         /working-days                controllers.test.TestWorkingDaysValidationController.show()`  
lists upcoming working days

`GET         /ct-registration             controllers.test.TestCTController.show()`  
gets CTActiveDate and displays it on screen

`GET         /insert-ii-stub-data         controllers.test.IncorporationInformationStubsController.postTestData()`  
this will load some officer details and home addresses into II Frontend Stub

`GET         /insert-ii-stub-data-incorp  controllers.test.IncorporationInformationStubsController.postTestDataIncorp()`  
this will load some incorporation details into II Frontend Stub

`GET         /wipe-incorp-stub-data       controllers.test.IncorporationInformationStubsController.wipeTestDataIncorp()`  
this will clear out the whole 'incorp-submission-status' collection from "incorporation-frontend-stubs" database

`GET         /incorp-info                 controllers.test.IncorporationInformationStubsController.getIncorpInfo()`  
returns JSON retrieved from our backend - whatever incorporation status we hold, can be checked in the browser

