@echo off
SET mypath=%~dp0
%mypath%\bin\java --class-path %mypath%\app;%mypath%\app\calendar.jar org.springframework.boot.loader.PropertiesLauncher
