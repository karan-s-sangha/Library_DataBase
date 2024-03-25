import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class MaintenanceRecordForm extends JFrame {
    private JTable maintenanceRecordsTable;
    private DefaultTableModel tableModel;
    private JTextField isbnField, dateField, descriptionField, actionTakenField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedRow = -1;

    public MaintenanceRecordForm() {
        setTitle("Maintenance Record Management");
        setSize(700, 400);
        initializeUI();
        loadMaintenanceRecords();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"RecordID", "ISBN", "Date", "Description", "Action Taken"};
        tableModel = new DefaultTableModel(columnNames, 0);
        maintenanceRecordsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(maintenanceRecordsTable);
        add(scrollPane, BorderLayout.CENTER);

        maintenanceRecordsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && maintenanceRecordsTable.getSelectedRow() != -1) {
                selectedRow = maintenanceRecordsTable.getSelectedRow();
                isbnField.setText(maintenanceRecordsTable.getValueAt(selectedRow, 1).toString());
                String dateValue = maintenanceRecordsTable.getValueAt(selectedRow, 2).toString();
                dateField.setText(dateValue.equals("null") ? "" : dateValue); // Handle null dates
                descriptionField.setText(maintenanceRecordsTable.getValueAt(selectedRow, 3).toString());
                actionTakenField.setText(maintenanceRecordsTable.getValueAt(selectedRow, 4).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        isbnField = new JTextField();
        dateField = new JTextField();
        descriptionField = new JTextField();
        actionTakenField = new JTextField();
        inputPanel.add(new JLabel("ISBN:"));
        inputPanel.add(isbnField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Action Taken:"));
        inputPanel.add(actionTakenField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addMaintenanceRecord);
        updateButton.addActionListener(this::updateMaintenanceRecord);
        deleteButton.addActionListener(this::deleteMaintenanceRecord);
        clearButton.addActionListener(this::clearForm);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadMaintenanceRecords() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM MaintenanceRecord ORDER BY RecordID ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("RecordID"),
                    rs.getString("ISBN"),
                    rs.getString("Date"), // Date handled as string
                    rs.getString("Description"),
                    rs.getString("ActionTaken")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading maintenance records: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMaintenanceRecord(ActionEvent e) {
        String sql = "INSERT INTO MaintenanceRecord (ISBN, Date, Description, ActionTaken) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbnField.getText());
            pstmt.setString(2, dateField.getText()); // the date is entered in YYYY-MM-DD format
            pstmt.setString(3, descriptionField.getText());
            pstmt.setString(4, actionTakenField.getText());

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Maintenance record added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(null);
            loadMaintenanceRecords();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding maintenance record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateMaintenanceRecord(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "Select Record", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String sql = "UPDATE MaintenanceRecord SET ISBN = ?, Date = ?, Description = ?, ActionTaken = ? WHERE RecordID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int recordID = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            pstmt.setString(1, isbnField.getText());
            pstmt.setString(2, dateField.getText()); // Assuming the date is entered in YYYY-MM-DD format
            pstmt.setString(3, descriptionField.getText());
            pstmt.setString(4, actionTakenField.getText());
            pstmt.setInt(5, recordID);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Maintenance record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(null);
            loadMaintenanceRecords();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating maintenance record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMaintenanceRecord(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Select Record", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM MaintenanceRecord WHERE RecordID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                int recordID = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                pstmt.setInt(1, recordID);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Maintenance record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm(null);
                loadMaintenanceRecords();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting maintenance record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm(ActionEvent e) {
        isbnField.setText("");
        dateField.setText("");
        descriptionField.setText("");
        actionTakenField.setText("");
        selectedRow = -1;
        maintenanceRecordsTable.clearSelection();
        loadMaintenanceRecords();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MaintenanceRecordForm().setVisible(true));
    }
}
