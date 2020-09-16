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
this will load some applicant details and home addresses into II Frontend Stub.

`GET         /incorp-company`         
inserts the required data directly into II through our backend which will essentially incorporate which ever user you are authenticated as via their transaction ID, used for testing post incorporated journeys.

`GET         /incorp-info`  
returns JSON retrieved from our backend - whatever incorporation status we hold, can be checked in the browser

