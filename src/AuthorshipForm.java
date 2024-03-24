
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorshipForm extends JFrame {
    private JComboBox<Author> authorComboBox;
    private JComboBox<Item> itemComboBox;
    private DefaultListModel<Authorship> authorshipListModel;
    private JList<Authorship> authorshipList;

    public AuthorshipForm() {
        initUI();
        fetchAuthors();
        fetchItems();
        fetchAuthorships();
    }

    private void initUI() {
        setTitle("Manage Authorships");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(700, 450);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        authorComboBox = new JComboBox<>();
        itemComboBox = new JComboBox<>();
        topPanel.add(new JLabel("Author:"));
        topPanel.add(authorComboBox);
        topPanel.add(new JLabel("Item (Book):"));
        topPanel.add(itemComboBox);
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> saveAuthorship());
        topPanel.add(addButton);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());
        topPanel.add(clearButton);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        authorshipListModel = new DefaultListModel<>();
        authorshipList = new JList<>(authorshipListModel);
        JScrollPane listScrollPane = new JScrollPane(authorshipList);
        centerPanel.add(listScrollPane);
        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteAuthorship());
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateAuthorship());
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void fetchAuthors() {
        String sql = "SELECT AuthorID, FirstName, LastName FROM Author";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Author author = new Author(rs.getInt("AuthorID"), rs.getString("FirstName"), rs.getString("LastName"));
                authorComboBox.addItem(author);
            }
        } catch (SQLException ex) {
            handleSQLException("Error fetching authors: " + ex.getMessage());
        }
    }

    private void fetchItems() {
        String sql = "SELECT ISBN, Title FROM Item";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Item item = new Item(rs.getString("ISBN"), rs.getString("Title"));
                itemComboBox.addItem(item);
            }
        } catch (SQLException ex) {
            handleSQLException("Error fetching items: " + ex.getMessage());
        }
    }

    private void fetchAuthorships() {
        String sql = "SELECT AuthorID, ISBN FROM Authorship";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            authorshipListModel.clear();
            while (rs.next()) {
                Authorship authorship = new Authorship(rs.getInt("AuthorID"), rs.getString("ISBN"));
                authorshipListModel.addElement(authorship);
            }
        } catch (SQLException ex) {
            handleSQLException("Error fetching authorships: " + ex.getMessage());
        }
    }

    private void saveAuthorship() {
        Author selectedAuthor = (Author) authorComboBox.getSelectedItem();
        Item selectedItem = (Item) itemComboBox.getSelectedItem();
        if (selectedAuthor != null && selectedItem != null) {
            String sql = "INSERT INTO Authorship (AuthorID, ISBN) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedAuthor.getAuthorID());
                pstmt.setString(2, selectedItem.getISBN());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Authorship added successfully!");
                fetchAuthorships();
            } catch (SQLException ex) {
                handleSQLException("Error saving authorship: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select both an author and an item.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAuthorship() {
        Authorship selectedAuthorship = authorshipList.getSelectedValue();
        if (selectedAuthorship != null) {
            String sql = "DELETE FROM Authorship WHERE AuthorID = ? AND ISBN = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedAuthorship.getAuthorID());
                pstmt.setString(2, selectedAuthorship.getISBN());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Authorship deleted successfully!");
                fetchAuthorships();
            } catch (SQLException ex) {
                handleSQLException("Error deleting authorship: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an authorship to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAuthorship() {
        Authorship selectedAuthorship = authorshipList.getSelectedValue();
        if (selectedAuthorship != null) {
            // Open a dialog or input form to allow users to modify the selected authorship
            Authorship updatedAuthorship = showUpdateDialog(selectedAuthorship);
            if (updatedAuthorship != null) {
                String sql = "UPDATE Authorship SET AuthorID = ?, ISBN = ? WHERE AuthorID = ? AND ISBN = ?";
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, updatedAuthorship.getAuthorID());
                    pstmt.setString(2, updatedAuthorship.getISBN());
                    pstmt.setInt(3, selectedAuthorship.getAuthorID());
                    pstmt.setString(4, selectedAuthorship.getISBN());
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Authorship updated successfully!");
                    fetchAuthorships();
                } catch (SQLException ex) {
                    handleSQLException("Error updating authorship: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an authorship to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Authorship showUpdateDialog(Authorship authorship) {
        // You can implement a custom dialog or input form to allow users to modify authorship details
        // For simplicity, let's use JOptionPane to input new AuthorID and ISBN
        JTextField authorIDField = new JTextField(String.valueOf(authorship.getAuthorID()));
        JTextField ISBNField = new JTextField(authorship.getISBN());
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Author ID:"));
        panel.add(authorIDField);
        panel.add(new JLabel("ISBN:"));
        panel.add(ISBNField);
        int result = JOptionPane.showConfirmDialog(null, panel, "Update Authorship", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int updatedAuthorID;
            try {
                updatedAuthorID = Integer.parseInt(authorIDField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Author ID", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            String updatedISBN = ISBNField.getText();
            return new Authorship(updatedAuthorID, updatedISBN);
        }
        return null;
    }

    private void clearFields() {
        authorComboBox.setSelectedIndex(0);
        itemComboBox.setSelectedIndex(0);
    }

    private void handleSQLException(String message) {
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthorshipForm().setVisible(true));
    }

    static class Author {
        private int authorID;
        private String firstName, lastName;

        public Author(int authorID, String firstName, String lastName) {
            this.authorID = authorID;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public int getAuthorID() {
            return authorID;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }
    }

    static class Item {
        private String ISBN, title;

        public Item(String ISBN, String title) {
            this.ISBN = ISBN;
            this.title = title;
        }

        public String getISBN() {
            return ISBN;
        }

        @Override
        public String toString() {
            return title + " (" + ISBN + ")";
        }
    }

    static class Authorship {
        private int authorID;
        private String ISBN;

        public Authorship(int authorID, String ISBN) {
            this.authorID = authorID;
            this.ISBN = ISBN;
        }

        public int getAuthorID() {
            return authorID;
        }

        public String getISBN() {
            return ISBN;
        }

        @Override
        public String toString() {
            return "Author ID: " + authorID + ", ISBN: " + ISBN;
        }
    }
}
