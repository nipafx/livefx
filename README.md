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

#### Chat

* create chat log (with Chatty?)
* use author's Twitch color as alt-color for their messages
* format code snippets
* highlight snippets
* replace Moobot
* how does moderation work with the on-screen chat? / remove moderated messages

#### Channel

* set stream language from configuration
* set notification text from configuration once that is possible:
  https://twitch.uservoice.com/forums/310213-developers/suggestions/39703660-live-notification

#### Rewards

* configure rewards locally (to easier configure rewards depending on stream topic or desk position)
* auto-patch reward status (only works for rewards that were created by the app)

### OBS

* create intro screen
    * configure target time
    * display countdown in frontend

### Internet of 💩

* change lighting color from Twitch

### Implementation

* use TypeScript
* maybe replace microhttp with inbuilt simple web server
