`GET         /current-profile-setup`  
calls our backend which will try to GET current profile from Business Registration (BR), if it's not found our backend will POST to Business Registration to create a profile entry; this only needs to be called once per user to "setup" that user

`GET         /s4l-teardown`  
clears all data stored in SAVE4LATER mongo collection for the current user

`GET         /db-teardown`  
clears all data for currently logged on user from our backend mongo collection

`GET         /test-setup`  
`POST        /test-setup`  
for the test setup page used by acceptance tests

`GET         /sic-stub`  
`POST        /sic-stub`  
temporary solution for capturing SIC codes

`GET         /working-days`  
lists upcoming working days

`GET         /ct-registration`  
gets CTActiveDate and displays it on screen

`GET         /insert-ii-stub-data`  
this will load some officer details and home addresses into II Frontend Stub.

`GET         /insert-ii-stub-data-incorp`  
this will load some incorporation details into II Frontend Stub. In order for VATREG backend to pick this data up, some feature toggles on the II service will have to be engaged.
1. First, switch your II service's transactional API to the stub which you have pre-loaded with data using this test endpoint.  
http://localhost:9976/incorporation-information/test-only/feature-switch/transactionalAPI/stub
2. Next, enable the incorporation updates (scheduler):  
http://localhost:9976/incorporation-information/test-only/feature-switch/incorpUpdate/on

`GET         /wipe-incorp-stub-data`  
this will clear out the whole 'incorp-submission-status' collection from "incorporation-frontend-stubs" database

`GET         /incorp-info`  
returns JSON retrieved from our backend - whatever incorporation status we hold, can be checked in the browser

