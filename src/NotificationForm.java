import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationForm extends JFrame {
    private JTable notificationsTable;
    private DefaultTableModel tableModel;
    private JTextField messageField, dateField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedNotificationId = -1;

    public NotificationForm() {
        setTitle("Notification Management");
        setSize(700, 400);
        initializeUI();
        loadNotifications();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"NotificationID", "Message", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        notificationsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        add(scrollPane, BorderLayout.CENTER);

        notificationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && notificationsTable.getSelectedRow() != -1) {
                int selectedRow = notificationsTable.getSelectedRow();
                selectedNotificationId = Integer.parseInt(notificationsTable.getValueAt(selectedRow, 0).toString());
                messageField.setText(notificationsTable.getValueAt(selectedRow, 1).toString());
                dateField.setText(notificationsTable.getValueAt(selectedRow, 2).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        messageField = new JTextField();
        dateField = new JTextField();

        inputPanel.add(new JLabel("Message:"));
        inputPanel.add(messageField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD HH:MM):"));
        inputPanel.add(dateField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addNotification);
        updateButton.addActionListener(this::updateNotification);
        deleteButton.addActionListener(this::deleteNotification);
        clearButton.addActionListener(this::clearForm);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Notification ORDER BY NotificationID ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("NotificationID"),
                        rs.getString("Message"),
                        rs.getString("Date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateNotification(ActionEvent e) {
        if (selectedNotificationId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to update", "Select Notification", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String sql = "UPDATE Notification SET Message = ?, Date = ? WHERE NotificationID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, messageField.getText());
            pstmt.setString(2, dateField.getText());
            pstmt.setInt(3, selectedNotificationId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Notification updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadNotifications();
            clearForm(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating notification: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteNotification(ActionEvent e) {
        if (selectedNotificationId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to delete", "Select Notification", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this notification?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Notification WHERE NotificationID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedNotificationId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Notification deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadNotifications();
                clearForm(null);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting notification: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm(ActionEvent e) {
        messageField.setText("");
        dateField.setText("");
        selectedNotificationId = -1;
        notificationsTable.clearSelection();
        loadNotifications();
    }

    private void addNotification(ActionEvent e) {
        String sql = "INSERT INTO Notification (Message, Date) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, messageField.getText());
            pstmt.setString(2, dateField.getText());
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Notification added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadNotifications();
            clearForm(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding notification: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NotificationForm().setVisible(true));
    }
}
