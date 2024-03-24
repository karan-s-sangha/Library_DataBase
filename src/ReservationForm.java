import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class ReservationForm extends JFrame {
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JTextField customerIdField, isbnField, reservationDateField, reservationStatusField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedReservationId = -1;

    public ReservationForm() {
        setTitle("Reservation Management");
        setSize(700, 400);
        initializeUI();
        loadReservations();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"ReservationID", "CustomerID", "ISBN", "ReservationDate", "ReservationStatus"};
        tableModel = new DefaultTableModel(columnNames, 0);
        reservationsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        add(scrollPane, BorderLayout.CENTER);

        reservationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && reservationsTable.getSelectedRow() != -1) {
                int selectedRow = reservationsTable.getSelectedRow();
                selectedReservationId = Integer.parseInt(reservationsTable.getValueAt(selectedRow, 0).toString());
                customerIdField.setText(reservationsTable.getValueAt(selectedRow, 1).toString());
                isbnField.setText(reservationsTable.getValueAt(selectedRow, 2).toString());
                reservationDateField.setText(reservationsTable.getValueAt(selectedRow, 3).toString());
                reservationStatusField.setText(reservationsTable.getValueAt(selectedRow, 4).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        customerIdField = new JTextField();
        isbnField = new JTextField();
        reservationDateField = new JTextField();
        reservationStatusField = new JTextField();

        inputPanel.add(new JLabel("Customer ID:"));
        inputPanel.add(customerIdField);
        inputPanel.add(new JLabel("ISBN:"));
        inputPanel.add(isbnField);
        inputPanel.add(new JLabel("Reservation Date (YYYY-MM-DD):"));
        inputPanel.add(reservationDateField);
        inputPanel.add(new JLabel("Reservation Status:"));
        inputPanel.add(reservationStatusField);
        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addReservation);
        updateButton.addActionListener(this::updateReservation);
        deleteButton.addActionListener(this::deleteReservation);
        clearButton.addActionListener(this::clearFields);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReservations() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Reservation ORDER BY ReservationID ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("ReservationID"),
                        rs.getInt("CustomerID"),
                        rs.getString("ISBN"),
                        rs.getString("ReservationDate"),
                        rs.getString("ReservationStatus")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading reservations: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addReservation(ActionEvent e) {
        String customerID = customerIdField.getText();
        String isbn = isbnField.getText();
        String reservationDate = reservationDateField.getText();
        String reservationStatus = reservationStatusField.getText();

        String sql = "INSERT INTO Reservation (CustomerID, ISBN, ReservationDate, ReservationStatus) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(customerID));
            pstmt.setString(2, isbn);
            pstmt.setString(3, reservationDate);
            pstmt.setString(4, reservationStatus);
            pstmt.executeUpdate();
            loadReservations();
            clearFields(null);
            JOptionPane.showMessageDialog(this, "Reservation added successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding reservation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Customer ID format!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReservation(ActionEvent e) {
        if (selectedReservationId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to update!", "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String customerID = customerIdField.getText();
        String isbn = isbnField.getText();
        String reservationDate = reservationDateField.getText();
        String reservationStatus = reservationStatusField.getText();

        String sql = "UPDATE Reservation SET CustomerID=?, ISBN=?, ReservationDate=?, ReservationStatus=? WHERE ReservationID=?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(customerID));
            pstmt.setString(2, isbn);
            pstmt.setString(3, reservationDate);
            pstmt.setString(4, reservationStatus);
            pstmt.setInt(5, selectedReservationId);
            pstmt.executeUpdate();
            loadReservations();
            clearFields(null);
            JOptionPane.showMessageDialog(this, "Reservation updated successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating reservation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Customer ID format!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReservation(ActionEvent e) {
        if (selectedReservationId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to delete!", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "DELETE FROM Reservation WHERE ReservationID=?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedReservationId);
            pstmt.executeUpdate();
            loadReservations();
            clearFields(null);
            JOptionPane.showMessageDialog(this, "Reservation deleted successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting reservation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields(ActionEvent e) {
        customerIdField.setText("");
        isbnField.setText("");
        reservationDateField.setText("");
        reservationStatusField.setText("");
        selectedReservationId = -1;
        reservationsTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationForm().setVisible(true));
    }
}
