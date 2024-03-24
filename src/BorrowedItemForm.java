import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BorrowedItemForm extends JFrame {
    private JTextField isbnField, customerIDField, borrowDateField, returnDateField, dueDateField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JTable borrowedItemsTable;
    private DefaultTableModel tableModel;

    public BorrowedItemForm() {
        setTitle("Manage Borrowed Items");
        setSize(800, 600);
        initializeUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loadBorrowedItems();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        String[] columnNames = {"Borrow ID", "ISBN", "Customer ID", "Borrow Date", "Return Date", "Due Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        borrowedItemsTable = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        isbnField = new JTextField();
        customerIDField = new JTextField();
        borrowDateField = new JTextField();
        returnDateField = new JTextField();
        dueDateField = new JTextField();

        formPanel.add(new JLabel("ISBN:"));
        formPanel.add(isbnField);
        formPanel.add(new JLabel("Customer ID:"));
        formPanel.add(customerIDField);
        formPanel.add(new JLabel("Borrow Date (YYYY-MM-DD):"));
        formPanel.add(borrowDateField);
        formPanel.add(new JLabel("Return Date (YYYY-MM-DD):"));
        formPanel.add(returnDateField);
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        formPanel.add(dueDateField);
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::saveBorrowRecord);
        updateButton.addActionListener(this::updateBorrowRecord);
        deleteButton.addActionListener(this::deleteBorrowRecord);
        clearButton.addActionListener(this::clearForm);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JScrollPane scrollPane = new JScrollPane(borrowedItemsTable);
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        borrowedItemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = borrowedItemsTable.getSelectedRow();
                if (selectedRow != -1) {
                    isbnField.setText(borrowedItemsTable.getValueAt(selectedRow, 1).toString());
                    customerIDField.setText(borrowedItemsTable.getValueAt(selectedRow, 2).toString());
                    borrowDateField.setText(borrowedItemsTable.getValueAt(selectedRow, 3).toString());
                    returnDateField.setText(borrowedItemsTable.getValueAt(selectedRow, 4) != null ? borrowedItemsTable.getValueAt(selectedRow, 4).toString() : "");
                    dueDateField.setText(borrowedItemsTable.getValueAt(selectedRow, 5).toString());
                }
            }
        });
    }

    private void saveBorrowRecord(ActionEvent e) {
        if (!validateInput()) return;

        String isbn = isbnField.getText().trim();
        int customerID = Integer.parseInt(customerIDField.getText().trim());
        LocalDate borrowDate;
        LocalDate returnDate = null;
        LocalDate dueDate;

        try {
            borrowDate = LocalDate.parse(borrowDateField.getText().trim());
            if (!returnDateField.getText().trim().isEmpty()) {
                returnDate = LocalDate.parse(returnDateField.getText().trim());
            }
            dueDate = LocalDate.parse(dueDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please enter dates in YYYY-MM-DD format.", "Date Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO BorrowedItem (ISBN, CustomerID, BorrowDate, ReturnDate, DueDate) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setString(1, isbn);
            pstmt.setInt(2, customerID);
            pstmt.setString(3, borrowDate.toString());
            if (returnDate != null) {
                pstmt.setString(4, returnDate.toString());
            } else {
                pstmt.setNull(4, Types.NULL);
            }
            pstmt.setString(5, dueDate.toString());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Borrow record added successfully!");
                clearForm(null);
                loadBorrowedItems();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add borrow record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add borrow record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateBorrowRecord(ActionEvent e) {
        int selectedRow = borrowedItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "Update Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String isbn = isbnField.getText().trim();
        int customerID = Integer.parseInt(customerIDField.getText().trim());
        LocalDate borrowDate = LocalDate.parse(borrowDateField.getText().trim());
        LocalDate returnDate = null;
        if (!returnDateField.getText().trim().isEmpty()) {
            returnDate = LocalDate.parse(returnDateField.getText().trim());
        }
        LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());

        int borrowID = (int) borrowedItemsTable.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE BorrowedItem SET ISBN=?, CustomerID=?, BorrowDate=?, ReturnDate=?, DueDate=? WHERE BorrowID=?")) {

            pstmt.setString(1, isbn);
            pstmt.setInt(2, customerID);
            pstmt.setString(3, borrowDate.toString());
            if (returnDate != null) {
                pstmt.setString(4, returnDate.toString());
            } else {
                pstmt.setNull(4, Types.NULL);
            }
            pstmt.setString(5, dueDate.toString());
            pstmt.setInt(6, borrowID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Borrow record updated successfully!");
                clearForm(null);
                loadBorrowedItems();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update borrow record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update borrow record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBorrowRecord(ActionEvent e) {
        int selectedRow = borrowedItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int borrowID = (int) borrowedItemsTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM BorrowedItem WHERE BorrowID=?")) {

                pstmt.setInt(1, borrowID);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Borrow record deleted successfully!");
                    clearForm(null);
                    loadBorrowedItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete borrow record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete borrow record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm(ActionEvent e) {
        isbnField.setText("");
        customerIDField.setText("");
        borrowDateField.setText("");
        returnDateField.setText("");
        dueDateField.setText("");
    }

    private LocalDate convertTimestampToLocalDate(long timestamp) {
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
}

private void loadBorrowedItems() {
    clearTable();
    try (Connection conn = DatabaseConnection.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM BorrowedItem")) {

        while (rs.next()) {
            int borrowID = rs.getInt("BorrowID");
            String isbn = rs.getString("ISBN");
            int customerID = rs.getInt("CustomerID");
            LocalDate borrowDate = convertTimestampToLocalDate(rs.getLong("BorrowDate"));
            LocalDate returnDate = rs.getLong("ReturnDate") != 0 ? convertTimestampToLocalDate(rs.getLong("ReturnDate")) : null;
            LocalDate dueDate = convertTimestampToLocalDate(rs.getLong("DueDate"));

            Object[] rowData = {borrowID, isbn, customerID, borrowDate, returnDate, dueDate};
            tableModel.addRow(rowData);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error loading borrowed items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private boolean validateInput() {
        String isbn = isbnField.getText().trim();
        String customerIDText = customerIDField.getText().trim();
        String borrowDateText = borrowDateField.getText().trim();
        String returnDateText = returnDateField.getText().trim();
        String dueDateText = dueDateField.getText().trim();

        if (isbn.isEmpty() || customerIDText.isEmpty() || borrowDateText.isEmpty() || dueDateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (ISBN, Customer ID, Borrow Date, Due Date).", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            LocalDate.parse(borrowDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!returnDateText.isEmpty()) {
                LocalDate.parse(returnDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            LocalDate.parse(dueDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format.", "Date Format Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (!existsInDatabase(conn, "Item", "ISBN", isbn)) {
                JOptionPane.showMessageDialog(this, "ISBN does not exist in the database.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            int customerID = Integer.parseInt(customerIDText);
            if (!existsInDatabase(conn, "Customer", "CustomerID", Integer.toString(customerID))) {
                JOptionPane.showMessageDialog(this, "Customer ID does not exist in the database.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean existsInDatabase(Connection conn, String tableName, String columnName, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BorrowedItemForm::new);
    }
}

