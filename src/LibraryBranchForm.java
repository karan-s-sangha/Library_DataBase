
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;

public class LibraryBranchForm extends JFrame {
    private JTextField branchIDField, nameField, addressField, phoneNumberField, operatingHoursField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private DefaultTableModel tableModel;
    private JTable branchTable;

    public LibraryBranchForm() {
        setTitle("Library Branch Management");
        setSize(800, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadBranches();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        branchIDField = new JTextField();
        nameField = new JTextField();
        addressField = new JTextField();
        phoneNumberField = new JTextField();
        operatingHoursField = new JTextField();
        inputPanel.add(new JLabel("BranchID:"));
        inputPanel.add(branchIDField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Address:"));
        inputPanel.add(addressField);
        inputPanel.add(new JLabel("Phone Number:"));
        inputPanel.add(phoneNumberField);
        inputPanel.add(new JLabel("Operating Hours:"));
        inputPanel.add(operatingHoursField);
        add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"BranchID", "Name", "Address", "Phone Number", "Operating Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Allow editing for all cells
            }
        };
        branchTable = new JTable(tableModel);
        branchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && branchTable.getSelectedRow() != -1) {
                    int selectedRow = branchTable.getSelectedRow();
                    branchIDField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    addressField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    phoneNumberField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    operatingHoursField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(branchTable);
        add(scrollPane, BorderLayout.CENTER);

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addBranch);
        updateButton.addActionListener(this::updateBranch);
        deleteButton.addActionListener(this::deleteBranch);
        clearButton.addActionListener(e -> clearFields());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadBranches() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM LibraryBranch");
             ResultSet rs = pstmt.executeQuery()) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("BranchID"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Address"));
                row.add(rs.getString("PhoneNumber"));
                row.add(rs.getString("OperatingHours"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading library branches: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBranch(ActionEvent e) {
        int branchID = Integer.parseInt(branchIDField.getText().trim());
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String operatingHours = operatingHoursField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || operatingHours.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO LibraryBranch (BranchID, Name, Address, PhoneNumber, OperatingHours) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchID);
            pstmt.setString(2, name);
            pstmt.setString(3, address);
            pstmt.setString(4, phoneNumber);
            pstmt.setString(5, operatingHours);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, "Failed to add library branch.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Library branch added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadBranches();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding library branch: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBranch(ActionEvent e) {
        int selectedRow = branchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a library branch to update.", "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int branchID = Integer.parseInt(branchIDField.getText().trim());
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String operatingHours = operatingHoursField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || operatingHours.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE LibraryBranch SET Name = ?, Address = ?, PhoneNumber = ?, OperatingHours = ? WHERE BranchID = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, operatingHours);
            pstmt.setInt(5, branchID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, "Failed to update library branch.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Library branch updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadBranches();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating library branch: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBranch(ActionEvent e) {
        int selectedRow = branchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a library branch to delete.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int branchID = Integer.parseInt(branchIDField.getText().trim());
        String sql = "DELETE FROM LibraryBranch WHERE BranchID = ?";

        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this library branch?", "Delete Library Branch", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, branchID);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    JOptionPane.showMessageDialog(this, "Failed to delete library branch.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Library branch deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadBranches();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting library branch: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        branchIDField.setText("");
        nameField.setText("");
        addressField.setText("");
        phoneNumberField.setText("");
        operatingHoursField.setText("");
        branchTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryBranchForm().setVisible(true));
    }
}
