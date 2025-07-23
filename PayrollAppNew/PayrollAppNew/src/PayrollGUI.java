// PayrollGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PayrollGUI extends JFrame {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextArea resultArea;
    private final String CSV_FILE = "employees.csv";
    private JButton updateButton;
    private JButton deleteButton;
    private JButton changePasswordButton;
    private Employee currentEmployee = null;

    public PayrollGUI() {
        LoginSystem loginSystem = new LoginSystem();
        if (!loginSystem.authenticateUser()) {
            System.exit(0); // Exit if not authenticated
        }
        initializeUI();
        setupComponents();
        checkCSVExists();

  setVisible(true);
    }


    private void initializeUI() {
        setTitle("Payroll System - Employee Management");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Search Panel
        JPanel searchPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Employee Search"));
        lastNameField = new JTextField();
        firstNameField = new JTextField();
        searchPanel.add(new JLabel("Last Name:"));
        searchPanel.add(lastNameField);
        searchPanel.add(new JLabel("First Name:"));
        searchPanel.add(firstNameField);

        // Action Buttons - Added Change Password button
        JButton searchButton = new JButton("Generate Payslip");
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(this::performSearch);

        JButton addButton = new JButton("Add New Employee");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> new NewEmployeeForm(this, CSV_FILE).setVisible(true));
        
        updateButton = new JButton("Update Employee");
        updateButton.setBackground(new Color(255, 140, 0));
        updateButton.setForeground(Color.WHITE);
        updateButton.setEnabled(false);
        updateButton.addActionListener(e -> updateEmployee());
        
        deleteButton = new JButton("Delete Employee");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteEmployee());

        // Change Password Button
        changePasswordButton = new JButton("Change Password");
        changePasswordButton.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        changePasswordButton.setForeground(Color.WHITE);
        changePasswordButton.addActionListener(e -> showPasswordChangeDialog());

        // Button Panel - Added changePasswordButton
        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topButtonsPanel.add(addButton);
        topButtonsPanel.add(searchButton);
        topButtonsPanel.add(updateButton);
        topButtonsPanel.add(deleteButton);
        topButtonsPanel.add(changePasswordButton); // NEW BUTTON

        // Rest of the UI setup
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(topButtonsPanel, BorderLayout.NORTH);
        inputPanel.add(searchPanel, BorderLayout.CENTER);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Payroll Details"));

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    //Password Change Dialog
    private void showPasswordChangeDialog() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPasswordField currentPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        dialog.add(new JLabel("Current Password:"));
        dialog.add(currentPassField);
        dialog.add(new JLabel("New Password:"));
        dialog.add(newPassField);
        dialog.add(new JLabel("Confirm Password:"));
        dialog.add(confirmPassField);

        JButton submitButton = new JButton("Change Password");
        submitButton.addActionListener(e -> {
            try {
                String currentPass = new String(currentPassField.getPassword());
                String newPass = new String(newPassField.getPassword());
                String confirmPass = new String(confirmPassField.getPassword());

                // Validation
                if (!newPass.equals(confirmPass)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "New passwords don't match!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (newPass.length() < 8) {
                    JOptionPane.showMessageDialog(dialog,
                        "Password must be at least 8 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Verify current password
                if (!CredentialManager.validateCredentials(
                    LoginSystem.getCurrentUsername(), currentPass)) {
                    JOptionPane.showMessageDialog(dialog,
                        "Current password is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update password
                CredentialManager.resetPassword(
                    LoginSystem.getCurrentEmployeeId(), 
                    newPass
                );
                
                JOptionPane.showMessageDialog(dialog,
                    "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error changing password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel()); // spacer
        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    // Rest of your existing methods remain unchanged:
    private void checkCSVExists() { /* ... */ }
    private void performSearch(ActionEvent e) {
        try {
            String lastName = lastNameField.getText().trim();
            String firstName = firstNameField.getText().trim();

            var employees = EmployeeCSVReader.readEmployees(CSV_FILE);

            for (Employee emp : employees) {
                Map<String, String> details = emp.getDetails();
                if (details.get("Last Name").equalsIgnoreCase(lastName)
                        && details.get("First Name").equalsIgnoreCase(firstName)) {

                    currentEmployee = emp;
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);

                    // Format salary values by removing commas
                    String basicSalary = details.get("Basic Salary").replace(",", "");
                    String riceSubsidy = details.get("Rice Subsidy").replace(",", "");
                    String phoneAllowance = details.get("Phone Allowance").replace(",", "");
                    String clothingAllowance = details.get("Clothing Allowance").replace(",", "");

                    // Calculate and display payroll
                    double grossSalary = Double.parseDouble(basicSalary)
                            + Double.parseDouble(riceSubsidy)
                            + Double.parseDouble(phoneAllowance)
                            + Double.parseDouble(clothingAllowance);

                    resultArea.setText(String.format(
                            "Employee: %s, %s\nPosition: %s\n\n"
                            + "Basic Salary: %s\nRice Subsidy: %s\nPhone Allowance: %s\n"
                            + "Clothing Allowance: %s\n\nGross Salary: ₱%,.2f",
                            details.get("Last Name"), details.get("First Name"),
                            details.get("Position"),
                            details.get("Basic Salary"), details.get("Rice Subsidy"),
                            details.get("Phone Allowance"), details.get("Clothing Allowance"),
                            grossSalary
                    ));
                    return;
                }
            }

            resultArea.setText("Employee not found");
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } catch (Exception ex) {
            showError("Search Error", ex.getMessage());
        }
    }  


    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new PayrollGUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateEmployee() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void deleteEmployee() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}  

