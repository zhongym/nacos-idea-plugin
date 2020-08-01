@echo off
for /f "tokens=1 delims= " %%i in ('jps^|findstr "mall-gateway"') do (
	echo kill the process mall-gateway who use the pid [%%i]
    taskkill /F /pid %%i
)

for /f "tokens=1 delims= " %%i in ('jps^|findstr "MallGatewayApplication"') do (
	echo kill the process MallGatewayApplication who use the pid [%%i]
    taskkill /F /pid %%i
)
