import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;

public class StaffForm extends JFrame {
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JTextField firstNameField, lastNameField, roleField, emailField, phoneNumberField;
    private JComboBox<LibraryBranch> branchComboBox;
    private Vector<LibraryBranch> branches = new Vector<>();
    private JButton addButton, updateButton, deleteButton, clearButton;

    // Use -1 to indicate no selection or a new entry
    private int selectedStaffId = -1;

    public StaffForm() {
        setTitle("Staff Management");
        setSize(700, 400);
        initializeUI();
        fetchLibraryBranches();
        loadStaff();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        String[] columnNames = {"StaffID", "First Name", "Last Name", "Role", "Email", "Phone Number", "BranchID"};
        tableModel = new DefaultTableModel(columnNames, 0);
        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(staffTable);
        add(scrollPane, BorderLayout.CENTER);

        staffTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && staffTable.getSelectedRow() != -1) {
                // Extract and display staff member's details when a row is selected
                selectedStaffId = Integer.parseInt(staffTable.getValueAt(staffTable.getSelectedRow(), 0).toString());
                firstNameField.setText(staffTable.getValueAt(staffTable.getSelectedRow(), 1).toString());
                lastNameField.setText(staffTable.getValueAt(staffTable.getSelectedRow(), 2).toString());
                roleField.setText(staffTable.getValueAt(staffTable.getSelectedRow(), 3).toString());
                emailField.setText(staffTable.getValueAt(staffTable.getSelectedRow(), 4).toString());
                phoneNumberField.setText(staffTable.getValueAt(staffTable.getSelectedRow(), 5).toString());
                
                // Set the branch combo box to the corresponding branch
                int branchId = Integer.parseInt(staffTable.getValueAt(staffTable.getSelectedRow(), 6).toString());
                for (LibraryBranch branch : branches) {
                    if (branch.getBranchID() == branchId) {
                        branchComboBox.setSelectedItem(branch);
                        break;
                    }
                }
            }
        });

        // Input panel for staff details
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        roleField = new JTextField();
        emailField = new JTextField();
        phoneNumberField = new JTextField();
        branchComboBox = new JComboBox<>(branches);

        // Add components to input panel
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Role:"));
        inputPanel.add(roleField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Phone Number:"));
        inputPanel.add(phoneNumberField);
        inputPanel.add(new JLabel("Library Branch:"));
        inputPanel.add(branchComboBox);

        add(inputPanel, BorderLayout.NORTH);

        // Button panel for operations
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        // Add action listeners for buttons
        addButton.addActionListener(this::addStaff);
        updateButton.addActionListener(this::updateStaff);
        deleteButton.addActionListener(this::deleteStaff);
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchLibraryBranches() {
        // DatabaseConnection.connect() is a method that connects to your SQLite database
        String sql = "SELECT BranchID, Name FROM LibraryBranch";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                LibraryBranch branch = new LibraryBranch(rs.getInt("BranchID"), rs.getString("Name"));
                branches.add(branch);
                branchComboBox.addItem(branch);
            }
        } catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(this, "Error fetching library branches: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStaff() {
        String sql = "SELECT StaffID, FirstName, LastName, Role, Email, PhoneNumber, BranchID FROM Staff";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            tableModel.setRowCount(0); // Clear the table first
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("StaffID"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Role"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("PhoneNumber"));
                row.add(rs.getInt("BranchID"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading staff data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStaff(ActionEvent e) {
        String sql = "INSERT INTO Staff (FirstName, LastName, Role, Email, PhoneNumber, BranchID) VALUES (?, ?, ?, ?, ?, ?)";
        LibraryBranch selectedBranch = (LibraryBranch) branchComboBox.getSelectedItem();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstNameField.getText());
            pstmt.setString(2, lastNameField.getText());
            pstmt.setString(3, roleField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, phoneNumberField.getText());
            pstmt.setInt(6, selectedBranch != null ? selectedBranch.getBranchID() : 0);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Staff member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadStaff();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding staff member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStaff(ActionEvent e) {
        if (selectedStaffId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to update.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "UPDATE Staff SET FirstName = ?, LastName = ?, Role = ?, Email = ?, PhoneNumber = ?, BranchID = ? WHERE StaffID = ?";
        LibraryBranch selectedBranch = (LibraryBranch) branchComboBox.getSelectedItem();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstNameField.getText());
            pstmt.setString(2, lastNameField.getText());
            pstmt.setString(3, roleField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, phoneNumberField.getText());
            pstmt.setInt(6, selectedBranch != null ? selectedBranch.getBranchID() : 0);
            pstmt.setInt(7, selectedStaffId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Staff member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadStaff();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating staff member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStaff(ActionEvent e) {
        if (selectedStaffId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this staff member?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Staff WHERE StaffID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedStaffId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Staff member deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStaff();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting staff member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        roleField.setText("");
        emailField.setText("");
        phoneNumberField.setText("");
        branchComboBox.setSelectedIndex(-1);
        selectedStaffId = -1;
        staffTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffForm().setVisible(true));
    }

    // Inner class for LibraryBranch
    class LibraryBranch {
        private int branchID;
        private String name;

        public LibraryBranch(int branchID, String name) {
            this.branchID = branchID;
            this.name = name;
        }

        public int getBranchID() {
            return branchID;
        }

        public void setBranchID(int branchID) {
            this.branchID = branchID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + branchID + ")";
        }
    }
}

