import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;


public class CustomerForm extends JFrame {
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private JTextField customerIdField, firstNameField, lastNameField, addressField, emailField, phoneNumberField, registrationDateField, planIDField;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public CustomerForm() {
        setTitle("Customer Management");
        setSize(900, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
        loadCustomers();
    }

    private void initializeUI() {
        tableModel = new DefaultTableModel(new String[]{"CustomerID", "First Name", "Last Name", "Address", "Email", "Phone Number", "Registration Date", "PlanID"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        customersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customersTable);
        add(scrollPane, BorderLayout.CENTER);

        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customersTable.getSelectedRow() != -1) {
                updateInputFields();
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        customerIdField = new JTextField(); firstNameField = new JTextField();
        lastNameField = new JTextField(); addressField = new JTextField();
        emailField = new JTextField(); phoneNumberField = new JTextField();
        registrationDateField = new JTextField(); planIDField = new JTextField();

        inputPanel.add(new JLabel("Customer ID:")); inputPanel.add(customerIdField);
        inputPanel.add(new JLabel("First Name:")); inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:")); inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Address:")); inputPanel.add(addressField);
        inputPanel.add(new JLabel("Email:")); inputPanel.add(emailField);
        inputPanel.add(new JLabel("Phone Number:")); inputPanel.add(phoneNumberField);
        inputPanel.add(new JLabel("Registration Date:")); inputPanel.add(registrationDateField);
        inputPanel.add(new JLabel("Plan ID:")); inputPanel.add(planIDField);

        add(inputPanel, BorderLayout.NORTH);

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(e -> addOrUpdateCustomer(true));
        updateButton.addActionListener(e -> addOrUpdateCustomer(false));
        deleteButton.addActionListener(this::deleteCustomer);
        clearButton.addActionListener(e -> clearForm());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton); buttonPanel.add(updateButton); buttonPanel.add(deleteButton); buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customer")) {
            tableModel.setRowCount(0);
            int maxCustomerId = 0;
            while (rs.next()) {
                int customerId = rs.getInt("CustomerID");
                maxCustomerId = Math.max(customerId, maxCustomerId);
                Vector<Object> row = new Vector<>();
                row.add(customerId); row.add(rs.getString("FirstName")); row.add(rs.getString("LastName"));
                row.add(rs.getString("Address")); row.add(rs.getString("Email")); row.add(rs.getString("PhoneNumber"));
                row.add(rs.getString("RegistrationDate")); row.add(rs.getInt("PlanID")); // Fixed to read as string
                tableModel.addRow(row);
            }
            customerIdField.setText(Integer.toString(maxCustomerId + 1));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateInputFields() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = customersTable.convertRowIndexToModel(selectedRow);
            customerIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            firstNameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lastNameField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            addressField.setText(tableModel.getValueAt(selectedRow, 3).toString());
            emailField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            phoneNumberField.setText(tableModel.getValueAt(selectedRow, 5).toString());
            registrationDateField.setText(tableModel.getValueAt(selectedRow, 6).toString());
            planIDField.setText(tableModel.getValueAt(selectedRow, 7).toString());
        }
    }

    private void addOrUpdateCustomer(boolean isAdding) {
        String sql;
        if (isAdding) {
            sql = "INSERT INTO Customer (FirstName, LastName, Address, Email, PhoneNumber, RegistrationDate, PlanID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE Customer SET FirstName = ?, LastName = ?, Address = ?, Email = ?, PhoneNumber = ?, RegistrationDate = ?, PlanID = ? WHERE CustomerID = ?";
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;

            pstmt.setString(index++, firstNameField.getText());
            pstmt.setString(index++, lastNameField.getText());
            pstmt.setString(index++, addressField.getText());
            pstmt.setString(index++, emailField.getText());
            pstmt.setString(index++, phoneNumberField.getText());
            pstmt.setString(index++, registrationDateField.getText()); // Fixed to use string directly
            pstmt.setInt(index++, Integer.parseInt(planIDField.getText()));

            if (!isAdding) {
                pstmt.setInt(index, Integer.parseInt(customerIdField.getText()));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Customer data has been " + (isAdding ? "added" : "updated") + " successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No changes were made to the database.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            clearForm();
            loadCustomers();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer(ActionEvent e) {
        if (customerIdField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Customer WHERE CustomerID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(customerIdField.getText()));
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Customer could not be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                clearForm();
                loadCustomers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        customerIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        addressField.setText("");
        emailField.setText("");
        phoneNumberField.setText("");
        registrationDateField.setText("");
        planIDField.setText("");
        customersTable.clearSelection();
        loadCustomers();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerForm().setVisible(true));
    }
}

