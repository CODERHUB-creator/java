package currencyconverter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class CurrencyConverterGUI extends JFrame {
    private final JTextField amountField = new JTextField("1000");
    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JLabel resultLabel = new JLabel("Enter an amount and click Convert.");
    private final JLabel rateLabel = new JLabel(" ");
    private final JLabel updateLabel = new JLabel(" ");
    private final JButton convertButton = new JButton("CONVERT");
    private final JButton swapButton = new JButton("⇄");

    private static final String[] CURRENCIES = {
            "INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD",
            "CHF", "CNY", "AED", "SGD", "NZD", "HKD", "KRW",
            "THB", "MYR", "ZAR", "BRL", "MXN", "SEK", "NOK"
    };

    public CurrencyConverterGUI() {
        setTitle("Java Currency Converter - Live Rates");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 470);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(25, 30, 25, 30));
        root.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("CURRENCY CONVERTER", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Live exchange rates • Java desktop application",
                SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel amountText = new JLabel("Amount");
        amountText.setFont(new Font("SansSerif", Font.BOLD, 14));
        amountText.setAlignmentX(Component.LEFT_ALIGNMENT);

        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JPanel currencyPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        currencyPanel.setOpaque(false);
        currencyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel fromPanel = labeledCombo("FROM", fromBox);
        JPanel swapPanel = new JPanel(new GridBagLayout());
        swapPanel.setOpaque(false);
        swapButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        swapButton.setToolTipText("Swap currencies");
        swapPanel.add(swapButton);
        JPanel toPanel = labeledCombo("TO", toBox);

        currencyPanel.add(fromPanel);
        currencyPanel.add(swapPanel);
        currencyPanel.add(toPanel);

        convertButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        convertButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        convertButton.setPreferredSize(new Dimension(170, 45));

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(18, 18, 18, 18)));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 23));

        rateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultPanel.add(resultLabel);
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(rateLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(updateLabel);

        center.add(amountText);
        center.add(Box.createVerticalStrut(7));
        center.add(amountField);
        center.add(Box.createVerticalStrut(20));
        center.add(currencyPanel);
        center.add(Box.createVerticalStrut(18));
        center.add(convertButton);
        center.add(Box.createVerticalStrut(20));
        center.add(resultPanel);

        for (String currency : CURRENCIES) {
            fromBox.addItem(currency);
            toBox.addItem(currency);
        }
        fromBox.setSelectedItem("INR");
        toBox.setSelectedItem("USD");

        convertButton.addActionListener(e -> convert());
        amountField.addActionListener(e -> convert());
        swapButton.addActionListener(e -> {
            Object from = fromBox.getSelectedItem();
            fromBox.setSelectedItem(toBox.getSelectedItem());
            toBox.setSelectedItem(from);
            convert();
        });

        root.add(heading, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    private JPanel labeledCombo(String text, JComboBox<String> combo) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(label, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        return panel;
    }

    private void convert() {
        final double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid non-negative amount.",
                    "Invalid amount", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String from = (String) fromBox.getSelectedItem();
        String to = (String) toBox.getSelectedItem();

        convertButton.setEnabled(false);
        resultLabel.setText("Fetching live rate...");
        rateLabel.setText("");
        updateLabel.setText("");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private double rate;
            private double converted;
            private String lastUpdate;
            private String error;

            @Override
            protected Void doInBackground() {
                try {
                    ExchangeRateService service = new ExchangeRateService();
                    ExchangeRateService.RateResult result = service.getRates(from);
                    Map<String, Double> rates = result.getRates();

                    if (!rates.containsKey(to)) {
                        throw new Exception("Currency " + to + " is not available.");
                    }

                    rate = rates.get(to);
                    converted = amount * rate;
                    lastUpdate = result.getLastUpdate();
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                convertButton.setEnabled(true);

                if (error != null) {
                    resultLabel.setText("Unable to fetch rate");
                    rateLabel.setText(error);
                    updateLabel.setText("Check your internet connection.");
                    return;
                }

                resultLabel.setText(String.format("%.2f %s = %.2f %s",
                        amount, from, converted, to));
                rateLabel.setText(String.format("1 %s = %.8f %s", from, rate, to));
                updateLabel.setText("Provider last update: " + lastUpdate);
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverterGUI().setVisible(true));
    }
}
