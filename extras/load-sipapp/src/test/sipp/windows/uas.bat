@ECHO OFF
call "setEnv.cmd"
title SIPp-UAS

%SIPP_EXE% -sf ..\uas.xml -i %SIPP_HOST% -p %SIPP_UAS_PORT% %SIPP_OPTIONS% %TRACE_OPTIONS% -mi %SIPP_HOST%