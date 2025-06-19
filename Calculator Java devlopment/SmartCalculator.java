import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class SmartCalculator extends JFrame implements ActionListener, KeyListener {

    private JTextField display;
    private boolean isScientific = false;
    private JPanel buttonPanel, sciPanel;

    public SmartCalculator() {
        setTitle("Smart Calculator");
        setSize(400, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(30, 30, 30)); // Dark theme

        display = new JTextField();
        display.setBounds(20, 20, 340, 40);
        display.setFont(new Font("Arial", Font.BOLD, 20));
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(Color.GREEN);
        add(display);

        createStandardButtons();
        createScientificButtons();

        // Toggle Sci Mode
        JButton sciToggle = new JButton("Sci Mode");
        sciToggle.setBounds(270, 470, 100, 30);
        sciToggle.addActionListener(e -> toggleScientificMode());
        add(sciToggle);

        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }

    private void createStandardButtons() {
        buttonPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        buttonPanel.setBounds(20, 80, 350, 200);
        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            styleButton(btn);
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }
        add(buttonPanel);

        JButton clear = new JButton("C");
        clear.setBounds(20, 300, 350, 40);
        styleButton(clear);
        clear.addActionListener(e -> display.setText(""));
        add(clear);
    }

    private void createScientificButtons() {
        sciPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        sciPanel.setBounds(20, 360, 350, 90);
        sciPanel.setVisible(false);

        String[] sciButtons = { "sin", "cos", "tan", "log", "sqrt", "^", "(", ")" };

        for (String text : sciButtons) {
            JButton btn = new JButton(text);
            styleButton(btn);
            btn.addActionListener(this);
            sciPanel.add(btn);
        }

        add(sciPanel);
    }

    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
    }

    private void toggleScientificMode() {
        isScientific = !isScientific;
        sciPanel.setVisible(isScientific);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String input = e.getActionCommand();
        switch (input) {
            case "=":
                try {
                    String expr = display.getText();
                    double result = evaluate(expr);
                    display.setText(Double.toString(result));
                } catch (Exception ex) {
                    display.setText("Error");
                }
                break;
            case "sin":
                applyTrig(Math::sin);
                break;
            case "cos":
                applyTrig(Math::cos);
                break;
            case "tan":
                applyTrig(Math::tan);
                break;
            case "log":
                applyTrig(Math::log10);
                break;
            case "sqrt":
                applyTrig(Math::sqrt);
                break;
            default:
                display.setText(display.getText() + input);
        }
    }

    private void applyTrig(java.util.function.DoubleUnaryOperator func) {
        try {
            double val = Double.parseDouble(display.getText());
            display.setText(String.valueOf(func.applyAsDouble(Math.toRadians(val))));
        } catch (Exception e) {
            display.setText("Error");
        }
    }

    // Simple expression evaluator (supports +, -, *, /, ^, (), decimals)
    private double evaluate(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() { ch = (++pos < expr.length()) ? expr.charAt(pos) : -1; }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }
        }.parse();
    }

    // Keyboard support
    public void keyPressed(KeyEvent e) {
        char ch = e.getKeyChar();
        if (Character.isDigit(ch) || "+-*/.^()".indexOf(ch) >= 0) {
            display.setText(display.getText() + ch);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                display.setText(Double.toString(evaluate(display.getText())));
            } catch (Exception ex) {
                display.setText("Error");
            }
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            String current = display.getText();
            if (!current.isEmpty()) {
                display.setText(current.substring(0, current.length() - 1));
            }
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartCalculator::new);
    }
}
