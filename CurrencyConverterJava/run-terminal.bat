@echo off
if not exist out mkdir out
javac -d out src\currencyconverter\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Make sure Java JDK 11 or newer is installed.
    pause
    exit /b 1
)
java -cp out currencyconverter.CurrencyConverterTerminal
pause
