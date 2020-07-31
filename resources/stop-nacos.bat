@echo off
for /f "tokens=1 delims= " %%i in ('jps^|findstr "MallNacosApplication"') do (
	echo kill the process MallNacosApplication who use the pid [%%i]
    taskkill /F /pid %%i
)

for /f "tokens=1 delims= " %%i in ('jps^|findstr "nacos-server"') do (
    echo kill the process nacos-server who use the pid [%%i]
    taskkill /F /pid %%i
)