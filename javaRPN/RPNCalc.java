package javaRPN;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A Reverse Polish Notation (RPN) calculator with a graphical user interface.
 * This calculator supports basic arithmetic operations and maintains a calculation history.
 */
public class RPNCalc extends JFrame {
	/** initialize an array list called calculationHistory*/
    private static List<String> calculationHistory = new ArrayList<>();
    /** Keeps track of the number of calculations performed. */
    private static int calcCount = 0;
    
    private JTextField display;
    private JTextArea historyArea;
    private StringBuilder currentExpression;
    private boolean clearOnNextInput;

    /**
     * Constructs a new RPNCalc instance and initializes the GUI.
     */
    public RPNCalc() {
        setTitle("RPN Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout());

        currentExpression = new StringBuilder();

        initializeComponents();
    }

    /**
     * Initializes and arranges the GUI components.
     */
    private void initializeComponents() {
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

    /**
     * Creates and returns the panel containing calculator buttons.
     *
     * @return JPanel containing calculator buttons
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between buttons

        String[] buttonLabels = {
        		"empty" ,"empty" ,"DEL",
                "AC", 
                 "(",  ")", "/", "empty" ,
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "._.", "0", ".", "="
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            if (!label.isEmpty()) {
                JButton button = new JButton(label);
                button.setFont(new Font("Arial", Font.PLAIN, 16));
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.addActionListener(new ButtonClickListener());
                gbc.gridx = i % 4;
                gbc.gridy = i / 4;
                button.setBackground(new Color(125, 125, 255)); // Bright red
                button.setForeground(Color.WHITE);
//                button.setBackground(Color.blue);
//                button.setForeground(Color.white);

//                button.setBackground(Color.blue);
                if ("AC".equals(label)) {
//                    button.setBackground(Color.red);
//                    button.setForeground(Color.white);
                	 button.setBackground(new Color(255, 87, 51)); // Bright red
                     button.setForeground(Color.WHITE);
                } else if (label.equals("DEL")) {
                    button.setBackground(new Color(0, 128, 0)); // Dark green
                    button.setForeground(Color.WHITE);
                } else if (label.equals("empty")) {
                	button.setOpaque(false);
                	button.setText("");

                }
                buttonPanel.add(button, gbc);
            }
        }

        return buttonPanel;
    }

    /**
     * ActionListener for calculator buttons.
     */
    private class ButtonClickListener implements ActionListener {
        /**
         * Handles button click events.
         *
         * @param e the ActionEvent object
         */
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
            } else if (command.equals("DEL") && currentExpression.length() > 0) {
                    currentExpression.deleteCharAt(currentExpression.length() - 1);
                    display.setText(currentExpression.toString());
                
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

    /**
     * KeyListener for the display text field.
     */
    private class DisplayKeyListener extends KeyAdapter {
        /**
         * Handles key press events in the display text field.
         *
         * @param e the KeyEvent object
         */
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

    /**
     * Updates the history area with the latest calculations.
     */
    private void updateHistoryArea() {
        List<String> history = getCalculationHistory();
        StringBuilder historyText = new StringBuilder();
        for (int i = history.size() - 1; i >= 0; i--) {
            historyText.append(history.get(i));
        }
        historyArea.setText(historyText.toString());
    }

    /**
     * Calculates the result of an infix expression.
     *
     * @param infixExpression the infix expression to calculate
     * @return the result of the calculation as a String
     * @throws IllegalArgumentException if the expression is invalid
     */
    public static String Calculate(String infixExpression) throws IllegalArgumentException {
        String postfixExpression = infixToPostfix(infixExpression);
        double result = evaluatePostfix(postfixExpression);
        addToHistory(infixExpression, postfixExpression, result);
        return String.valueOf(result);
    }

    /**
     * Checks if a character is a digit.
     *
     * @param c the character to check
     * @return true if the character is a digit, false otherwise
     */
    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Returns the precedence of an operator.
     *
     * @param c the operator character
     * @return the precedence value of the operator
     */
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

    /**
     * Returns the associativity of an operator.
     *
     * @param c the operator character
     * @return 'L' for left associative, 'R' for right associative
     */
    static char associativity(char c) {
        if (c == '^')
            return 'R';
        return 'L';
    }

    /**
     * Converts an infix expression to postfix notation.
     *
     * @param s the infix expression
     * @return the postfix expression
     */
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

    /**
     * Evaluates a postfix expression.
     *
     * @param postFixExp the postfix expression to evaluate
     * @return the result of the evaluation
     */
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
                        stack.push(Math.pow(val2, val1));
                        break;
                }
            }
        }

        return stack.pop();
    }

    /**
     * Returns the calculation history.
     *
     * @return a List of Strings representing the calculation history
     */
    public static List<String> getCalculationHistory() {
        return new ArrayList<>(calculationHistory);
    }

    /**
     * Clears the calculation history.
     */
    public void clearHistory() {
        calculationHistory.clear();
        calcCount = 0;
    }

    /**
     * Adds a calculation to the history.
     *
     * @param infix the infix expression
     * @param postfix the postfix expression
     * @param result the result of the calculation
     */
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

    /**
     * The main method to run the RPN Calculator application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RPNCalc calc = new RPNCalc();
            calc.setVisible(true);
        });
    }
}