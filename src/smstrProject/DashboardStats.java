package smstrProject;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.border.AbstractBorder;
import java.awt.geom.RoundRectangle2D;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DashboardStats {
    private DatabaseOperations db;
    private JPanel statsPanel;
    private static final Color PRIMARY_COLOR = new Color(75, 0, 130);
    private static final Color SECONDARY_COLOR = new Color(147, 112, 219);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 255);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font ICON_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 32);

    public DashboardStats(DatabaseOperations db) {
        this.db = db;
    }
    
    // Define the ButtonHoverEffect class
    static class ButtonHoverEffect extends java.awt.event.MouseAdapter {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            e.getComponent().setBackground(new Color(95, 0, 150));
        }
    
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            e.getComponent().setBackground(DashboardStats.PRIMARY_COLOR);
        }
    }

    // Add a custom panel for a gradient background
    private static class DashboardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            // Gradient from a darker blue to BACKGROUND_COLOR for a nicer look
            GradientPaint gp = new GradientPaint(0, 0, new Color(70,130,180), w, h, BACKGROUND_COLOR);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    public JPanel createDashboard() {
        // Replace default panel with custom DashboardPanel for improved graphics
        JPanel dashboard = new DashboardPanel(); // modified line
        dashboard.setLayout(new BorderLayout(10, 10));  // Reduced spacing
        dashboard.setBackground(BACKGROUND_COLOR);
        dashboard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));  // Reduced padding

        // Smaller grid spacing
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(BACKGROUND_COLOR);
        
        // Set preferred size for statsPanel to control overall size
        statsPanel.setPreferredSize(new Dimension(400, 300));  // Reduced dimensions

        // Add stat cards with same order but smaller size
        statsPanel.add(createStatCard("Total Medicines", getTotalMedicines(), "🏥"));
        statsPanel.add(createStatCard("Low Stock", getLowStockCount(), "⚠️"));
        statsPanel.add(createStatCard("Expired", getExpiredCount(), "⏰"));
        statsPanel.add(createStatCard("Total Value", "₨ " + getTotalValue(), "💰"));

        dashboard.add(statsPanel, BorderLayout.CENTER);
        return dashboard;
    }

    private JPanel createStatCard(String title, String value, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
            }
        };
        
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(ICON_FONT);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(REGULAR_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(TITLE_FONT);
        valueLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private String getTotalMedicines() {
        try {
            PreparedStatement pstmt = db.getConnection().prepareStatement(
                "SELECT COUNT(*) as total FROM testMed");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("total") : "0";
        } catch (SQLException e) {
            return "0";
        }
    }

    private String getLowStockCount() {
        try {
            PreparedStatement pstmt = db.getConnection().prepareStatement(
                "SELECT COUNT(*) as low FROM testMed WHERE quantity < 10");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("low") : "0";
        } catch (SQLException e) {
            return "0";
        }
    }

    private String getExpiredCount() {
        try {
            PreparedStatement pstmt = db.getConnection().prepareStatement(
                "SELECT COUNT(*) as expired FROM testMed WHERE expiry_date < CURDATE()");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("expired") : "0";
        } catch (SQLException e) {
            return "0";
        }
    }

    private String getTotalValue() {
        try {
            PreparedStatement pstmt = db.getConnection().prepareStatement(
                "SELECT SUM(quantity * price) as total FROM testMed");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? String.format("%.2f", rs.getDouble("total")) : "0.00";
        } catch (SQLException e) {
            return "0.00";
        }
    }

    private static JPanel createCheckStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Search Section with modern design
        JPanel searchSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchSection.setBackground(BACKGROUND_COLOR);
        searchSection.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    
        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search medicines by name or formula...");
    
        // Control buttons with icons
        JButton searchButton = createIconButton("🔍 Search");
        JButton showAllButton = createIconButton("📋 All Medicines");
        JButton expiredButton = createIconButton("⚠️ Expired");
        JButton lowStockButton = createIconButton("📉 Low Stock");
    
        searchSection.add(searchField);
        searchSection.add(searchButton);
        searchSection.add(showAllButton);
        searchSection.add(expiredButton);
        searchSection.add(lowStockButton);
    
        // Table with enhanced styling
        String[] columns = {"ID", "Name", "Formula", "Expiry Date", "Quantity", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable medicineTable = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                return c;
            }
        };
    
        // Style table
        medicineTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        medicineTable.setRowHeight(35);
        medicineTable.setShowGrid(true);
        medicineTable.setGridColor(new Color(230, 230, 250));
        medicineTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        medicineTable.getTableHeader().setBackground(PRIMARY_COLOR);
        medicineTable.getTableHeader().setForeground(Color.WHITE);
        medicineTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
    
        // Add table to scroll pane with styling
        JScrollPane scrollPane = new JScrollPane(medicineTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
    
        // Add components to panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.add(searchSection, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    
        panel.add(contentPanel, BorderLayout.CENTER);
    
        // Keep existing action listeners
        // ... [Previous action listener code remains the same]
    
        return panel;
    }
    
    // Helper method for creating icon buttons
    private static JButton createIconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));
        button.addMouseListener(new ButtonHoverEffect());
        return button;
    }
}

// Move RoundedBorder to its own file or make it static inner class
class RoundedBorder extends AbstractBorder {
    private int radius;

    RoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
        g2d.draw(round);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = radius + 1;
        return insets;
    }
}