package smstrProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.sql.*;

public class Main {
    private static DatabaseOperations db;
    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;

    private static final Color PRIMARY_COLOR = new Color(75, 0, 130);
    private static final Color SECONDARY_COLOR = new Color(147, 112, 219);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 255);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            db = new DatabaseOperations();
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        mainFrame = new JFrame("Trio Chemist Pharmacy");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 700);
        mainFrame.setLayout(new BorderLayout(10, 10));

        // Sidebar
        JPanel sidebar = createSidebar();
        mainFrame.add(sidebar, BorderLayout.WEST);

        // Main content
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);

        mainPanel.add(createDashboardPanel(), "DASHBOARD");
        mainPanel.add(createUpdatePanel(), "UPDATE");
        mainPanel.add(createCheckStatusPanel(), "STATUS");
        mainPanel.add(new invoice().getPanel(), "INVOICE");
        mainPanel.add(createAddMedicinePanel(), "ADD_MEDICINE");

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static JPanel createSidebar() {
        JPanel sidebar = new GradientPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel titleLabel = new JLabel("Pharmacy System");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] buttons = {"Dashboard", "Add Medicine", "Update Medicine", "Check Status", "Generate Invoice"};
        for (String text : buttons) {
            JButton button = createNavButton(text);
            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return sidebar;
    }

    private static JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false); // Remove border
        button.setContentAreaFilled(false); // Remove default button fill
        button.setOpaque(true); // Make button background visible
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 45));

        // Update action listener to handle panel switching
        button.addActionListener(e -> {
            String panelName = text.toUpperCase().replace(" ", "_");
            switch(panelName) {
                case "DASHBOARD":
                    cardLayout.show(mainPanel, "DASHBOARD");
                    break;
                case "ADD_MEDICINE":
                    cardLayout.show(mainPanel, "ADD_MEDICINE");
                    break;
                case "UPDATE_MEDICINE":
                    cardLayout.show(mainPanel, "UPDATE");
                    break;
                case "CHECK_STATUS":
                    cardLayout.show(mainPanel, "STATUS");
                    break;
                case "GENERATE_INVOICE":
                    cardLayout.show(mainPanel, "INVOICE");
                    break;
            }
        });

        button.addMouseListener(new ButtonHoverEffect());

        return button;
    }

    private static JPanel createDashboardPanel() {
        DashboardStats dashboardStats = new DashboardStats(db);
        return dashboardStats.createDashboard();
    }

    private static JPanel createCheckStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = createStyledTextField();
        JButton searchButton = createStyledButton("Search");
        JButton showAllButton = createStyledButton("Show All");
        JButton expiredButton = createStyledButton("Show Expired");
        JButton lowStockButton = createStyledButton("Low Stock");

        controlPanel.add(new JLabel("Search: "));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(showAllButton);
        controlPanel.add(expiredButton);
        controlPanel.add(lowStockButton);

        // Table setup
        String[] columns = {"ID", "Name", "Formula", "Expiry Date", "Quantity", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable medicineTable = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 6) { // Status column
                    String status = (String) getValueAt(row, column);
                    if (status.contains("Expired")) {
                        c.setForeground(Color.RED);
                    } else if (status.contains("Low")) {
                        c.setForeground(Color.ORANGE);
                    } else {
                        c.setForeground(new Color(0, 150, 0));
                    }
                }
                return c;
            }
        };

        JScrollPane scrollPane = new JScrollPane(medicineTable);
        medicineTable.setFillsViewportHeight(true);

        // Action Listeners
        searchButton.addActionListener(e -> {
            model.setRowCount(0); // Clear existing rows
            try {
                String searchPattern = "%" + searchField.getText() + "%";
                PreparedStatement pstmt = db.getConnection().prepareStatement(
                    "SELECT * FROM testMed WHERE name LIKE ? OR formula LIKE ?");
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                ResultSet rs = pstmt.executeQuery();

                boolean found = false;
                while(rs.next()) {
                    found = true;
                    java.sql.Date expiryDate = rs.getDate("expiry_date");
                    int quantity = rs.getInt("quantity");
                    String status = "In Stock";

                    if (expiryDate != null && expiryDate.before(new java.sql.Date(System.currentTimeMillis()))) {
                        status = "Expired";
                    } else if (quantity < 10) {
                        status = "Low Stock";
                    }

                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("formula"),
                        expiryDate,
                        quantity,
                        rs.getDouble("price"),
                        status
                    };
                    model.addRow(row);
                }

                if (!found) {
                    JOptionPane.showMessageDialog(mainFrame,
                        "No medicines found matching: " + searchField.getText(),
                        "Search Result",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Error searching medicines: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        showAllButton.addActionListener(e -> {
            model.setRowCount(0);
            db.SeeAllMedicines(model);
        });

        expiredButton.addActionListener(e -> {
            model.setRowCount(0);
            db.getExpiredMedicines(model);
        });

        lowStockButton.addActionListener(e -> {
            model.setRowCount(0);
            db.checkLowStock(model);
        });

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JLabel idLabel = new JLabel("Medicine ID:");
        idLabel.setFont(REGULAR_FONT);
        JTextField idField = createStyledTextField();
        JButton searchButton = createSearchButton();

        searchPanel.add(idLabel);
        searchPanel.add(idField);
        searchPanel.add(searchButton);

        // Update Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Update Medicine Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            REGULAR_FONT,
            PRIMARY_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input fields
        JTextField priceField = createStyledTextField();
        JTextField stockField = createStyledTextField();
        JTextField expiryField = createStyledTextField();

        // Initially disable fields
        priceField.setEnabled(false);
        stockField.setEnabled(false);
        expiryField.setEnabled(false);

        // Add components with labels
        addFormRow(formPanel, "New Price:", priceField, gbc, 0);
        addFormRow(formPanel, "New Stock:", stockField, gbc, 1);
        addFormRow(formPanel, "New Expiry:", expiryField, gbc, 2);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton updateButton = createStyledButton("Update");
        JButton deleteButton = createStyledButton("Delete");
        JButton clearButton = createStyledButton("Clear");

        // Style delete button differently
        deleteButton.setBackground(new Color(220, 53, 69));

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Action Listeners
        searchButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                if (db.getMedicineName(id) != null) {
                    priceField.setEnabled(true);
                    stockField.setEnabled(true);
                    expiryField.setEnabled(true);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                    "Please enter a valid ID",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        updateButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                if (!priceField.getText().isEmpty()) {
                    db.updatePrice(id, Double.parseDouble(priceField.getText()));
                }
                if (!stockField.getText().isEmpty()) {
                    db.updateQuan(id, Integer.parseInt(stockField.getText()));
                }
                if (!expiryField.getText().isEmpty()) {
                    db.updateExpiry(id, expiryField.getText());
                }
                clearFields(idField, priceField, stockField, expiryField);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                    "Please enter valid numbers",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to delete this medicine?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    db.deleteMedicine(id);
                    clearFields(idField, priceField, stockField, expiryField);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                    "Please enter a valid ID",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        clearButton.addActionListener(e ->
            clearFields(idField, priceField, stockField, expiryField));

        // Layout assembly
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);
        mainContent.add(searchPanel, BorderLayout.NORTH);
        mainContent.add(formPanel, BorderLayout.CENTER);
        mainContent.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContent, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createAddMedicinePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Add New Medicine",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            TITLE_FONT,
            PRIMARY_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create input fields
        JTextField idField = createStyledTextField();
        JTextField nameField = createStyledTextField();
        JTextField formulaField = createStyledTextField();
        JTextField expiryField = createStyledTextField();
        JTextField quantityField = createStyledTextField();
        JTextField priceField = createStyledTextField();

        // Add form components
        addFormRow(formPanel, "Medicine ID:", idField, gbc, 0);
        addFormRow(formPanel, "Name:", nameField, gbc, 1);
        addFormRow(formPanel, "Formula:", formulaField, gbc, 2);
        addFormRow(formPanel, "Expiry Date (YYYY-MM-DD):", expiryField, gbc, 3);
        addFormRow(formPanel, "Quantity:", quantityField, gbc, 4);
        addFormRow(formPanel, "Price:", priceField, gbc, 5);

        // Add Button
        JButton addButton = createStyledButton("Add Medicine");
        JButton clearButton = createStyledButton("Clear");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        // Add action listeners
        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                String formula = formulaField.getText();
                String expiry = expiryField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());

                if (name.isEmpty() || formula.isEmpty() || expiry.isEmpty()) {
                    JOptionPane.showMessageDialog(panel,
                        "Please fill all fields",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                db.insertMedicine(id, name, formula, expiry, quantity, price);

                // Show success message
                JOptionPane.showMessageDialog(panel,
                    "Medicine added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                // Clear fields and reset form
                clearFields(idField, nameField, formulaField, expiryField, quantityField, priceField);
                idField.requestFocus();

                // Reset connection to ensure fresh state
                // db.refreshConnection(); // Method not defined, so this line is removed

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                    "Please enter valid numbers for ID, Quantity and Price",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        clearButton.addActionListener(e ->
            clearFields(idField, nameField, formulaField, expiryField, quantityField, priceField));

        // Layout assembly
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);
        mainContent.add(formPanel, BorderLayout.CENTER);
        mainContent.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContent, BorderLayout.CENTER);
        return panel;
    }

    private static JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(SECONDARY_COLOR),
            new EmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new ButtonHoverEffect());

        // Add padding around text
        button.setMargin(new Insets(5, 15, 5, 15));

        return button;
    }

    private static void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
            field.setEnabled(false);
        }
    }

    private static JButton createSearchButton() {
        JButton searchButton = new JButton(" Search");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(true);
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(120, 40));
        searchButton.setMargin(new Insets(5, 15, 5, 15));

        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                searchButton.setBackground(SECONDARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                searchButton.setBackground(PRIMARY_COLOR);
            }
        });

        return searchButton;
    }

    private static JButton createIconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new ButtonHoverEffect());
        return button;
    }

    private static void addFormRow(JPanel panel, String label, JComponent field,
                                 GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(field, gbc);
    }

    // Helper classes
    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, SECONDARY_COLOR);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    static class ButtonHoverEffect extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            JButton button = (JButton)e.getSource();
            button.setBackground(SECONDARY_COLOR);
            button.setForeground(Color.WHITE); // Maintain text visibility on hover
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JButton button = (JButton)e.getSource();
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE); // Maintain text visibility
        }
    }

    static class RoundedBorder implements Border {
        private int radius;
        RoundedBorder(int radius) { this.radius = this.radius; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
