
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class LibraryItemForm extends JFrame {
    private JComboBox<String> branchComboBox, itemComboBox;
    private Vector<LibraryBranch> branches;
    private Vector<Item> items;
    private DefaultTableModel model;
    private JTable libraryItemsTable;

    public LibraryItemForm() {
        initUI();
        fetchLibraryBranches();
        fetchItems();
    }

    private void initUI() {
        setTitle("Library Item Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        branchComboBox = new JComboBox<>();
        itemComboBox = new JComboBox<>();

        topPanel.add(new JLabel("Library Branch:"));
        topPanel.add(branchComboBox);
        topPanel.add(new JLabel("Item (Book):"));
        topPanel.add(itemComboBox);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> addLibraryItem());
        updateButton.addActionListener(e -> updateLibraryItem());
        deleteButton.addActionListener(e -> deleteLibraryItem());
        clearButton.addActionListener(e -> clearFields());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        String[] columns = new String[]{"Branch ID", "Branch Name", "ISBN", "Title"};
        model = new DefaultTableModel(columns, 0);
        libraryItemsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(libraryItemsTable);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        viewAllLibraryItems(); // Initially load all library items
    }

    private void fetchLibraryBranches() {
        // DatabaseConnection.connect() correctly establishes a database connection
        String sql = "SELECT BranchID, Name FROM LibraryBranch";
        branches = new Vector<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                branches.add(new LibraryBranch(rs.getInt("BranchID"), rs.getString("Name")));
                branchComboBox.addItem(rs.getString("Name"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching library branches: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchItems() {
        String sql = "SELECT ISBN, Title FROM Item";
        items = new Vector<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(new Item(rs.getString("ISBN"), rs.getString("Title")));
                itemComboBox.addItem(rs.getString("Title"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addLibraryItem() {
        if (branchComboBox.getSelectedIndex() != -1 && itemComboBox.getSelectedIndex() != -1) {
            int branchIndex = branchComboBox.getSelectedIndex();
            int itemIndex = itemComboBox.getSelectedIndex();

            LibraryBranch selectedBranch = branches.get(branchIndex);
            Item selectedItem = items.get(itemIndex);

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO LibraryItem (BranchID, ISBN) VALUES (?, ?)")) {
                pstmt.setInt(1, selectedBranch.branchID);
                pstmt.setString(2, selectedItem.ISBN);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Library item added successfully!");
                    viewAllLibraryItems(); // Refresh table view
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add library item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving library item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select both a branch and an item.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateLibraryItem() {
        int selectedRow = libraryItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "No Item Selected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Branch ID and ISBN are in the first and third column respectively
        int branchID = (int) model.getValueAt(selectedRow, 0);
        String ISBN = (String) model.getValueAt(selectedRow, 2);

        // Placeholder for new Branch ID and ISBN, in a real scenario, gather these from a user input form
        int newBranchID = branchID; // This should be updated based on user input
        String newISBN = ISBN; // This should be updated based on user input

        String sql = "UPDATE LibraryItem SET BranchID = ?, ISBN = ? WHERE BranchID = ? AND ISBN = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newBranchID);
            pstmt.setString(2, newISBN);
            pstmt.setInt(3, branchID);
            pstmt.setString(4, ISBN);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Library item updated successfully!");
                viewAllLibraryItems(); // Refresh the table view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update the library item.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating library item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteLibraryItem() {
        int selectedRow = libraryItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Item Selected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int branchID = (int) model.getValueAt(selectedRow, 0);
        String ISBN = (String) model.getValueAt(selectedRow, 2);

        String sql = "DELETE FROM LibraryItem WHERE BranchID = ? AND ISBN = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchID);
            pstmt.setString(2, ISBN);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Library item deleted successfully!");
                viewAllLibraryItems(); // Refresh the table view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the library item.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting library item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void clearFields() {
        branchComboBox.setSelectedIndex(-1);
        itemComboBox.setSelectedIndex(-1);
    }

    private void viewAllLibraryItems() {
        // Clear existing table data
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT li.BranchID, lb.Name as BranchName, li.ISBN, i.Title FROM LibraryItem li JOIN LibraryBranch lb ON li.BranchID = lb.BranchID JOIN Item i ON li.ISBN = i.ISBN");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("BranchID"),
                    rs.getString("BranchName"),
                    rs.getString("ISBN"),
                    rs.getString("Title")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching library items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Utility classes
    private class LibraryBranch {
        int branchID;
        String name;

        LibraryBranch(int branchID, String name) {
            this.branchID = branchID;
            this.name = name;
        }
    }

    private class Item {
        String ISBN;
        String title;

        Item(String ISBN, String title) {
            this.ISBN = ISBN;
            this.title = title;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryItemForm().setVisible(true));
    }
}
