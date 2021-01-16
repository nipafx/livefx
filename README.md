# Calendar

A calendar view on static data, but all of those details may change between now and later - who knows, maybe I can even come up with a good name. 😬

## What I did

### Setup

* went to [spring initializr](https://start.spring.io/) to create a Spring Boot App:
	* Java 15, Maven, Spring Boot 2.4.2, JAR
	* _Spring Reactive Web_ as dependency
	* bump Java to 16
* applied [AGPL](https://www.gnu.org/licenses/agpl-3.0.en.html)
* replaced JUnit 4 with JUnit 5
* added React frontend
	* ran `npx create-react-app`
	* used [react-app-rewired](https://github.com/timarney/react-app-rewired) to align frontend's folder structure with project layout
