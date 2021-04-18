# Calendar

A calendar view on static data, but all of those details may change between now and later - who knows, maybe I can even come up with a good name. 😬

## How to try it out

### Setup

* clone the repo
* with Java 16, run `mvn verify` in the project root folder
* with Node 15, run `npm install` in the project root folder

### Launch

* open the app in your IDE and launch `CalendarApplication`
* run `npm start` in the root directory
* go to localhost:3000

## How to build it

This app creates a class-path image, i.e.:

* a Java runtime image containing the required modules with `jlink`
* Spring Boot's fat JAR in a subfolder of the runtime image
* a launch script in the image's `bin` directory

Check `build-images.sh` for details or simply execute these two scripts:

```shell
./build.sh
./build-images.sh
```

To launch the application:

* specify path to data folder on the `app/application.properties` in the image
* launch `bin/calendar` in the image

Go to [localhost:8080](http://localhost:8080/) to see the calendar.

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
* edited generated backend and frontend code
	* removed unneeded code
	* adapted to calendar domain
	* indented JS/CSS code with tabs
	* made frontend GET data from backend

### First Steps

* backend
	* made domain model richer
	* added application property `data.folder` to configure folder with JSON files
	* created Repository based on JSON files in folder 
	* used local records (yay!) to ease parsing domain types from JSON
	* created controller for REST endpoints
	* handled wiring without relying on Spring annotations in domain model
* frontend
	* created calendar view with CSS grid
	* sorted entries into grid
	* used Luxon for date manipulation

### Refinement

* enhanced build
	* included frontend code in JAR
	* turned backend into Java module
	* used jlink to create self-contained application image
* applied TypeScript (way too late; should've started with it)
