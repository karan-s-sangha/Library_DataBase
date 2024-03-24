
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class AuthorForm extends JFrame {
    private JTextField authorIDField, firstNameField, lastNameField, addressField, phoneNumberField;
    private JButton addButton, updateButton, deleteButton, clearButton; // Added clearButton
    private DefaultTableModel tableModel;
    private int selectedAuthorId = -1;

    public AuthorForm() {
        setTitle("Author Management");
        setSize(700, 400);
        initializeUI();
        loadAuthors();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        String[] columnNames = {"AuthorID", "First Name", "Last Name", "Address", "Phone Number"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable authorsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(authorsTable);
        add(scrollPane, BorderLayout.CENTER);

        authorsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = authorsTable.getSelectedRow();
            if (selectedRow != -1) {
                selectedAuthorId = Integer.parseInt(authorsTable.getValueAt(selectedRow, 0).toString());
                authorIDField.setText(authorsTable.getValueAt(selectedRow, 0).toString());
                firstNameField.setText(authorsTable.getValueAt(selectedRow, 1).toString());
                lastNameField.setText(authorsTable.getValueAt(selectedRow, 2).toString());
                addressField.setText(authorsTable.getValueAt(selectedRow, 3).toString());
                phoneNumberField.setText(authorsTable.getValueAt(selectedRow, 4).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        authorIDField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        addressField = new JTextField();
        phoneNumberField = new JTextField();

        inputPanel.add(new JLabel("Author ID:"));
        inputPanel.add(authorIDField);
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Address:"));
        inputPanel.add(addressField);
        inputPanel.add(new JLabel("Phone Number:"));
        inputPanel.add(phoneNumberField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear"); // Create Clear button

        addButton.addActionListener(this::addAuthor);
        updateButton.addActionListener(this::updateAuthor);
        deleteButton.addActionListener(this::deleteAuthor);
        clearButton.addActionListener(this::clearFields); // Add listener for Clear button

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton); // Add Clear button to panel

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAuthors() {
        tableModel.setRowCount(0); // Clear table
        String sql = "SELECT * FROM Author";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("AuthorID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Address"),
                        rs.getString("PhoneNumber")
                };

                tableModel.addRow(row);
            }

            // Find the maximum author ID
            int maxAuthorId = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int id = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                if (id > maxAuthorId) {
                    maxAuthorId = id;
                }
            }
            // Set the next available author ID
            authorIDField.setText(String.valueOf(maxAuthorId + 1));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading authors: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAuthor(ActionEvent e) {
        int nextAuthorId = Integer.parseInt(authorIDField.getText());
        if (nextAuthorId <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid author ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        executeAuthorChange("INSERT INTO Author (AuthorID, FirstName, LastName, Address, PhoneNumber) VALUES (?, ?, ?, ?, ?)");
    }

    private void updateAuthor(ActionEvent e) {
        if (selectedAuthorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an author to update", "Select Author", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        executeAuthorChange("UPDATE Author SET AuthorID = ?, FirstName = ?, LastName = ?, Address = ?, PhoneNumber = ? WHERE AuthorID = ?");
    }

    private void deleteAuthor(ActionEvent e) {
        if (selectedAuthorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an author to delete", "Select Author", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this author?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Author WHERE AuthorID = ?";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedAuthorId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Author deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAuthors();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting author: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeAuthorChange(String sql) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(authorIDField.getText()));
            pstmt.setString(2, firstNameField.getText());
            pstmt.setString(3, lastNameField.getText());
            pstmt.setString(4, addressField.getText());
            pstmt.setString(5, phoneNumberField.getText());

            if (sql.startsWith("UPDATE")) {
                pstmt.setInt(6, selectedAuthorId);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, "No changes were made.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Author successfully " + (sql.startsWith("INSERT") ? "added." : "updated."), "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAuthors();
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error saving the author: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields(ActionEvent e) {
        authorIDField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        addressField.setText("");
        phoneNumberField.setText("");
        selectedAuthorId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthorForm().setVisible(true));
    }
}
