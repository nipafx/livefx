# LiveFX

My stream layout as a web app (that OBS shows via an embedded browser).
This is a Spring Boot + React app.

To run this in development:

* launch the class `LiveFxApplication`
* in the root of the project:
	* run `nvm use`
	* run `npm run start`

Note that building the Spring Boot UberJAR works but the app within it doesn't.

## Features

Chat:
* simple markup with a Markdown-style syntax:
	* text markup: `*bold*`, `_italic_`, `+emphasis+`, `~strikethrough~`, and `` `code` ``
	* text between three backticks is interpreted as a code block
* resolution of badges and emotes
* a chatbot that reads `!$command` messages and replies to the `$command` with its own message
* to keep the chat clean, it doesn't post reward redemption messages

Audience interaction:
* change accent color
* switch to the scene that shows the large screen

Back-end configuration (all updated live):
* theme color and pinning (to prevent audience from changing it)
* scene switching (to prevent audience from switching to the screen)
* on-stream guests
* stream title, description, and tags (the Twitch stream setting is updated accordingly)

Misc:
* Twitch credential management, including reauthorization when possible

## Feature Ideas

### Twitch

#### Chat

* create chat log
* handle "RECONNECT" messages that Twitch sends, so IRC doesn't disconnect
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

* allow (temporary display of "notes" instead of "chat" tab)
* create intro screen
	* configure target time
	* display countdown in frontend

### Internet of 💩

* change lighting color from Twitch

### Implementation

* use TypeScript
* maybe replace microhttp with inbuilt simple web server
