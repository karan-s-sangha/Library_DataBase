import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FineForm extends JFrame {
    private JTable finesTable;
    private DefaultTableModel tableModel;
    private JTextField descriptionField, amountDueField, dueDateField, borrowIDField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedFineId = -1;

    public FineForm() {
        setTitle("Fine Management");
        setSize(700, 400);
        initializeUI();
        loadFines();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"FineID", "Description", "Amount Due", "Due Date", "Borrow ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make table cells non-editable
                return false;
            }
        };
        finesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(finesTable);
        add(scrollPane, BorderLayout.CENTER);

        finesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && finesTable.getSelectedRow() != -1) {
                int selectedRow = finesTable.getSelectedRow();
                selectedFineId = Integer.parseInt(finesTable.getValueAt(selectedRow, 0).toString());
                descriptionField.setText(finesTable.getValueAt(selectedRow, 1).toString());
                amountDueField.setText(finesTable.getValueAt(selectedRow, 2).toString());
                dueDateField.setText(finesTable.getValueAt(selectedRow, 3).toString());
                borrowIDField.setText(finesTable.getValueAt(selectedRow, 4).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        descriptionField = new JTextField();
        amountDueField = new JTextField();
        dueDateField = new JTextField();
        borrowIDField = new JTextField();

        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Amount Due:"));
        inputPanel.add(amountDueField);
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Borrow ID:"));
        inputPanel.add(borrowIDField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addFine);
        updateButton.addActionListener(this::updateFine);
        deleteButton.addActionListener(this::deleteFine);
        clearButton.addActionListener(this::clearFields);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadFines() {
        String sql = "SELECT FineID, Description, AmountDue, DueDate, BorrowID FROM Fine ORDER BY FineID ASC";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("FineID"),
                        rs.getString("Description"),
                        rs.getDouble("AmountDue"),
                        rs.getString("DueDate"),
                        rs.getInt("BorrowID")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFine(ActionEvent e) {
        String sql = "INSERT INTO Fine (Description, AmountDue, DueDate, BorrowID) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, descriptionField.getText());
            pstmt.setDouble(2, Double.parseDouble(amountDueField.getText()));
            pstmt.setString(3, dueDateField.getText()); // Parse as String
            pstmt.setInt(4, Integer.parseInt(borrowIDField.getText()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Fine added successfully.");
                loadFines();
                clearFields(null);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding fine: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Validation Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFine(ActionEvent e) {
        if (selectedFineId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fine to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String sql = "UPDATE Fine SET Description = ?, AmountDue = ?, DueDate = ?, BorrowID = ? WHERE FineID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, descriptionField.getText());
            pstmt.setDouble(2, Double.parseDouble(amountDueField.getText()));
            pstmt.setString(3, dueDateField.getText()); // Parse as String
            pstmt.setInt(4, Integer.parseInt(borrowIDField.getText()));
            pstmt.setInt(5, selectedFineId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Fine updated successfully.");
                loadFines();
                clearFields(null);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating fine: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFine(ActionEvent e) {
        if (selectedFineId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fine to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this fine?", "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Fine WHERE FineID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedFineId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Fine deleted successfully.");
                    loadFines();
                    clearFields(null);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting fine: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields(ActionEvent e) {
        descriptionField.setText("");
        amountDueField.setText("");
        dueDateField.setText("");
        borrowIDField.setText("");
        selectedFineId = -1;
        finesTable.clearSelection();
        }
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> new FineForm().setVisible(true));
        }
}        