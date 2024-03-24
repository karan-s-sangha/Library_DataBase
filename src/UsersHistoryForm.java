import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UsersHistoryForm extends JFrame {
    private JTextField historyIDField, userIDField, borrowIDField, dateField, timeField, fineIDField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private DefaultTableModel tableModel;
    private int selectedHistoryId = -1;

    public UsersHistoryForm() {
        setTitle("User History Management");
        setSize(800, 600);
        initializeUI();
        loadHistoryData();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        String[] columnNames = {"History ID", "User ID", "Borrow ID", "Date", "Time", "Fine ID"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable historyTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow != -1) {
                selectedHistoryId = Integer.parseInt(historyTable.getValueAt(selectedRow, 0).toString());
                historyIDField.setText(historyTable.getValueAt(selectedRow, 0).toString());
                userIDField.setText(historyTable.getValueAt(selectedRow, 1).toString());
                borrowIDField.setText(historyTable.getValueAt(selectedRow, 2).toString());
                dateField.setText(historyTable.getValueAt(selectedRow, 3).toString());
                timeField.setText(historyTable.getValueAt(selectedRow, 4).toString());
                fineIDField.setText(historyTable.getValueAt(selectedRow, 5).toString().equals("N/A") ? "" : historyTable.getValueAt(selectedRow, 5).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        historyIDField = new JTextField();
        userIDField = new JTextField();
        borrowIDField = new JTextField();
        dateField = new JTextField(LocalDate.now().toString());
        timeField = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        fineIDField = new JTextField();

        inputPanel.add(new JLabel("History ID:"));
        inputPanel.add(historyIDField);
        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(userIDField);
        inputPanel.add(new JLabel("Borrow ID:"));
        inputPanel.add(borrowIDField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Time (HH:MM:SS):"));
        inputPanel.add(timeField);
        inputPanel.add(new JLabel("Fine ID (optional):"));
        inputPanel.add(fineIDField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::saveUserHistory);
        updateButton.addActionListener(this::updateUserHistory);
        deleteButton.addActionListener(this::deleteUserHistory);
        clearButton.addActionListener(this::clearForm);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadHistoryData() {
        tableModel.setRowCount(0); // Clear existing data before loading new data
        String query = "SELECT * FROM UsersHistory";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("HistoryID"),
                        rs.getInt("UserID"),
                        rs.getInt("BorrowID"),
                        rs.getString("Date"),
                        rs.getString("Time"),
                        rs.getObject("FineID") != null ? rs.getInt("FineID") : "N/A"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user histories: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUserHistory(ActionEvent e) {
        String sql = "INSERT INTO UsersHistory (HistoryID, UserID, BorrowID, Date, Time, FineID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(historyIDField.getText().trim()));
            pstmt.setInt(2, Integer.parseInt(userIDField.getText().trim()));
            pstmt.setInt(3, Integer.parseInt(borrowIDField.getText().trim()));
            pstmt.setString(4, dateField.getText().trim());
            pstmt.setString(5, timeField.getText().trim());
            if (fineIDField.getText().trim().isEmpty()) {
                pstmt.setNull(6, Types.INTEGER);
            } else {
                pstmt.setInt(6, Integer.parseInt(fineIDField.getText().trim()));
            }

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "History added successfully!");
            clearForm(null);
            loadHistoryData(); // Refresh the table
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check your entries.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserHistory(ActionEvent e) {
        if (selectedHistoryId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a history record to update.", "No Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE UsersHistory SET UserID = ?, BorrowID = ?, Date = ?, Time = ?, FineID = ? WHERE HistoryID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userIDField.getText().trim()));
            pstmt.setInt(2, Integer.parseInt(borrowIDField.getText().trim()));
            pstmt.setString(3, dateField.getText().trim());
            pstmt.setString(4, timeField.getText().trim());
            if (fineIDField.getText().trim().isEmpty()) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, Integer.parseInt(fineIDField.getText().trim()));
            }
            pstmt.setInt(6, selectedHistoryId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "History updated successfully!");
                clearForm(null);
                loadHistoryData(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update the history.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check your entries.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUserHistory(ActionEvent e)
    {
        if (selectedHistoryId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a history record to delete.", "No Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "DELETE FROM UsersHistory WHERE HistoryID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedHistoryId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "History deleted successfully!");
                loadHistoryData(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the history.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm(ActionEvent e) {
        historyIDField.setText("");
        userIDField.setText("");
        borrowIDField.setText("");
        dateField.setText(LocalDate.now().toString());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        fineIDField.setText("");
        selectedHistoryId = -1;
        fetchNextHistoryID(); // Fetch the next suggested HistoryID
    }
    private void fetchNextHistoryID() {
        String query = "SELECT MAX(HistoryID) AS MaxHistoryID FROM UsersHistory";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextID = rs.getInt("MaxHistoryID") + 1;
                historyIDField.setText(String.valueOf(nextID));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching next History ID: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(UsersHistoryForm::new);
    }
}
