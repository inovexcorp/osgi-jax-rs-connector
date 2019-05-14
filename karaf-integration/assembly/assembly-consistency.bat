@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call ../../mvnw clean install -Passembly-consistency %PARAMS%

cd "%WD%"

PAUSE
