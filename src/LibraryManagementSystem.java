
import javax.swing.*;
import java.awt.*;

import java.sql.*;
import java.text.SimpleDateFormat;

public class LibraryManagementSystem extends JFrame {

    private Connection connection;
     // Original color variables
     static Color backgroundColor;
     static Color buttonColor;
     static Color headingColor;

    public LibraryManagementSystem() {
        initializeUI();
        connection = DatabaseConnection.connect();
    }

    private void initializeUI() {
        setTitle("Library Management System");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

    
        backgroundColor = new Color(240, 240, 240); // Light gray
        buttonColor = new Color(210, 210, 210); // Medium gray
        headingColor = new Color(100, 100, 100); // Dark gray
        
        
        // Adding a heading to the UI
        JLabel headingLabel = new JLabel("Library Management System Dashboard", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headingLabel.setOpaque(true);
        headingLabel.setBackground(headingColor);
        getContentPane().add(headingLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(backgroundColor);

        // Management Section
        JPanel managementPanel = new JPanel();
        managementPanel.setLayout(new BoxLayout(managementPanel, BoxLayout.Y_AXIS));
        managementPanel.setBackground(backgroundColor);
        addButtonsToManagementPanel(managementPanel, buttonColor);
        tabbedPane.addTab("Management", new JScrollPane(managementPanel));

        // Queries Section
        JPanel queriesPanel = new JPanel();
        queriesPanel.setLayout(new BoxLayout(queriesPanel, BoxLayout.Y_AXIS));
        queriesPanel.setBackground(backgroundColor);
        addButtonsToQueriesPanel(queriesPanel, buttonColor);
        tabbedPane.addTab("Queries", new JScrollPane(queriesPanel));

        // User Actions Section
        JPanel userActionsPanel = new JPanel();
        userActionsPanel.setLayout(new BoxLayout(userActionsPanel, BoxLayout.Y_AXIS));
        userActionsPanel.setBackground(backgroundColor);
        addButtonsToUserActionsPanel(userActionsPanel, buttonColor);
        tabbedPane.addTab("User Actions", new JScrollPane(userActionsPanel));

        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void addButtonsToManagementPanel(JPanel panel, Color buttonColor) {
        JButton[] buttons = {
            createButton("Manage Membership Plans", () -> new MembershipPlanForm().setVisible(true), buttonColor),
            createButton("Manage Customers", () -> new CustomerForm().setVisible(true), buttonColor),
            createButton("Manage Author", () -> new AuthorForm().setVisible(true), buttonColor),
            createButton("Manage Item", () -> new ItemForm().setVisible(true), buttonColor),
            createButton("Manage Authorship", () -> new AuthorshipForm().setVisible(true), buttonColor),
            createButton("Manage Borrowed Item", () -> new BorrowedItemForm().setVisible(true), buttonColor),
            createButton("Manage Fine", () -> new FineForm().setVisible(true), buttonColor),
            createButton("Manage Review", () -> new ReviewForm().setVisible(true), buttonColor),
            createButton("Manage Reservation", () -> new ReservationForm().setVisible(true), buttonColor),
            createButton("Manage Notification", () -> new NotificationForm().setVisible(true), buttonColor),
            createButton("Manage Maintenance Record", () -> new MaintenanceRecordForm().setVisible(true), buttonColor),
            createButton("Manage Library Branch", () -> new LibraryBranchForm().setVisible(true), buttonColor),
            createButton("Manage Library Item", () -> new LibraryItemForm().setVisible(true), buttonColor),
            createButton("Manage User History", () -> new UsersHistoryForm().setVisible(true), buttonColor),
            createButton("Manage Staff", () -> new StaffForm().setVisible(true), buttonColor),
            createButton("Manage Customer Notified", () -> new CustomerNotifiedForm().setVisible(true), buttonColor),
        };

        for (JButton button : buttons) {
            panel.add(button);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    private void addButtonsToQueriesPanel(JPanel panel, Color buttonColor) {
        JButton[] buttons = {
            createButton("Query 1: Mystery Books Rated Above 4.5 and Available", this::executeQuery1, buttonColor),
            createButton("Query 2: All Science Fiction Books Published After 2010 With 'Space' in the Title", this::executeQuery2, buttonColor),
            createButton("Query 3: Books With a Rating Above 4.5 and Borrowed More Than 10 Times", this::executeQuery3, buttonColor),
            createButton("Query 4: Find Available eBooks in the Mystery Genre", this::executeQuery4, buttonColor),
            createButton("Query 5: Top Book Borrowed by the Month", this::executeQuery5,buttonColor),
            };
            for (JButton button : buttons) {
                panel.add(button);
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        private void addButtonsToUserActionsPanel(JPanel panel, Color buttonColor) {
            JButton[] buttons = {
                createButton("Register/Sign In", this::registerOrSignIn, buttonColor),
                createButton("View Availability", this::viewAvailability, buttonColor),
                createButton("Borrow Information", this::borrowInformation, buttonColor),
                createButton("Reserve/Hold Books", this::reserveBooks, buttonColor),
                createButton("View Account History", this::viewAccountHistory, buttonColor),
                createButton("Rate and Write Reviews", this::rateWriteReviews, buttonColor),
                createButton("Cancel Reservation", this::cancelReservation, buttonColor),
            };
        
            for (JButton button : buttons) {
                panel.add(button);
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        private JButton createButton(String text, Runnable action, Color buttonColor) {
            JButton button = new JButton(text);
            button.addActionListener(e -> action.run());
            button.setBackground(buttonColor);
            return button;
        }

    
    private void executeQuery1() {
        try {
            String sql = "SELECT i.Title, i.Genre, r.Rating " +
                    "FROM Item i " +
                    "JOIN Review r ON i.ISBN = r.ISBN " +
                    "WHERE r.Rating > 4.5 AND i.AvailableCopies > 0";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("Title");
                String genre = rs.getString("Genre");
                double rating = rs.getDouble("Rating");
                result.append("Title: ").append(title).append(", Genre: ").append(genre).append(", Rating: ").append(rating).append("\n");
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No books found with the specified criteria.", "Query 1 Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.toString(), "Query 1 Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeQuery2() {
        try {
            String sql = "SELECT Title, PublishedYear " +
                    "FROM Item " +
                    "WHERE Genre = 'Science Fiction' AND PublishedYear > 2010 AND Title LIKE '%space%'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("Title");
                int publishedYear = rs.getInt("PublishedYear");
                result.append("Title: ").append(title).append(", Published Year: ").append(publishedYear).append("\n");
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No books found with the specified criteria.", "Query 2 Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.toString(), "Query 2 Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeQuery3() {
        try {
            String sql = "SELECT i.Title, AVG(r.Rating) AS AverageRating, COUNT(bi.ISBN) AS TimesBorrowed " +
                    "FROM Item i " +
                    "JOIN Review r ON i.ISBN = r.ISBN " +
                    "JOIN BorrowedItem bi ON i.ISBN = bi.ISBN " +
                    "GROUP BY i.Title " +
                    "HAVING AVG(r.Rating) > 4.5 AND COUNT(bi.ISBN) > 10";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("Title");
                double avgRating = rs.getDouble("AverageRating");
                int timesBorrowed = rs.getInt("TimesBorrowed");
                result.append("Title: ").append(title).append(", Average Rating: ").append(avgRating).append(", Times Borrowed: ").append(timesBorrowed).append("\n");
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No books found with the specified criteria.", "Query 3 Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.toString(), "Query 3 Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeQuery4() {
        try {
            String sql = "SELECT Title, Genre, Type " +
                    "FROM Item " +
                    "WHERE Genre = 'Mystery' AND Type = 'eBook' AND AvailableCopies > 0";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("Title");
                String genre = rs.getString("Genre");
                String type = rs.getString("Type");
                result.append("Title: ").append(title).append(", Genre: ").append(genre).append(", Type: ").append(type).append("\n");
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No books found with the specified criteria.", "Query 4 Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.toString(), "Query 4 Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeQuery5() {
        try {
            String sql = "SELECT i.Title, COUNT(bi.ISBN) AS TimesBorrowed " +
                    "FROM BorrowedItem bi " +
                    "JOIN Item i ON bi.ISBN = i.ISBN " +
                    "WHERE strftime('%m', bi.BorrowDate) = strftime('%m', 'now') " +
                    "GROUP BY i.Title " +
                    "ORDER BY TimesBorrowed DESC LIMIT 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("Title");
                int timesBorrowed = rs.getInt("TimesBorrowed");
                result.append("Title: ").append(title).append(", Times Borrowed: ").append(timesBorrowed).append("\n");
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No books found with the specified criteria.", "Query 5 Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.toString(), "Query 5 Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void registerOrSignIn() {
    JTextField customerIDField = new JTextField();
    JTextField firstNameField = new JTextField();
    JTextField lastNameField = new JTextField();
    JTextField addressField = new JTextField();
    JTextField emailField = new JTextField();
    JTextField phoneNumberField = new JTextField();
    JTextField planIDField = new JTextField();

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Customer ID:"));
    panel.add(customerIDField);
    panel.add(new JLabel("First Name:"));
    panel.add(firstNameField);
    panel.add(new JLabel("Last Name:"));
    panel.add(lastNameField);
    panel.add(new JLabel("Address:"));
    panel.add(addressField);
    panel.add(new JLabel("Email:"));
    panel.add(emailField);
    panel.add(new JLabel("Phone Number:"));
    panel.add(phoneNumberField);
    panel.add(new JLabel("Plan ID:"));
    panel.add(planIDField);

    int result = JOptionPane.showConfirmDialog(null, panel, "Register/Sign In", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Customer (CustomerID, FirstName, LastName, Address, Email, PhoneNumber, RegistrationDate, PlanID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, Integer.parseInt(customerIDField.getText()));
            pstmt.setString(2, firstNameField.getText());
            pstmt.setString(3, lastNameField.getText());
            pstmt.setString(4, addressField.getText());
            pstmt.setString(5, emailField.getText());
            pstmt.setString(6, phoneNumberField.getText());
            pstmt.setString(7, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())); // Date
            pstmt.setInt(8, Integer.parseInt(planIDField.getText()));
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Registration successful!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void viewAvailability() {
        try {
            String sql = "SELECT Title, Genre, AvailableCopies FROM Item WHERE AvailableCopies > 0";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String title = rs.getString("Title");
                String genre = rs.getString("Genre");
                int availableCopies = rs.getInt("AvailableCopies");
                result.append("Title: ").append(title).append(", Genre: ").append(genre).append(", Available Copies: ").append(availableCopies).append("\n");
            }
            JOptionPane.showMessageDialog(this, result.toString(), "Available Items", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void borrowInformation() {
        JTextField isbnField = new JTextField();
    
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("ISBN:"));
        panel.add(isbnField);
    
        int result = JOptionPane.showConfirmDialog(null, panel, "Borrow Information", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String isbn = isbnField.getText();
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT i.Title, bi.BorrowDate, bi.ReturnDate FROM BorrowedItem bi JOIN Item i ON bi.ISBN = i.ISBN WHERE i.ISBN = ?")) {
                pstmt.setString(1, isbn);
                ResultSet rs = pstmt.executeQuery();
                StringBuilder resultText = new StringBuilder();
                while (rs.next()) {
                    String title = rs.getString("Title");
                    String borrowDate = rs.getString("BorrowDate"); 
                    String returnDate = rs.getString("ReturnDate") != null ? rs.getString("ReturnDate") : "Not Returned";
    
                    // If database stores the full datetime and you only want to display the date part:
                    // It's safe to substring the ISO8601 datetime format to get just the date part.
                    borrowDate = borrowDate != null ? borrowDate.substring(0, 10) : "Unknown";
                    returnDate = !returnDate.equals("Not Returned") ? returnDate.substring(0, 10) : "Not Returned";
    
                    resultText.append("Title: ").append(title).append(", Borrow Date: ").append(borrowDate).append(", Return Date: ").append(returnDate).append("\n");
                }
                JOptionPane.showMessageDialog(null, resultText.toString(), "Borrow Information", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    public void reserveBooks() {
    JTextField customerIDField = new JTextField();
    JTextField isbnField = new JTextField();

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Customer ID:"));
    panel.add(customerIDField);
    panel.add(new JLabel("ISBN:"));
    panel.add(isbnField);

    int result = JOptionPane.showConfirmDialog(null, panel, "Reserve/Hold Books", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String customerID = customerIDField.getText();
        String isbn = isbnField.getText();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Reservation (CustomerID, ISBN, ReservationDate, ReservationStatus) VALUES (?, ?, ?, ?)")) {
            pstmt.setInt(1, Integer.parseInt(customerID));
            pstmt.setString(2, isbn);
            pstmt.setString(3, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())); // Date
            pstmt.setString(4, "Reserved");
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Book reserved successfully!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to reserve book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


private void viewAccountHistory() {
    JTextField customerIDField = new JTextField();

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Customer ID:"));
    panel.add(customerIDField);

    int result = JOptionPane.showConfirmDialog(null, panel, "View Account History", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String customerID = customerIDField.getText();
        try (Connection conn = DatabaseConnection.connect(); // Ensure you have a method to get your SQLite connection
             PreparedStatement pstmt = conn.prepareStatement("SELECT i.Title, bi.BorrowDate, bi.ReturnDate FROM BorrowedItem bi JOIN Item i ON bi.ISBN = i.ISBN WHERE bi.CustomerID = ?")) {
            pstmt.setInt(1, Integer.parseInt(customerID));
            ResultSet rs = pstmt.executeQuery();
            StringBuilder resultText = new StringBuilder();
            while (rs.next()) {
                String title = rs.getString("Title");
                // dates are stored as TEXT in the format "YYYY-MM-DD"
                String borrowDate = rs.getString("BorrowDate"); // Directly use the string representation
                String returnDate = rs.getString("ReturnDate") != null ? rs.getString("ReturnDate") : "Not Returned"; // Check for null
                resultText.append("Title: ").append(title).append(", Borrow Date: ").append(borrowDate).append(", Return Date: ").append(returnDate).append("\n");
            }
            JOptionPane.showMessageDialog(null, resultText.toString(), "Account History", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void rateWriteReviews() {
    JTextField isbnField = new JTextField();
    JTextField customerIDField = new JTextField();
    JTextField ratingField = new JTextField();
    JTextField commentField = new JTextField();

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("ISBN:"));
    panel.add(isbnField);
    panel.add(new JLabel("Customer ID:"));
    panel.add(customerIDField);
    panel.add(new JLabel("Rating:"));
    panel.add(ratingField);
    panel.add(new JLabel("Comment:"));
    panel.add(commentField);

    int result = JOptionPane.showConfirmDialog(null, panel, "Rate and Write Reviews", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String isbn = isbnField.getText();
        String customerID = customerIDField.getText();
        String rating = ratingField.getText();
        String comment = commentField.getText();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Review (ISBN, CustomerID, Rating, Comment, ReviewDate, ReviewTime) VALUES (?, ?, ?, ?, date('now'), time('now'))")) {
                pstmt.setString(1, isbn);
                pstmt.setInt(2, Integer.parseInt(customerID));
                pstmt.setDouble(3, Double.parseDouble(rating));
                pstmt.setString(4, comment);
                // The dates are now being handled directly within the SQL statement for SQLite compatibility.
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Review added successfully!");
                } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to add review.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                }
                }


    private void cancelReservation() {
        JTextField reservationIDField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Reservation ID:"));
        panel.add(reservationIDField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Cancel Reservation", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String reservationID = reservationIDField.getText();
            try {
                String sql = "DELETE FROM Reservation WHERE ReservationID = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(reservationID));
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Reservation canceled successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel reservation.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystem ex = new LibraryManagementSystem();
            ex.setVisible(true);
        });
    }

}
