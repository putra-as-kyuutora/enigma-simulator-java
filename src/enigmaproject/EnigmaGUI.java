package enigmaproject;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * EnigmaGUI (Modernized)
 * - Preserves original behavior and logic but upgrades UI:
 *   - Modern rounded buttons with hover/press effects
 *   - Dark / Light theme toggle
 *   - Improved shortcuts using InputMap/ActionMap
 *   - Word-wrapping for output area
 *   - Modern thin scrollbar
 *   - Consistent single initialization of components
 *
 * Note: This class still depends on your existing Enigma class for enciphering.
 */
public class EnigmaGUI extends JFrame {

    private JTextField inputField;
    private JTextArea outputArea;
    private JLabel rotorPositionLabel;
    private JLabel statusLabel;
    private Enigma enigma;
    private String lastProcessedInput = "";

    // Configuration variables
    private String[] currentRotorWires;
    private char[] currentNotches;
    private String currentReflector;
    private String currentRingSettings;
    private String currentInitialPositions;
    private String[] currentPlugboardPairs;

    // Theme colors (mutable by theme toggle)
    private Color backgroundPanel;
    private Color cardColor;
    private Color accentColor;
    private Color accentDark;
    private Color textColor;
    private boolean darkMode = true;

    public EnigmaGUI() {
        initializeDefaults();
        initThemeColors();
        setupGUI();
        resetEnigma();
    }

    private void initializeDefaults() {
        currentRotorWires = new String[]{
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "BDFHJLCPRTXVZNYEIWGAKMUSQO"
        };
        currentNotches = new char[]{'Q', 'E', 'V'};
        currentReflector = "YRUHQSLDPXNGOKMIEBFZCWVJAT";
        currentRingSettings = "A A A";
        currentInitialPositions = "A A A";
        currentPlugboardPairs = new String[]{};
    }

    private void initThemeColors() {
        // Dark theme initial values
        backgroundPanel = new Color(0x0F1720); // almost-black blue
        cardColor = new Color(0x111827);
        accentColor = new Color(0x22C55E); // green accent
        accentDark = accentColor.darker();
        textColor = new Color(0x22C55E);
    }

    private void setupGUI() {
        setTitle("Enigma Machine Simulator - Modern");
        setSize(800, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Root panel with padding
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(backgroundPanel);
        add(root, BorderLayout.CENTER);

        // Top: input + rotor display + theme toggle
        JPanel topPanel = createTopPanel();
        root.add(topPanel, BorderLayout.NORTH);

        // Center: output area inside a card
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(cardColor);
        centerCard.setBorder(new CompoundBorder(
                new LineBorder(accentDark, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        outputArea = new JTextArea(14, 80);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(accentColor);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accentColor, 1),
                "OUTPUT",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                accentColor
        ));

        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        customizeScrollBar(outputScrollPane.getVerticalScrollBar());
        customizeScrollBar(outputScrollPane.getHorizontalScrollBar());
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder());

        centerCard.add(outputScrollPane, BorderLayout.CENTER);

        root.add(centerCard, BorderLayout.CENTER);

        // Bottom: controls
        JPanel bottomPanel = createBottomPanel();
        root.add(bottomPanel, BorderLayout.SOUTH);

        // Set icons/fonts for consistent look
        setUIFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Register keyboard shortcuts using InputMap/ActionMap
        registerShortcuts();

        // Center on screen
        setLocationRelativeTo(null);
    }

