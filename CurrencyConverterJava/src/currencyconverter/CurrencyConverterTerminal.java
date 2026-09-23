package currencyconverter;

import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverterTerminal {
    private static final String[] COMMON = {
            "INR", "USD", "EUR", "GBP", "JPY", "AUD",
            "CAD", "CHF", "CNY", "AED", "SGD"
    };

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner scanner = new Scanner(System.in);
        ExchangeRateService service = new ExchangeRateService();

        System.out.println("==============================================");
        System.out.println("          JAVA CURRENCY CONVERTER");
        System.out.println("          LIVE EXCHANGE RATES");
        System.out.println("==============================================");

        while (true) {
            System.out.println("\n1. Convert currency");
            System.out.println("2. Show supported/common currencies");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            if ("3".equals(choice)) {
                System.out.println("Thank you for using Currency Converter.");
                break;
            }

            if ("2".equals(choice)) {
                System.out.println("Common currencies: " + String.join(", ", COMMON));
                continue;
            }

            if (!"1".equals(choice)) {
                System.out.println("Invalid choice.");
                continue;
            }

            try {
                System.out.print("Enter amount: ");
                double amount = Double.parseDouble(scanner.nextLine().trim());

                if (amount < 0) {
                    System.out.println("Amount cannot be negative.");
                    continue;
                }

                System.out.print("From currency (e.g. INR): ");
                String from = scanner.nextLine().trim().toUpperCase();

                System.out.print("To currency (e.g. USD): ");
                String to = scanner.nextLine().trim().toUpperCase();

                if (!from.matches("[A-Z]{3}") || !to.matches("[A-Z]{3}")) {
                    System.out.println("Currency codes must be 3 letters, e.g. INR or USD.");
                    continue;
                }

                System.out.println("\nFetching latest exchange rate...");

                ExchangeRateService.RateResult result = service.getRates(from);
                Map<String, Double> rates = result.getRates();

                if (!rates.containsKey(to)) {
                    System.out.println("Target currency " + to + " is not available.");
                    continue;
                }

                double rate = rates.get(to);
                double converted = amount * rate;

                System.out.printf("%n%.2f %s = %.2f %s%n", amount, from, converted, to);
                System.out.printf("Exchange rate: 1 %s = %.8f %s%n", from, rate, to);
                System.out.println("Provider last update: " + result.getLastUpdate());
                System.out.println("Next provider update: " + result.getNextUpdate());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric amount.");
            } catch (Exception e) {
                System.out.println("Could not fetch live rates: " + e.getMessage());
                System.out.println("Check your internet connection and try again.");
            }
        }

        scanner.close();
    }
}
