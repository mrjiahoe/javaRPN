import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class rpnCalc extends JFrame {
    private static List<String> calculationHistory = new ArrayList<>();
    private static int calcCount = 0;
    
    private JTextField display;
    private JTextArea historyArea;
    private StringBuilder currentExpression;
    private boolean clearOnNextInput;

    public rpnCalc() {
        setTitle("RPN Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout());

        currentExpression = new StringBuilder();

        // Create left panel for history
        JPanel leftPanel = new JPanel(new BorderLayout());
        historyArea = new JTextArea(20, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Arial", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Create top panel for display
        JPanel topPanel = new JPanel(new BorderLayout());
        display = new JTextField();
        display.setEditable(true);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("Arial", Font.PLAIN, 30));
        display.addKeyListener(new DisplayKeyListener());
        topPanel.add(display, BorderLayout.CENTER);

        // Create right panel for display and buttons
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        rightPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add panels to the frame
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        String[] buttonLabels = {
                // "(", ")", "C", "^",
                // "7", "8", "9", "/",
                // "4", "5", "6", "*",
                // "1", "2", "3", "-",
                // "0", ".", "=", "+"
                "AC", "(",  ")", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                " ", "0", ".", "="
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            if (!label.isEmpty()) {
                JButton button = new JButton(label);
                button.setFont(new Font("Arial", Font.PLAIN, 16));
                button.addActionListener(new ButtonClickListener());
                gbc.gridx = i % 4;
                gbc.gridy = i / 4;
                if ("=".equals(label)) {
                    button.setBackground(Color.orange);
                }
                buttonPanel.add(button, gbc);
            }
        }

        return buttonPanel;
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("=")) {
                try {
                    String result = Calculate(currentExpression.toString());
                    display.setText(result);
                    currentExpression = new StringBuilder(String.valueOf(result));
                    updateHistoryArea();
                    clearOnNextInput = true;
                } catch (Exception ex) {
                    display.setText("Error");
                    currentExpression = new StringBuilder();
                }
            } else if (command.equals("AC")) {
                currentExpression = new StringBuilder();
                display.setText("");
                clearOnNextInput = false;
            } else {
                if (clearOnNextInput) {
                    currentExpression = new StringBuilder();
                    display.setText("");
                    clearOnNextInput = false;
                }
                currentExpression.append(command);
                display.setText(currentExpression.toString());
            }
        }
    }

    private class DisplayKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                try {
                    String input = display.getText().trim();
                    String result = Calculate(input);
                    display.setText(result);
                    currentExpression = new StringBuilder(result);
                    clearOnNextInput = true;
                    updateHistoryArea();
                } catch (IllegalArgumentException ex) {
                    display.setText("Error: " + ex.getMessage());
                }
            }
        }
    }

    private void updateHistoryArea() {
        List<String> history = getCalculationHistory();
        StringBuilder historyText = new StringBuilder();
        for (int i = history.size() - 1; i >= 0; i--) {
            historyText.append(history.get(i));
        }
        historyArea.setText(historyText.toString());
    }

    public static String Calculate(String infixExpression) throws IllegalArgumentException {
        String postfixExpression = infixToPostfix(infixExpression);
        double result = evaluatePostfix(postfixExpression);
        addToHistory(infixExpression, postfixExpression, result);
        return String.valueOf(result);
    }

    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    static int prec(char c) {
        if (c == '^')
            return 3;
        else if (c == '/' || c == '*')
            return 2;
        else if (c == '+' || c == '-')
            return 1;
        else
            return -1;
    }

    static char associativity(char c) {
        if (c == '^')
            return 'R';
        return 'L';
    }

    private static String infixToPostfix(String s) {
        StringBuilder result = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (isDigit(c)) {
                result.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                        result.append(' ');
                    }
                    result.append(stack.pop());
                }
                stack.pop();
            } else {
                while (!stack.isEmpty() &&
                        (prec(s.charAt(i)) < prec(stack.peek()) ||
                                prec(s.charAt(i)) == prec(stack.peek()) &&
                                        associativity(s.charAt(i)) == 'L')) {
                    if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                        result.append(' ');
                    }
                    result.append(stack.pop());
                }
                if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                    result.append(' ');
                }
                stack.push(c);
            }
        }

        while (!stack.isEmpty()) {
            if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                result.append(' ');
            }
            result.append(stack.pop());
        }

        return result.toString();
    }

    private static double evaluatePostfix(String postFixExp) {
        Stack<Double> stack = new Stack<>();

        for (int i = 0; i < postFixExp.length(); i++) {
            char c = postFixExp.charAt(i);

            if (c == ' ')
                continue;
            else if (Character.isDigit(c)) {
                double n = 0;

                while (Character.isDigit(c)) {
                    n = n * 10 + (int) (c - '0');
                    i++;
                    c = postFixExp.charAt(i);
                }
                i--;

                stack.push(n);
            } else {
                double val1 = stack.pop();
                double val2 = stack.pop();

                switch (c) {
                    case '+':
                        stack.push(val2 + val1);
                        break;
                    case '-':
                        stack.push(val2 - val1);
                        break;
                    case '/':
                        stack.push(val2 / val1);
                        break;
                    case '*':
                        stack.push(val2 * val1);
                        break;
                    case '^':
                        stack.push( Math.pow(val2, val1));
                        break;
                }
            }
        }

        return stack.pop();
    }

    public static List<String> getCalculationHistory() {
        return new ArrayList<>(calculationHistory);
    }

    public void clearHistory() {
        calculationHistory.clear();
        calcCount = 0;
    }

    public static void addToHistory(String infix, String postfix, double result) {
        calcCount++;
        String calculation = String.format(
                "Calculation #%d:\n" +
                        "Infix:   %s\n" +
                        "Postfix: %s\n" +
                        "Result:  %s\n\n",
                calcCount,
                infix, postfix, result);
        calculationHistory.add(calculation);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            rpnCalc calc = new rpnCalc();
            calc.setVisible(true);
        });
    }
}