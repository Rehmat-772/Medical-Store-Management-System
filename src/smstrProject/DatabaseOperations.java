package smstrProject;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;
import java.text.ParseException;

public class DatabaseOperations {
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "d238d2ff";
    private Connection connection;

    public DatabaseOperations() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected......");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
     public void insertMedicine(int id, String name, String formula, String expiryDate, 
                             int quantity, double price) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO testMed (id, name, formula, expiry_date, quantity, price) VALUES (?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, formula);
            pstmt.setDate(4, DateUtil.convertStringToSqlDate(expiryDate));
            pstmt.setInt(5, quantity);
            pstmt.setDouble(6, price);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null,
                "Medicine added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(null,
                "Error adding medicine: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getMedicineName(int id) {
        String query = "SELECT name FROM testMed WHERE id = ?";
        String name = null;
    
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
    
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("name");
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "No medicine found with ID: " + id,
                        "Medicine Not Found",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error retrieving medicine: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return name;
    }

    public void getExpiredMedicines() {
        
        JFrame frame = new JFrame("Expired Medicines");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
               String[] columns = {"ID", "Name", "Expiry Date"};
    
               DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
       
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
    
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, name, expiry_date FROM testMed WHERE expiry_date < CURDATE()");
            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDate("expiry_date")
                };
                model.addRow(row);  
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching expired medicines: " + e.getMessage());
        }
    
        frame.setVisible(true);  
    }
    
  
public void searchbyName(String name) {
    try {
        PreparedStatement pstmt = connection.prepareStatement(
            "SELECT * FROM testMed WHERE name LIKE ? OR formula LIKE ?");
        String searchPattern = "%" + name + "%";
        pstmt.setString(1, searchPattern);
        pstmt.setString(2, searchPattern);
        
        ResultSet rs = pstmt.executeQuery();
        StringBuilder result = new StringBuilder();
        boolean found = false;
        
        while(rs.next()) {
            found = true;
            result.append(String.format("ID: %d | Name: %s | Formula: %s | Expiry: %s | Quantity: %d | Price: %.2f\n",
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("formula"),
                rs.getDate("expiry_date"),
                rs.getInt("quantity"),
                rs.getDouble("price")));
        }
        
        if (!found) {
            System.out.println("No medicines found matching: " + name);
        } else {
            System.out.println(result.toString());
        }
    } catch (SQLException e) {
        System.out.println("Error searching for medicine: " + e.getMessage());
    }
}

   
   public void updateQuantity(int id, int quantity) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE testMed SET quantity = quantity-? WHERE id = ?");
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            // JOptionPane.showMessageDialog(null,
            //     "Quantity updated successfully!",
            //     "Success",
            //     JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error updating quantity: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    public void updateQuan(int id, int quantity) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE testMed SET quantity = quantity + ? WHERE id = ?");
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Quantity updated successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

     public void updatePrice(int id, double price) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE testMed SET price = ? WHERE id = ?");
            pstmt.setDouble(1, price);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null,
                "Price updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error updating price: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
   
public void updateExpiry(int id, String expiryDate) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE testMed SET expiry_date = ? WHERE id = ?");
            pstmt.setDate(1, DateUtil.convertStringToSqlDate(expiryDate));
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Expiry date updated successfully");
        } catch (SQLException | ParseException e) {
            System.out.println(e.getMessage());
        }
    }
    public void deleteMedicine(int id) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "DELETE FROM testMed WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Medicine deleted successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void SeeAllMedicines() {
              JFrame frame = new JFrame("All Medicines");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
                String[] columns = {"ID", "Name", "Formula", "Expiry Date", "Quantity", "Price"};
        
                DefaultTableModel model = new DefaultTableModel(columns, 1);
        
                JTable table = new JTable(model);
        
                JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
        
               try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM testMed");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                    Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("formula"),
                    rs.getDate("expiry_date"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                };
                model.addRow(row);  
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage());
        }
        
        
        frame.setVisible(true);
    }

    public void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection closed");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public boolean checkQuantity(int id, int quantity) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT quantity FROM testMed WHERE id = ?");
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("quantity") >= quantity;
            }
            return false; 
            
        } catch (SQLException e) {
            System.out.println("Error checking quantity: " + e.getMessage());
            return false;
        }
    }

    public double getMedicinePrice(int medicineId) {
        String query = "SELECT price FROM testMed WHERE id = ?";
        double price = -1; 
    
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, medicineId);
    
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    price = resultSet.getDouble("price");
                } else {
                    System.out.println("No medicine found with ID: " + medicineId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving price: " + e.getMessage());
        }
        return price;
    }


    public void checkLowStock() {
              JFrame frame = new JFrame("Low Stock Medicines");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
                String[] columns = {"ID", "Name", "Quantity"};
    
                DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
    
                JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
    
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, name, quantity FROM testMed WHERE quantity < 10");
            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity")
                };
                model.addRow(row); 
                        }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching low stock medicines: " + e.getMessage());
        }
            frame.setVisible(true);  
    }
    
public void SeeAllMedicines(DefaultTableModel model) {
    model.setRowCount(0);
    try {
        PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM testMed");
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String status = "In Stock";
            if (rs.getDate("expiry_date").before(new java.sql.Date(System.currentTimeMillis()))) {
                status = "Expired";
            } else if (rs.getInt("quantity") < 10) {
                status = "Low Stock";
            }
            
            Object[] row = {
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("formula"),
                rs.getDate("expiry_date"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                status
            };
            model.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage());
    }
}

public void checkLowStock(DefaultTableModel model) {
    model.setRowCount(0);
    try {
        PreparedStatement pstmt = connection.prepareStatement(
            "SELECT * FROM testMed WHERE quantity < 10");
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("formula"),
                rs.getDate("expiry_date"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                "Low Stock"
            };
            model.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error fetching low stock: " + e.getMessage());
    }
}

public void getExpiredMedicines(DefaultTableModel model) {
    model.setRowCount(0);
    try {
        PreparedStatement pstmt = connection.prepareStatement(
            "SELECT * FROM testMed WHERE expiry_date < CURDATE()");
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("formula"),
                rs.getDate("expiry_date"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                "Expired"
            };
            model.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error fetching expired: " + e.getMessage());
    }
}

public void refreshConnection() {
    try {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null,
            "Error refreshing database connection: " + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

public boolean medicineExists(int id) {
    try {
        PreparedStatement pstmt = connection.prepareStatement(
            "SELECT id FROM testMed WHERE id = ?");
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    } catch (SQLException e) {
        return false;
    }
}

}