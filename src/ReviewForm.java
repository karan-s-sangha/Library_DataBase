import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ReviewForm extends JFrame {
    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    private JTextField isbnField, customerIDField, ratingField, commentField, reviewDateField, reviewTimeField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedReviewId = -1;

    public ReviewForm() {
        setTitle("Review Management");
        setSize(800, 400);
        initializeUI();
        loadReviews();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ReviewID", "ISBN", "Customer ID", "Rating", "Comment", "Review Date", "Review Time"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reviewsTable = new JTable(tableModel);
        reviewsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reviewsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && reviewsTable.getSelectedRow() != -1) {
                selectedReviewId = (int) reviewsTable.getValueAt(reviewsTable.getSelectedRow(), 0);
                updateFormFields(reviewsTable.getSelectedRow());
            }
        });
        JScrollPane scrollPane = new JScrollPane(reviewsTable);
        add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        isbnField = new JTextField();
        customerIDField = new JTextField();
        ratingField = new JTextField();
        commentField = new JTextField();
        reviewDateField = new JTextField(LocalDate.now().toString());
        reviewTimeField = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        inputPanel.add(new JLabel("ISBN:"));
        inputPanel.add(isbnField);
        inputPanel.add(new JLabel("Customer ID:"));
        inputPanel.add(customerIDField);
        inputPanel.add(new JLabel("Rating (0.0 - 5.0):"));
        inputPanel.add(ratingField);
        inputPanel.add(new JLabel("Comment:"));
        inputPanel.add(commentField);
        inputPanel.add(new JLabel("Review Date (YYYY-MM-DD):"));
        inputPanel.add(reviewDateField);
        inputPanel.add(new JLabel("Review Time (HH:MM:SS):"));
        inputPanel.add(reviewTimeField);
        add(inputPanel, BorderLayout.NORTH);
        addButton = new JButton("Add");
        addButton.addActionListener(this::addReview);
        updateButton = new JButton("Update");
        updateButton.addActionListener(this::updateReview);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this::deleteReview);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReviews() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT ReviewID, ISBN, CustomerID, Rating, Comment, ReviewDate, ReviewTime FROM Review ORDER BY ReviewID ASC");
             ResultSet rs = pstmt.executeQuery()) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("ReviewID"),
                        rs.getString("ISBN"),
                        rs.getInt("CustomerID"),
                        rs.getDouble("Rating"),
                        rs.getString("Comment"),
                        rs.getString("ReviewDate"), // No conversion needed for date
                        rs.getString("ReviewTime")  // No conversion needed for time
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading reviews: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addReview(ActionEvent e) {
        String isbn = isbnField.getText();
        String customerID = customerIDField.getText();
        String rating = ratingField.getText();
        String comment = commentField.getText();
        String reviewDate = reviewDateField.getText();
        String reviewTime = reviewTimeField.getText();

        try {
            LocalDate.parse(reviewDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime.parse(reviewTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
            double ratingValue = Double.parseDouble(rating);
            if (ratingValue < 0.0 || ratingValue > 5.0) {
                JOptionPane.showMessageDialog(this, "Rating must be between 0.0 and 5.0.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Review (ISBN, CustomerID, Rating, Comment, ReviewDate, ReviewTime) VALUES (?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, isbn);
                pstmt.setInt(2, Integer.parseInt(customerID));
                pstmt.setDouble(3, ratingValue);
                pstmt.setString(4, comment);
                pstmt.setString(5, reviewDate); // No conversion needed for date
                pstmt.setString(6, reviewTime); // No conversion needed for time

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Review added successfully!");
                clearForm();
                loadReviews();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please ensure all fields are correctly filled: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReview(ActionEvent e) {
        if (selectedReviewId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a review to update.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate.parse(reviewDateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime.parse(reviewTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            double ratingValue = Double.parseDouble(ratingField.getText());
            if (ratingValue < 0.0 || ratingValue > 5.0) {
                JOptionPane.showMessageDialog(this, "Rating must be between 0.0 and 5.0.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE Review SET ISBN = ?, CustomerID = ?, Rating = ?, Comment = ?, ReviewDate = ?, ReviewTime = ? WHERE ReviewID = ?")) {
                pstmt.setString(1, isbnField.getText());
                pstmt.setInt(2, Integer.parseInt(customerIDField.getText()));
                pstmt.setDouble(3, ratingValue);
                pstmt.setString(4, commentField.getText());
                pstmt.setString(5, reviewDateField.getText()); // No conversion needed for date
                pstmt.setString(6, reviewTimeField.getText()); // No conversion needed for time
                pstmt.setInt(7, selectedReviewId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Review updated successfully!");
                clearForm();
                loadReviews();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please ensure all fields are correctly filled: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReview(ActionEvent e) {
        if (selectedReviewId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a review to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this review?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Review WHERE ReviewID = ?")) {
                pstmt.setInt(1, selectedReviewId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Review deleted successfully!");
                clearForm();
                loadReviews();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateFormFields(int selectedRow) {
        isbnField.setText((String) reviewsTable.getValueAt(selectedRow, 1));
        customerIDField.setText(reviewsTable.getValueAt(selectedRow, 2).toString());
        ratingField.setText(reviewsTable.getValueAt(selectedRow, 3).toString());
        commentField.setText((String) reviewsTable.getValueAt(selectedRow, 4));
        reviewDateField.setText(reviewsTable.getValueAt(selectedRow, 5).toString());
        reviewTimeField.setText(reviewsTable.getValueAt(selectedRow, 6).toString());
    }

    private void clearForm() {
        isbnField.setText("");
        customerIDField.setText("");
        ratingField.setText("");
        commentField.setText("");
        reviewDateField.setText(LocalDate.now().toString());
        reviewTimeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        reviewsTable.clearSelection();
        selectedReviewId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReviewForm::new);
    }
}

