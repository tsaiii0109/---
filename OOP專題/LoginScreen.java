package OOP專題;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private List<User> users;

    public LoginScreen() {
        users = new ArrayList<>();   // 實例化使用者列表
        initializeDefaultUsers();
        setTitle("登入系統");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel backgroundPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("C:\OOP專題\0615.jpg");    // 背景圖片路徑(*要改路徑)
                Image img = icon.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };

        int leftMargin = 30;
        int bottomMargin = 30;
        int labelWidth = 100;
        int fieldWidth = 150;
        int fieldHeight = 20;

        JLabel titleLabel = new JLabel("診所病歷管理系統");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setBounds(leftMargin, getHeight() - bottomMargin - 4 * fieldHeight - 85, 300, 30);

        // 創建底部邊框
        Border bottomBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
        // 應用底部邊框到標題標籤
        titleLabel.setBorder(bottomBorder);

        backgroundPanel.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(leftMargin, getHeight() - bottomMargin - 3 * (fieldHeight + 13) - 8, labelWidth, fieldHeight);
        backgroundPanel.add(userLabel);
        // username 輸入
        usernameField = new JTextField(15);
        usernameField.setBounds(leftMargin + labelWidth - 12, getHeight() - bottomMargin - 3 * (fieldHeight + 13) - 8, fieldWidth, fieldHeight);
        backgroundPanel.add(usernameField);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(leftMargin, getHeight() - bottomMargin - 2 * (fieldHeight + 13) - 8, labelWidth, fieldHeight);
        backgroundPanel.add(passwordLabel);
        // 密碼輸入
        passwordField = new JPasswordField(15);
        passwordField.setBounds(leftMargin + labelWidth - 12, getHeight() - bottomMargin - 2 * (fieldHeight + 13) - 8, fieldWidth, fieldHeight);
        backgroundPanel.add(passwordField);
        // 密碼可視
        JCheckBox showPassword = new JCheckBox("  Show Password");
        showPassword.setBounds(leftMargin + labelWidth - 15, getHeight() - bottomMargin - fieldHeight - 28, fieldWidth, fieldHeight);
        showPassword.setOpaque(false);
        showPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('*');
                }
            }
        });
        backgroundPanel.add(showPassword);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(leftMargin + labelWidth + fieldWidth + 20, getHeight() - bottomMargin - 2 * (fieldHeight + 13) - 8, 80, fieldHeight); // 調整登入按鈕位置
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        backgroundPanel.add(loginButton);

        // 鍵盤事件監聽器給使用者名稱輸入框
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 按下 Enter 鍵，轉移到密碼輸入框
                passwordField.requestFocusInWindow();
            }
        });

        // 鍵盤事件監聽器給密碼輸入框
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 按下 Enter 鍵時執行登入
                performLogin();
            }
        });

        setContentPane(backgroundPanel);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = authenticate(username, password);
        if (user != null) {
            JFrame system;
            if (user instanceof Doctor) {
                system = new MedicalRecordSystem_Doctor(user);
            } else if (user instanceof Nurse) {
                system = new MedicalRecordSystem_Nurse(user);
            } else {
                throw new IllegalStateException("未知的使用者類型");
            }
            system.setVisible(true);      
            dispose(); // 關閉登入視窗
        } else {
            JOptionPane.showMessageDialog(LoginScreen.this, "無效的使用者名稱或密碼。", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private User authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private void initializeDefaultUsers() {
        users.add(new Doctor("Mr. Smith", "drsmith", "password123"));
        users.add(new Doctor("Mr. Adams", "dradams", "password234"));
        users.add(new Doctor("Mr. Baker", "drbaker", "password345"));
        users.add(new Doctor("Mr. Johny", "test", "test123"));

        users.add(new Nurse("Ms. Jonna", "joanna", "joanna931129"));
        users.add(new Nurse("Ms. Jane", "jane", "password567"));
        users.add(new Nurse("Ms. Sam", "sam", "password678"));
        users.add(new Nurse("Ms. Anna", "anna", "password789"));
        users.add(new Nurse("Ms. Mike", "mike", "password890"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
            }
        });
    }
}

