// NewEmployeeForm.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map;

public class NewEmployeeForm extends JDialog {
    private final String csvFile;
    private final JTextField lastNameField = new JTextField(20);
    private final JTextField firstNameField = new JTextField(20);
    private final JTextField positionField = new JTextField(20);
    private final JTextField basicSalaryField = new JTextField(20);
    private final JTextField riceSubsidyField = new JTextField(20);
    private final JTextField phoneAllowanceField = new JTextField(20);
    private final JTextField clothingAllowanceField = new JTextField(20);
    private final Employee existingEmployee;

    public NewEmployeeForm(JFrame parent, String csvFile) {
        this(parent, csvFile, null);
    }

    public NewEmployeeForm(JFrame parent, String csvFile, Employee existingEmployee) {
        super(parent, existingEmployee == null ? "Add Employee" : "Update Employee", true);
        this.csvFile = csvFile;
        this.existingEmployee = existingEmployee;
        initializeUI();
    }

    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Position:"));
        formPanel.add(positionField);
        formPanel.add(new JLabel("Basic Salary:"));
        formPanel.add(basicSalaryField);
        formPanel.add(new JLabel("Rice Subsidy:"));
        formPanel.add(riceSubsidyField);
        formPanel.add(new JLabel("Phone Allowance:"));
        formPanel.add(phoneAllowanceField);
        formPanel.add(new JLabel("Clothing Allowance:"));
        formPanel.add(clothingAllowanceField);

        // Pre-fill form for updates
        if (existingEmployee != null) {
            Map<String, String> details = existingEmployee.getDetails();
            lastNameField.setText(details.get("Last Name"));
            firstNameField.setText(details.get("First Name"));
            positionField.setText(details.get("Position"));
            basicSalaryField.setText(details.get("Basic Salary"));
            riceSubsidyField.setText(details.get("Rice Subsidy"));
            phoneAllowanceField.setText(details.get("Phone Allowance"));
            clothingAllowanceField.setText(details.get("Clothing Allowance"));
        }

        // Action Buttons
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(34, 139, 34)); // Forest Green
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveEmployee());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(220, 20, 60)); // Crimson
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void saveEmployee() {
        // Validate required fields
        if (lastNameField.getText().trim().isEmpty() || 
            firstNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Last Name and First Name are required", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate numeric fields
        try {
            Double.parseDouble(basicSalaryField.getText().trim());
            Double.parseDouble(riceSubsidyField.getText().trim());
            Double.parseDouble(phoneAllowanceField.getText().trim());
            Double.parseDouble(clothingAllowanceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "All salary fields must be valid numbers", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> details = new LinkedHashMap<>();
        details.put("Last Name", lastNameField.getText().trim());
        details.put("First Name", firstNameField.getText().trim());
        details.put("Position", positionField.getText().trim());
        details.put("Basic Salary", basicSalaryField.getText().trim());
        details.put("Rice Subsidy", riceSubsidyField.getText().trim());
        details.put("Phone Allowance", phoneAllowanceField.getText().trim());
        details.put("Clothing Allowance", clothingAllowanceField.getText().trim());

        try {
            List<Employee> employees = EmployeeCSVReader.readEmployees(csvFile);
            
            // Remove existing record if updating
            if (existingEmployee != null) {
                Iterator<Employee> it = employees.iterator();
                while (it.hasNext()) {
                    Employee emp = it.next();
                    Map<String, String> empDetails = emp.getDetails();
                    if (empDetails.get("First Name").equals(existingEmployee.getDetails().get("First Name")) &&
                        empDetails.get("Last Name").equals(existingEmployee.getDetails().get("Last Name"))) {
                        it.remove();
                        break;
                    }
                }
            }
            
            employees.add(new Employee(details));
            EmployeeCSVWriter.writeEmployees(csvFile, employees);
            
            JOptionPane.showMessageDialog(this,
                existingEmployee != null ? "Employee updated successfully!" : "Employee added successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving employee: " + ex.getMessage(), 
                "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}