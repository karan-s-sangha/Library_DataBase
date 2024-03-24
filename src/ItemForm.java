import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;

public class ItemForm extends JFrame {
    private JTextField isbnField, titleField, publishedYearField, genreField, totalCopiesField, availableCopiesField, typeField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private DefaultTableModel tableModel;
    private JTable itemsTable;

    public ItemForm() {
        setTitle("Item Management");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadItems();
        suggestNextISBN();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        isbnField = new JTextField();
        titleField = new JTextField();
        publishedYearField = new JTextField();
        genreField = new JTextField();
        totalCopiesField = new JTextField();
        availableCopiesField = new JTextField();
        typeField = new JTextField();
        inputPanel.add(new JLabel("ISBN:"));
        inputPanel.add(isbnField);
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Published Year:"));
        inputPanel.add(publishedYearField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Total Copies:"));
        inputPanel.add(totalCopiesField);
        inputPanel.add(new JLabel("Available Copies:"));
        inputPanel.add(availableCopiesField);
        inputPanel.add(new JLabel("Type:"));
        inputPanel.add(typeField);
        add(inputPanel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Published Year", "Genre", "Total Copies", "Available Copies", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && itemsTable.getSelectedRow() != -1) {
                    int selectedRow = itemsTable.getSelectedRow();
                    isbnField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    titleField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    publishedYearField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    genreField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    totalCopiesField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    availableCopiesField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                    typeField.setText(tableModel.getValueAt(selectedRow, 6).toString());
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        add(scrollPane, BorderLayout.CENTER);
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        addButton.addActionListener(this::addItem);
        updateButton.addActionListener(this::updateItem);
        deleteButton.addActionListener(this::deleteItem);
        clearButton.addActionListener(e -> clearFields());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadItems() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Item");
             ResultSet rs = pstmt.executeQuery()) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("ISBN"));
                row.add(rs.getString("Title"));
                row.add(rs.getInt("PublishedYear"));
                row.add(rs.getString("Genre"));
                row.add(rs.getInt("TotalCopies"));
                row.add(rs.getInt("AvailableCopies"));
                row.add(rs.getString("Type"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItem(ActionEvent e) {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        int publishedYear = parseIntField(publishedYearField.getText(), "Published Year");
        if (publishedYear == -1) return;
        
        String genre = genreField.getText().trim();
        int totalCopies = parseIntField(totalCopiesField.getText(), "Total Copies");
        if (totalCopies == -1) return;
        
        int availableCopies = parseIntField(availableCopiesField.getText(), "Available Copies");
        if (availableCopies == -1) return;
        
        String type = typeField.getText().trim();

        String sql = "INSERT INTO Item (ISBN, Title, PublishedYear, Genre, TotalCopies, AvailableCopies, Type) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, title);
            pstmt.setInt(3, publishedYear);
            pstmt.setString(4, genre);
            pstmt.setInt(5, totalCopies);
            pstmt.setInt(6, availableCopies);
            pstmt.setString(7, type);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, "Unable to add item.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Item added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadItems();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("PK_Item")) {
                JOptionPane.showMessageDialog(this, "An item with this ISBN already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("CK_Item_TotalCopies")) {
                JOptionPane.showMessageDialog(this, "Total Copies must be non-negative.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("CK_Item_AvailableCopies")) {
                JOptionPane.showMessageDialog(this, "Available Copies must be non-negative and less than or equal to Total Copies.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        int publishedYear = parseIntField(publishedYearField.getText(), "Published Year");
        if (publishedYear == -1) return;
        
        String genre = genreField.getText().trim();
        int totalCopies = parseIntField(totalCopiesField.getText(), "Total Copies");
        if (totalCopies == -1) return;
        
        int availableCopies = parseIntField(availableCopiesField.getText(), "Available Copies");
        if (availableCopies == -1) return;
        
        String type = typeField.getText().trim();

        String sql = "UPDATE Item SET Title = ?, PublishedYear = ?, Genre = ?, TotalCopies = ?, AvailableCopies = ?, Type = ? WHERE ISBN = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, publishedYear);
            pstmt.setString(3, genre);
            pstmt.setInt(4, totalCopies);
            pstmt.setInt(5, availableCopies);
            pstmt.setString(6, type);
            pstmt.setString(7, isbn);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(this, "Unable to update item.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Item updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadItems();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("CK_Item_TotalCopies")) {
                JOptionPane.showMessageDialog(this, "Total Copies must be non-negative.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("CK_Item_AvailableCopies")) {
                JOptionPane.showMessageDialog(this, "Available Copies must be non-negative and less than or equal to Total Copies.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String isbn = tableModel.getValueAt(selectedRow, 0).toString();
        String sql = "DELETE FROM Item WHERE ISBN = ?";

        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this item?", "Delete Item", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, isbn);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    JOptionPane.showMessageDialog(this, "Unable to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadItems();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        isbnField.setText("");
        titleField.setText("");
        publishedYearField.setText("");
        genreField.setText("");
        totalCopiesField.setText("");
        availableCopiesField.setText("");
        typeField.setText("");
        itemsTable.clearSelection();
        suggestNextISBN(); // Suggest the next ISBN after clearing fields
    }

    private void suggestNextISBN() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(ISBN) AS max_isbn FROM Item")) {
            if (rs.next()) {
                String maxISBN = rs.getString("max_isbn");
                try {
                    long nextISBN = Long.parseLong(maxISBN) + 1;
                    isbnField.setText(String.valueOf(nextISBN));
                } catch (NumberFormatException ex) {
                    isbnField.setText("");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error suggesting next ISBN: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseIntField(String text, String fieldName) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid " + fieldName + ": must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ItemForm().setVisible(true));
    }
}

