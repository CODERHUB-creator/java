# Java Currency Converter

A complete Java desktop + terminal currency converter using live exchange rates.

## Features
- Terminal/console mode
- Java Swing GUI mode
- Live exchange rates from ExchangeRate-API's open endpoint
- INR, USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, AED, SGD and more
- No external Java libraries required
- Java 11+ required
- Automatic API timeout and useful error messages

## Run

### Windows
Double-click `run-gui.bat` for the desktop app.

For terminal mode:
```bat
run-terminal.bat
```

### Linux/macOS
```bash
chmod +x run-gui.sh run-terminal.sh
./run-gui.sh
```

or:
```bash
./run-terminal.sh
```

### Compile manually
```bash
javac -d out src/currencyconverter/*.java
java -cp out currencyconverter.CurrencyConverterGUI
```

Terminal:
```bash
java -cp out currencyconverter.CurrencyConverterTerminal
```

## API
The project uses:
`https://open.er-api.com/v6/latest/{BASE}`

The service returns current exchange-rate data. Internet access is required for live rates.

## Notes
Exchange rates fluctuate and may be delayed depending on the upstream provider. The application displays the provider's update timestamp when available.
