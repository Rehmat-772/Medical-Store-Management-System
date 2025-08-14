package smstrProject;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class invoice {
    private DatabaseOperations db;
    private JPanel mainPanel;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    static List<String[]> medicines = new ArrayList<>();
    private static double Total = 0.0;

    private static final Color PRIMARY_COLOR = new Color(75, 0, 130);
    private static final Color SECONDARY_COLOR = new Color(147, 112, 219);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 255);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public invoice() {
        db = new DatabaseOperations();
        mainPanel = createInvoicePanel();
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);

        // Top search panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(BACKGROUND_COLOR);

        // Search components
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBackground(BACKGROUND_COLOR);
        JTextField searchField = createStyledTextField();
        JButton searchButton = createStyledButton("Search", new ImageIcon("assets/search.png"));

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.NORTH);

        // Search results table
        String[] searchColumns = {"ID", "Name", "Formula", "Expiry Date", "Stock", "Price"};
        DefaultTableModel searchModel = new DefaultTableModel(searchColumns, 0);
        JTable searchTable = new JTable(searchModel);
        styleSearchTable(searchTable);
        JScrollPane searchScrollPane = new JScrollPane(searchTable);
        topPanel.add(searchScrollPane, BorderLayout.CENTER);

        // Bottom cart panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        // Cart input panel
        JPanel addPanel = new JPanel(new FlowLayout());
        addPanel.setBackground(BACKGROUND_COLOR);
        JTextField idField = createStyledTextField();
        JTextField quantityField = createStyledTextField();
        JButton addButton = createStyledButton("Add to Cart",
            resizeIcon("assets/cart.png", 25, 25));

        addPanel.add(new JLabel("ID: "));
        addPanel.add(idField);
        addPanel.add(new JLabel("Quantity: "));
        addPanel.add(quantityField);
        addPanel.add(addButton);
        bottomPanel.add(addPanel, BorderLayout.NORTH);

        // Cart table
        String[] cartColumns = {"ID", "Name", "Quantity", "Price", "Total"};
        cartModel = new DefaultTableModel(cartColumns, 0);
        JTable cartTable = new JTable(cartModel);
        styleTable(cartTable);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        bottomPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Total and checkout panel
        JPanel totalPanel = new GradientPanel();
        totalLabel = new JLabel("Total: PKR 0.00");
        totalLabel.setFont(TITLE_FONT);
        totalLabel.setForeground(Color.WHITE);
        JButton generateButton = createStyledButton("Checkout",
            resizeIcon("assets/recipt.png", 32, 32));
        totalPanel.add(totalLabel);
        totalPanel.add(generateButton);
        bottomPanel.add(totalPanel, BorderLayout.SOUTH);

        // Add components to split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setResizeWeight(0.5);
        panel.add(splitPane);

        // Add action listeners
        searchButton.addActionListener(e -> {
            searchModel.setRowCount(0);
            try {
                String searchPattern = "%" + searchField.getText() + "%";
                java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(
                    "SELECT * FROM testMed WHERE name LIKE ? OR formula LIKE ?");
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                java.sql.ResultSet rs = pstmt.executeQuery();

                while(rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("formula"),
                        rs.getDate("expiry_date"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    };
                    searchModel.addRow(row);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error searching: " + ex.getMessage());
            }
        });

        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                int quantity = Integer.parseInt(quantityField.getText());

                if (db.checkQuantity(id, quantity)) {
                    double price = db.getMedicinePrice(id);
                    String name = db.getMedicineName(id);
                    double itemTotal = price * quantity;

                    Object[] row = {id, name, quantity, price, itemTotal};
                    cartModel.addRow(row);
                    medicines.add(new String[]{String.valueOf(id), name, String.valueOf(quantity), String.valueOf(price)});

                    Total += itemTotal;
                    totalLabel.setText(String.format("Total: PKR %.2f", Total));

                    db.updateQuantity(id, quantity);
                    idField.setText("");
                    quantityField.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Insufficient stock!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter valid numbers!");
            }
        });

        generateButton.addActionListener(e -> {
            if (cartModel.getRowCount() > 0) {
                new printInvoice(); // Create new printInvoice instance
                JOptionPane.showMessageDialog(panel, "Invoice generated successfully!");
            } else {
                JOptionPane.showMessageDialog(panel,
                    "Cart is empty! Please add items before checkout.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private JButton createStyledButton(String text, ImageIcon icon) {
        JButton button = new JButton(text, icon);
        button.setFont(REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add padding around text
        button.setMargin(new Insets(5, 15, 5, 15));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(SECONDARY_COLOR),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private void styleTable(JTable table) {
        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(230, 230, 250));  // Light purple background
        header.setForeground(new Color(50, 50, 50));     // Dark gray text
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));

        // Style table rows
        table.setFont(REGULAR_FONT);
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 250));
        table.setShowGrid(true);
    }

    private void styleSearchTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(230, 230, 250));  // Light purple background
        header.setForeground(new Color(50, 50, 50));     // Dark gray text
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));

        table.setFont(REGULAR_FONT);
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 250));
        table.setShowGrid(true);
    }

    private ImageIcon resizeIcon(String path, int width, int height) {
        ImageIcon imageIcon = new ImageIcon(path);
        Image image = imageIcon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), getHeight(), SECONDARY_COLOR);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class ButtonHoverEffect extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            ((JButton)e.getSource()).setBackground(SECONDARY_COLOR);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ((JButton)e.getSource()).setBackground(PRIMARY_COLOR);
        }
    }

    class RoundedBorder implements Border {
        private int radius;
        RoundedBorder(int radius) {
            this.radius = radius;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }
        public boolean isBorderOpaque() {
            return true;
        }
    }

    public static double getTotal() {
        return Total;
    }
}
