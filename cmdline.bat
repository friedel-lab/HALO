@echo off

if (%1)==() GOTO Call

set mem=-Xmx1024m
set dummy=%1
if NOT %dummy:~0,4% == -Xmx GOTO Call
set mem=%dummy%

:Call

java -cp %~dps0lib\halo.jar;%~dps0lib\jfreechart-1.0.13\lib\* %mem% halo.userinterface.cmdline.CommandLine %* 