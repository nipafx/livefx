# LiveFX

My stream layout as a web app (that OBS shows via an embedded browser).
This is a Spring Boot + React app.

To run this in development:

* launch the class `LiveFxApplication`
* in the root of the project:
    * run `nvm use`
    * run `npm run start`

Note that building the Spring Boot UberJAR works but the app within it doesn't.

## Feature Ideas

### Twitch

* set stream info from configuration

#### Chat

* create chat log (with Chatty?)
* use author's Twitch color as alt-color for their messages
* format code snippets
* highlight snippets
* prevent (certain) reward redemption messages from showing up in chat
* replace Moobot
* how does moderation work with the on-screen chat? / remove moderated messages

### OBS

* allow scene change from Twitch
* create intro screen
    * configure target time
    * display countdown in frontend

### Internet of 💩

* change lighting color from Twitch

### Implementation

* use TypeScript
* maybe replace microhttp with inbuilt simple web server
