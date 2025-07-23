import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.awt.GridLayout;
import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

public class LoginSystem {
    private static final String CREDENTIALS_PATH = "data/login_credentials.csv";
    private String loggedInEmployeeId;
    private String loggedInRole;
    private String loggedInUsername;
    
    // Track current user statically for password changes
    private static String currentUsername;
    private static String currentEmployeeId;

    public boolean authenticateUser() {
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.setVisible(true);

        if (loginDialog.isAuthenticated()) {
            if (loginDialog.usingDefaultPassword()) {
                promptPasswordChange();
            }
            return true;
        }
        return false;
    }

    // Static getters for PayrollGUI access
    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static String getCurrentEmployeeId() {
        return currentEmployeeId;
    }

    private void promptPasswordChange() {
        int choice = JOptionPane.showConfirmDialog(null,
            "You're using the default password. Change it now?",
            "Password Change Required",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // PayrollGUI will handle this via its change password dialog
        }
    }

    private class LoginDialog extends JDialog {
        private boolean isAuthenticated = false;
        private boolean usingDefaultPassword = false;

        public LoginDialog() {
            super((JFrame)null, "MotorPH Login", true);
            setSize(350, 200);
            setLayout(new GridLayout(3, 2, 10, 10));
            setLocationRelativeTo(null);

            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();

            add(new JLabel("Username:"));
            add(usernameField);
            add(new JLabel("Password:"));
            add(passwordField);

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> {
                try {
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword());

                    if (validateCredentials(username, password)) {
                        isAuthenticated = true;
                        currentUsername = username;
                        currentEmployeeId = loggedInEmployeeId;
                        
                        // Check if password is default
                        usingDefaultPassword = 
                            hashPassword("password").equals(hashPassword(password));
                        
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Invalid username or password\n" +
                            "Username format: first initial + last name (e.g. mgarcia)",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "System error during login\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            add(loginButton);
        }

        private boolean validateCredentials(String username, String password) 
            throws CsvValidationException, IOException {
            
            try (CSVReader reader = new CSVReader(new FileReader(CREDENTIALS_PATH))) {
                String[] record;
                while ((record = reader.readNext()) != null) {
                    if (record.length >= 4 && 
                        record[0].equals(username) && 
                        record[1].equals(hashPassword(password))) {
                        
                        loggedInEmployeeId = record[2];
                        loggedInRole = record[3];
                        loggedInUsername = username;
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public boolean usingDefaultPassword() {
            return usingDefaultPassword;
        }
    }

    // Moved hashPassword to public static for CredentialManager access
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    // Getters for login information
    public String getLoggedInEmployeeId() {
        return loggedInEmployeeId;
    }

    public String getLoggedInRole() {
        return loggedInRole;
    }
}