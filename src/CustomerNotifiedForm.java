
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class CustomerNotifiedForm extends JFrame {
    private JTable customerNotifiedTable;
    private DefaultTableModel tableModel;
    private JTextField notificationIdField, customerIdField, statusField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedRow = -1;

    public CustomerNotifiedForm() {
        setTitle("Customer Notification Management");
        setSize(700, 400);
        initializeUI();
        loadCustomerNotifications();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"NotificationID", "CustomerID", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        customerNotifiedTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerNotifiedTable);
        add(scrollPane, BorderLayout.CENTER);

        customerNotifiedTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerNotifiedTable.getSelectedRow() != -1) {
                selectedRow = customerNotifiedTable.getSelectedRow();
                notificationIdField.setText(customerNotifiedTable.getValueAt(selectedRow, 0).toString());
                customerIdField.setText(customerNotifiedTable.getValueAt(selectedRow, 1).toString());
                statusField.setText(customerNotifiedTable.getValueAt(selectedRow, 2).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        notificationIdField = new JTextField();
        customerIdField = new JTextField();
        statusField = new JTextField();
        inputPanel.add(new JLabel("Notification ID:"));
        inputPanel.add(notificationIdField);
        inputPanel.add(new JLabel("Customer ID:"));
        inputPanel.add(customerIdField);
        inputPanel.add(new JLabel("Status:"));
        inputPanel.add(statusField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addCustomerNotified);
        updateButton.addActionListener(this::updateCustomerNotified);
        deleteButton.addActionListener(this::deleteCustomerNotified);
        clearButton.addActionListener(this::clearForm);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCustomerNotifications() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Customer_Notified ORDER BY NotificationID ASC, CustomerID ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("NotificationID"),
                    rs.getInt("CustomerID"),
                    rs.getString("Status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customer notifications: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomerNotified(ActionEvent e) {
        // The implementation for adding a new customer notified record goes here
        // Similar to the original addCustomerNotified method but with UI
        // reloading and form clearing after the operation
        String sql = "INSERT INTO Customer_Notified (NotificationID, CustomerID, Status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int notificationId = Integer.parseInt(notificationIdField.getText());
            int customerId = Integer.parseInt(customerIdField.getText());
            String status = statusField.getText();

            pstmt.setInt(1, notificationId);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, status);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer notified record added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCustomerNotifications();
            clearForm(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding customer notified record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid IDs.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomerNotified(ActionEvent e) {
        // Update selected customer notified record
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update", "Select Record", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String sql = "UPDATE Customer_Notified SET Status = ? WHERE NotificationID = ? AND CustomerID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int notificationId = Integer.parseInt(notificationIdField.getText());
            int customerId = Integer.parseInt(customerIdField.getText());
            String status = statusField.getText();

            pstmt.setString(1, status);
            pstmt.setInt(2, notificationId);
            pstmt.setInt(3, customerId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer notified record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCustomerNotifications();
            clearForm(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating customer notified record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please ensure all fields are correctly filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteCustomerNotified(ActionEvent e) {
        // Delete selected customer notified record
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete", "Select Record", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Customer_Notified WHERE NotificationID = ? AND CustomerID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                int notificationId = Integer.parseInt(notificationIdField.getText());
                int customerId = Integer.parseInt(customerIdField.getText());

                pstmt.setInt(1, notificationId);
                pstmt.setInt(2, customerId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer notified record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomerNotifications();
                clearForm(null);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting customer notified record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm(ActionEvent e) {
        // Clear all input fields and reset the form
        notificationIdField.setText("");
        customerIdField.setText("");
        statusField.setText("");
        selectedRow = -1;
        customerNotifiedTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerNotifiedForm().setVisible(true));
    }
}

