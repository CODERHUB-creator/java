#!/bin/sh
mkdir -p out
javac -d out src/currencyconverter/*.java || exit 1
java -cp out currencyconverter.CurrencyConverterGUI
