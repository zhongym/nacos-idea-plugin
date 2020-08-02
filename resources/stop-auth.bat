@echo off
for /f "tokens=1 delims= " %%i in ('jps^|findstr "mall-auth"') do (
	echo kill the process mall-auth who use the pid [%%i]
    taskkill /F /pid %%i
)

for /f "tokens=1 delims= " %%i in ('jps^|findstr "MallAuthApplication"') do (
	echo kill the process MallAuthApplication who use the pid [%%i]
    taskkill /F /pid %%i
)
