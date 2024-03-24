
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class MembershipPlanForm extends JFrame {
    private JTable plansTable;
    private DefaultTableModel tableModel;
    private JTextField planIDField, nameField, descriptionField, feeField, durationField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private int selectedPlanId = -1;

    public MembershipPlanForm() {
        setTitle("Membership Plan Management");
        setSize(700, 400);
        initializeUI();
        loadPlans();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        String[] columnNames = {"PlanID", "Name", "Description", "Fee", "Duration"};
        tableModel = new DefaultTableModel(columnNames, 0);
        plansTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(plansTable);
        add(scrollPane, BorderLayout.CENTER);

        plansTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && plansTable.getSelectedRow() != -1) {
                int selectedRow = plansTable.getSelectedRow();
                selectedPlanId = Integer.parseInt(plansTable.getValueAt(selectedRow, 0).toString());
                planIDField.setText(plansTable.getValueAt(selectedRow, 0).toString());
                nameField.setText(plansTable.getValueAt(selectedRow, 1).toString());
                descriptionField.setText(plansTable.getValueAt(selectedRow, 2).toString());
                feeField.setText(plansTable.getValueAt(selectedRow, 3).toString());
                durationField.setText(plansTable.getValueAt(selectedRow, 4).toString());
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        planIDField = new JTextField();
        nameField = new JTextField();
        descriptionField = new JTextField();
        feeField = new JTextField();
        durationField = new JTextField();

        inputPanel.add(new JLabel("Plan ID:"));
        inputPanel.add(planIDField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Fee:"));
        inputPanel.add(feeField);
        inputPanel.add(new JLabel("Duration (Days):"));
        inputPanel.add(durationField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        addButton.addActionListener(this::addPlan);
        updateButton.addActionListener(this::updatePlan);
        deleteButton.addActionListener(this::deletePlan);
        clearButton.addActionListener(this::clearFields);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPlans() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM MembershipPlan ORDER BY PlanID ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int maxPlanId = 0;
            while (rs.next()) {
                int planId = rs.getInt("PlanID");
                maxPlanId = Math.max(maxPlanId, planId);
                Object[] row = {
                    planId,
                    rs.getString("Name"),
                    rs.getString("Description"),
                    rs.getDouble("Fee"),
                    rs.getInt("Duration")
                };
                tableModel.addRow(row);
            }
            planIDField.setText(String.valueOf(maxPlanId + 1));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading membership plans: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPlan(ActionEvent e) {
        String sql = "INSERT INTO MembershipPlan (PlanID, Name, Description, Fee, Duration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(planIDField.getText()));
            pstmt.setString(2, nameField.getText());
            pstmt.setString(3, descriptionField.getText());
            pstmt.setDouble(4, Double.parseDouble(feeField.getText()));
            pstmt.setInt(5, Integer.parseInt(durationField.getText()));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Plan added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPlans();
            clearFields(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding plan: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error in number format: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePlan(ActionEvent e) {
        if (selectedPlanId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a plan to update", "Select Plan", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String sql = "UPDATE MembershipPlan SET Name = ?, Description = ?, Fee = ?, Duration = ? WHERE PlanID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setDouble(3, Double.parseDouble(feeField.getText()));
            pstmt.setInt(4, Integer.parseInt(durationField.getText()));
            pstmt.setInt(5, selectedPlanId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Plan updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPlans();
            clearFields(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating plan: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error in number format: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlan(ActionEvent e) {
        if (selectedPlanId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a plan to delete", "Select Plan", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this plan?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM MembershipPlan WHERE PlanID = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedPlanId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Plan deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPlans();
                clearFields(null);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting plan: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields(ActionEvent e) {
        planIDField.setText("");
        nameField.setText("");
        descriptionField.setText("");
        feeField.setText("");
        durationField.setText("");
        selectedPlanId = -1;
        plansTable.clearSelection();
        loadPlans();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MembershipPlanForm().setVisible(true));
    }
}
