`GET         /current-profile-setup`  
Calls our backend which will try to GET current profile from Business Registration (BR), if it's not found our backend will POST to Business Registration to create a profile entry; this only needs to be called once per user to "setup" that user.

`GET         /s4l-teardown`  
Clears all data stored in SAVE4LATER mongo collection for the current user.

`GET         /test-setup`  
`POST        /test-setup`  
For the test setup page used by acceptance tests.

`GET         /sic-stub`  
`POST        /sic-stub`  
Temporary solution for capturing SIC codes.

`GET         /working-days`  
Lists upcoming working days.

`GET         /ct-registration`  
Gets CTActiveDate and displays it on screen.

`GET         /insert-ii-stub-data`  
This will load some applicant details and home addresses into II Frontend Stub.

`GET         /incorp-company`         
Inserts the required data directly into II through our backend which will essentially incorporate which ever user you are authenticated as via their transaction ID, used for testing post incorporated journeys.

`GET        /feature-switches`
`POST       /feature-switches`
Used to turn features on/off.

`GET         /submission-payload`
Used to get the json payload from a submission, so we can confirm in Acceptance Tests that the correct JSON is being submitted.

`GET        /file-upload`
Used to test the integration with Upscan.

`GET        /setup-traffic-management`
`POST       /setup-traffic-management`
Used to set/reset the daily quota for local testing of traffic management.