    // Helper to set global UI font (lightweight)
    private void setUIFont(Font f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, f);
            }
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(backgroundPanel);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        inputField.setBorder(new RoundedTitledBorder("INPUT FIELD - Ketik atau paste pesan di sini (Ctrl+A untuk encrypt all)"));
        inputField.setBackground(new Color(0x0B1220));
        inputField.setForeground(textColor);

        panel.add(inputField, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(2, 1, 6, 6));
        right.setOpaque(false);

        rotorPositionLabel = new JLabel("Rotor Position: A A A", SwingConstants.CENTER);
        rotorPositionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rotorPositionLabel.setForeground(textColor);
        rotorPositionLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
        rotorPositionLabel.setOpaque(false);

        JPanel rotorWrap = new JPanel(new BorderLayout());
        rotorWrap.setOpaque(false);
        rotorWrap.add(rotorPositionLabel, BorderLayout.CENTER);
        rotorWrap.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel topRightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        topRightRow.setOpaque(false);

        JButton themeBtn = createModernButton("Toggle Theme", "#2563EB");
        themeBtn.setPreferredSize(new Dimension(130, 34));
        themeBtn.addActionListener(e -> toggleTheme());
        topRightRow.add(themeBtn);

        right.add(rotorWrap);
        right.add(topRightRow);

        panel.add(right, BorderLayout.EAST);

        // Now set up input listeners - Document and Key via InputMap
        setupInputFieldListeners();

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundPanel);

        JButton resetButton = createModernButton("Reset Rotor", "#EF4444");
        resetButton.addActionListener(e -> resetEnigma());

        JButton configButton = createModernButton("Configuration", "#2563EB");
        configButton.addActionListener(e -> openConfigurationDialog());

        JButton clearButton = createModernButton("Clear Output", "#F59E0B");
        clearButton.addActionListener(e -> {
            outputArea.setText("");
            statusLabel.setText("Output cleared");
        });

        JButton encryptAllButton = createModernButton("Encrypt All", "#8B5CF6");
        encryptAllButton.addActionListener(e -> encryptAllInput());

        JButton saveButton = createModernButton("Save Message", "#374151");
        saveButton.addActionListener(e -> saveMessage());

        JButton loadButton = createModernButton("Load Message", "#374151");
        loadButton.addActionListener(e -> loadMessage());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.add(resetButton);
        buttonPanel.add(configButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(encryptAllButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready - Shortcuts: Ctrl+R(Reset) Ctrl+S(Save) Ctrl+L(Load) F1(Config) Ctrl+A(Encrypt All)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        statusLabel.setForeground(textColor);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        return mainPanel;
    }

    // Single place to setup DocumentListener and InputMap/ActionMap shortcuts
    private void setupInputFieldListeners() {
        if (inputField == null) return; // defensive

        // Document listener for real-time plus paste
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> processNewInput());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> handleTextRemoval());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        // Use InputMap/ActionMap for cross-platform reliable shortcuts
        InputMap im = inputField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = inputField.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "reset");
        am.put("reset", new AbstractAction() { public void actionPerformed(ActionEvent e) { resetEnigma(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        am.put("save", new AbstractAction() { public void actionPerformed(ActionEvent e) { saveMessage(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "load");
        am.put("load", new AbstractAction() { public void actionPerformed(ActionEvent e) { loadMessage(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "config");
        am.put("config", new AbstractAction() { public void actionPerformed(ActionEvent e) { openConfigurationDialog(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "encryptAll");
        am.put("encryptAll", new AbstractAction() { public void actionPerformed(ActionEvent e) { encryptAllInput(); } });
    }

    private void processNewInput() {
        String currentInput = inputField.getText();

        if (currentInput.length() > lastProcessedInput.length()
                && currentInput.startsWith(lastProcessedInput)) {

            String newText = currentInput.substring(lastProcessedInput.length());

            StringBuilder sb = new StringBuilder();
            for (char ch : newText.toCharArray()) {
                if (Character.isLetter(ch)) {
                    char upperCh = Character.toUpperCase(ch);
                    String encrypted = enigma.encipher(String.valueOf(upperCh));
                    sb.append(encrypted);
                } else if (ch == ' ') {
                    sb.append(' ');
                } else if (Character.isDigit(ch)) {
                    sb.append(ch);
                }
            }

            outputArea.append(sb.toString());
            updateRotorDisplay();
            lastProcessedInput = currentInput;
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            statusLabel.setText("Processed " + newText.length() + " characters");
        } else if (!currentInput.startsWith(lastProcessedInput)) {
            statusLabel.setText("Input changed - Use Ctrl+A to encrypt all, or Reset first");
        }
    }

    private void handleTextRemoval() {
        String currentInput = inputField.getText();

        if (currentInput.length() < lastProcessedInput.length()) {
            statusLabel.setText("Text removed - Note: Rotor positions cannot be reversed (Authentic Mode)");
        }

        if (currentInput.length() <= lastProcessedInput.length()) {
            if (lastProcessedInput.startsWith(currentInput)) {
                lastProcessedInput = currentInput;
            }
        }
    }

    private void encryptAllInput() {
        String input = inputField.getText();
        if (input.trim().isEmpty()) {
            statusLabel.setText("No text to encrypt");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Reset rotor positions before encrypting?\n"
                        + "(Recommended for clean encryption)",
                "Encrypt All Input",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.CANCEL_OPTION) {
            return;
        }

        if (choice == JOptionPane.YES_OPTION) {
            resetEnigma();
        }

        StringBuilder out = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isLetter(ch)) {
                char upperCh = Character.toUpperCase(ch);
                String encrypted = enigma.encipher(String.valueOf(upperCh));
                out.append(encrypted);
            } else if (ch == ' ') {
                out.append(' ');
            } else if (Character.isDigit(ch)) {
                out.append(ch);
            }
        }

        outputArea.setText(out.toString());

        lastProcessedInput = input;
        updateRotorDisplay();
        statusLabel.setText("Encrypted " + input.length() + " characters");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private JPanel createBottomPanelLegacy() {
        // kept for reference if needed
        return null;
    }

    private void resetEnigma() {
        enigma = new Enigma(
                currentRotorWires.clone(),
                currentNotches.clone(),
                currentReflector,
                currentRingSettings,
                currentInitialPositions,
                currentPlugboardPairs.clone()
        );

        lastProcessedInput = "";
        inputField.setText("");
        outputArea.append("\n=== MESIN DI-RESET KE POSISI AWAL ===\n");
        updateRotorDisplay();
        statusLabel.setText("Enigma machine reset to initial position");
        inputField.requestFocus();
    }

    private void updateRotorDisplay() {
        try {
            String positions = enigma.getCurrentRotorPositions();
            rotorPositionLabel.setText("Rotor Position: " + positions);
        } catch (Exception e) {
            rotorPositionLabel.setText("Rotor Position: [Position tracking unavailable]");
        }
    }

    private void openConfigurationDialog() {
        JDialog configDialog = new JDialog(this, "Enigma Configuration", true);
        configDialog.setSize(480, 380);
        configDialog.setLocationRelativeTo(this);
        configDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        contentPanel.setBackground(cardColor);

        contentPanel.add(new JLabel("Ring Settings (A-Z):"));
        JTextField ringField = new JTextField(currentRingSettings);
        contentPanel.add(ringField);

        contentPanel.add(new JLabel("Initial Positions:"));
        JTextField posField = new JTextField(currentInitialPositions);
        contentPanel.add(posField);

        contentPanel.add(new JLabel("Plugboard Pairs:"));
        JTextField plugField = new JTextField(String.join(" ", currentPlugboardPairs));
        contentPanel.add(plugField);

        contentPanel.add(new JLabel("Rotor Model:"));
        JComboBox<String> rotorCombo = new JComboBox<>(new String[]{"Enigma I (Standard)", "Enigma M3", "Custom"});
        contentPanel.add(rotorCombo);

        contentPanel.add(new JLabel("Help:"));
        JLabel helpLabel = new JLabel("<html><font size='2'>Ring: A A A, Pos: A A A<br/>Plugboard: AT BS DE (pairs)</font></html>");
        contentPanel.add(helpLabel);

        configDialog.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton okButton = createModernButton("Apply", "#10B981");
        JButton cancelButton = createModernButton("Cancel", "#9CA3AF");

        okButton.setPreferredSize(new Dimension(90, 34));
        cancelButton.setPreferredSize(new Dimension(90, 34));

        okButton.addActionListener(e -> {
            try {
                String ringText = ringField.getText().trim();
                if (!isValidTripleFormat(ringText)) {
                    throw new IllegalArgumentException("Ring settings must be in format 'A A A'");
                }

                String posText = posField.getText().trim();
                if (!isValidTripleFormat(posText)) {
                    throw new IllegalArgumentException("Initial positions must be in format 'A A A'");
                }

                currentRingSettings = ringText;
                currentInitialPositions = posText;

                String plugText = plugField.getText().trim();
                if (plugText.isEmpty()) {
                    currentPlugboardPairs = new String[]{};
                } else {
                    String[] pairs = plugText.split("\\s+");
                    for (String pair : pairs) {
                        if (pair.length() != 2 || !pair.matches("[A-Za-z]{2}")) {
                            throw new IllegalArgumentException("Plugboard pairs must be 2 letters each (e.g., AT BS DE)");
                        }
                    }
                    currentPlugboardPairs = pairs;
                }

                resetEnigma();
                statusLabel.setText("Configuration updated successfully");
                configDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(configDialog,
                        "Error in configuration: " + ex.getMessage(),
                        "Configuration Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> configDialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        configDialog.add(buttonPanel, BorderLayout.SOUTH);

        configDialog.setVisible(true);
    }

    private boolean isValidTripleFormat(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 3) return false;
        for (String part : parts) {
            if (part.length() != 1 || !part.matches("[A-Za-z]")) return false;
        }
        return true;
    }

    private void saveMessage() {
        if (outputArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No message to save!", "Nothing to Save", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Encrypted Message");
        fileChooser.setSelectedFile(new java.io.File("enigma_message.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.println("=== ENIGMA ENCRYPTED MESSAGE ===");
                writer.println("Date: " + java.time.LocalDateTime.now().toString());
                writer.println("Input: " + inputField.getText());
                writer.println("Output: " + outputArea.getText().replace("\n", " ").trim());
                writer.println("Ring Settings: " + currentRingSettings);
                writer.println("Initial Positions: " + currentInitialPositions);
                writer.println("Plugboard Pairs: " + String.join(" ", currentPlugboardPairs));
                writer.println("=== END MESSAGE ===");
                statusLabel.setText("Message saved to: " + fileChooser.getSelectedFile().getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Save failed");
            }
        }
    }

    private void loadMessage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Message File");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(fileChooser.getSelectedFile().toPath()));
                outputArea.append("\n=== LOADED MESSAGE ===\n");
                outputArea.append(content);
                statusLabel.setText("Message loaded successfully");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ------------------ UI Helpers ------------------
    private JButton createModernButton(String text, String hexColor) {
        Color base = Color.decode(hexColor);
        Color hover = lighten(base, 0.12f);
        Color pressed = lighten(base, -0.12f);

        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(accentColor); 
        btn.setBackground(base);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        // Rounded look via setBorder and override paintComponent
        btn.setBorder(new RoundedBorder(12));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(base); }
            @Override
            public void mousePressed(MouseEvent e) { btn.setBackground(pressed); }
            @Override
            public void mouseReleased(MouseEvent e) { btn.setBackground(base); }
        });

        return btn;
    }

    private static class RoundedBorder implements Border {
        private final int radius;
        public RoundedBorder(int radius) { this.radius = radius; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius, radius/2, radius); }
        @Override public boolean isBorderOpaque() { return false; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,30));
            g2.fillRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }

    // Rounded titled border for input field
    private static class RoundedTitledBorder extends LineBorder {
        private final String title;
        public RoundedTitledBorder(String title) {
            super(new Color(0,0,0,0), 0, true);
            this.title = title;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,10));
            g2.fillRoundRect(x, y, width-1, height-1, 10, 10);
            g2.setColor(new Color(255,255,255,20));
            g2.drawRoundRect(x, y, width-1, height-1, 10, 10);
            // draw title text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(200,200,200));
            g2.drawString(title, x + 10, y + 16);
            g2.dispose();
        }
    }

    private Color lighten(Color c, float fraction) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        if (fraction < 0) {
            fraction = -fraction;
            r = (int) Math.max(0, r - 255 * fraction);
            g = (int) Math.max(0, g - 255 * fraction);
            b = (int) Math.max(0, b - 255 * fraction);
        } else {
            r = (int) Math.min(255, r + 255 * fraction);
            g = (int) Math.min(255, g + 255 * fraction);
            b = (int) Math.min(255, b + 255 * fraction);
        }
        return new Color(r, g, b);
    }

    private void customizeScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            private final Dimension d = new Dimension();
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(120, 120, 120, 180);
                this.thumbDarkShadowColor = new Color(100,100,100);
                this.thumbHighlightColor = new Color(150,150,150);
                this.trackColor = new Color(0,0,0,30);
            }
            @Override public Dimension getPreferredSize(JComponent c) { return new Dimension(10,10); }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) { }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                g2.dispose();
            }
        });
        bar.setUnitIncrement(16);
    }
    
    private void styleButton(JButton button) {
    button.setBackground(new Color(0x2C2C2C)); // abu-abu gelap
    button.setForeground(Color.WHITE);
    button.setOpaque(true);
    button.setBorderPainted(false);
}


    private void registerShortcuts() {
        // Additional global shortcuts if needed (root pane)
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "reset");
        am.put("reset", new AbstractAction() { public void actionPerformed(ActionEvent e) { resetEnigma(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        am.put("save", new AbstractAction() { public void actionPerformed(ActionEvent e) { saveMessage(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "load");
        am.put("load", new AbstractAction() { public void actionPerformed(ActionEvent e) { loadMessage(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "config");
        am.put("config", new AbstractAction() { public void actionPerformed(ActionEvent e) { openConfigurationDialog(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "encryptAll");
        am.put("encryptAll", new AbstractAction() { public void actionPerformed(ActionEvent e) { encryptAllInput(); } });
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            backgroundPanel = new Color(0x0F1720);
            cardColor = new Color(0x111827);
            accentColor = new Color(0x22C55E);
            accentDark = accentColor.darker();
            textColor = new Color(0xE5E7EB);
            outputArea.setBackground(Color.BLACK);
            outputArea.setForeground(accentColor);
        } else {
            backgroundPanel = new Color(0xF3F4F6);
            cardColor = new Color(0xFFFFFF);
            accentColor = new Color(0x2563EB);
            accentDark = accentColor.darker();
            textColor = new Color(0x111827);
            outputArea.setBackground(new Color(0xF8FAFF));
            outputArea.setForeground(textColor);
        }
        // apply to main components
        getContentPane().setBackground(backgroundPanel);
        // Walk components to apply background/text where appropriate
        SwingUtilities.invokeLater(() -> updateUIThemeRecursively(this.getContentPane()));
    }

    private void updateUIThemeRecursively(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(cardColor.equals(new Color(0xFFFFFF)) ? cardColor : cardColor);
            }
            if (comp instanceof JLabel) ((JLabel) comp).setForeground(textColor);
            if (comp instanceof JButton) ((JButton) comp).setForeground(Color.WHITE);
            if (comp instanceof Container) updateUIThemeRecursively((Container) comp);
        }
        repaint();
    }

    // ------------------ Main ------------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* ignore */ }

        SwingUtilities.invokeLater(() -> new EnigmaGUI().setVisible(true));
    }
}
