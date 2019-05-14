@ECHO OFF

SET WD=%CD%
SET HD=%~dp0
SET PARAMS=%*

SET KARAF_TITLE=Karaf
SET PAUSE=true

ECHO Starting Karaf (${apache.karaf.version})...
CALL "%HD%bin\karaf.bat" %PARAMS%
