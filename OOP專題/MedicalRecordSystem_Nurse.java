package OOP專題;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import java.io.*;

class MedicalRecordSystem_Nurse extends JFrame {
    private DefaultTableModel tableModel;
    private JTable patientTable;
    private List<Patient> patients;
    private ArrayList<MedicalRecord> medicalRecords;
    private User currentUser;
    // private Nurse nurse;
    private SimpleDateFormat sdf;
    // private static int recordCounter = 0;
    private static final String PATIENTS_FILE = "C:\OOP專題\patients.txt";
    private static final String MEDICAL_RECORDS_FILE = "C:\OOP專題\medical_records.txt";
 

    public MedicalRecordSystem_Nurse(User user) {
        this.currentUser = user;
        this.patients = new ArrayList<>();
        readPatientsFromFile();
        this.medicalRecords = new ArrayList<>();
        readMedicalRecordsFromFile();

        setTitle("醫療記錄管理系統");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                savePatientsOnExit();
                System.exit(0);
            }
        });
    }


    // 資料檔案處理
    private void readPatientsFromFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        File file = new File(PATIENTS_FILE);
        if (!file.exists()) {
            System.out.println("Patient data file not found. Starting with an empty patient list.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length != 8) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    String idNumber = fields[0];
                    String name = fields[1];
                    String gender = fields[2];
                    String birthDateString = fields[3];
                    Date birthDate = sdf.parse(birthDateString);
                    String address = fields[4];
                    String phone = fields[5];
                    String bloodType = fields[6];
                    boolean hasAllergies = Boolean.parseBoolean(fields[7]);
                    
                    Patient patient = new Patient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                    patients.add(patient);
                } catch (ParseException e) {
                    System.out.println("Error parsing date in line: " + line);
                    e.printStackTrace();
                }
            }
            System.out.println("Read " + patients.size() + " patients from file.");
        } catch (IOException e) {
            System.out.println("Error reading patients from file: " + e.getMessage());
        }
    }

    private void savePatientsOnExit() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient patient : patients) {
                bw.write(patientToCSV(patient));
                bw.newLine();
            }
            System.out.println("Patients data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String patientToCSV(Patient patient) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                patient.getIdNumber(), patient.getName(), patient.getGender(), sdf.format(patient.getBirthDate()),
                patient.getAddress(), patient.getPhone(), patient.getBloodType(), patient.hasAllergies());
    }

    public void saveMedicalRecords() {
 	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEDICAL_RECORDS_FILE))) {
 	        for (MedicalRecord medicalRecord : medicalRecords) {
 	            bw.write(medicalRecordToCSV(medicalRecord));
 	            bw.newLine();
 	        }
 	        System.out.println("Medical records saved successfully.");
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    }
 	}

 	private String escapeField(String field) {
 	    // Replace newline characters with a special placeholder
 	    field = field.replace("\n", "\\n");
 	    if (field.contains(",") || field.contains("\"")) {
 	        field = field.replace("\"", "\"\"");
 	        return "\"" + field + "\"";
 	    } else {
 	        return field;
 	    }
 	}

 	private String medicalRecordToCSV(MedicalRecord medicalRecord) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		return String.join(",",
 	            String.valueOf(medicalRecord.getMedicalRecordNumber()),
 	            escapeField(sdf.format(medicalRecord.getVisitDate())),
 	            escapeField(medicalRecord.getDoctorName()),
 	            escapeField(medicalRecord.getDiagnosis()),
 	            escapeField(medicalRecord.getTreatment()),
 	            escapeField(medicalRecord.getPrescription()));
 	}

 	private void readMedicalRecordsFromFile() {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    File file = new File(MEDICAL_RECORDS_FILE);
 	    if (!file.exists()) {
 	        System.out.println("Medical records file not found, starting with an empty record list.");
 	        return;
 	    }

 	    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
 	        String line;
 	        while ((line = br.readLine()) != null) {
 	            String[] fields = parseCSVLine(line);
 	            if (fields.length != 6) {
 	                System.out.println("Skipping improperly formatted line: " + line);
 	                continue;
 	            }

 	            try {
 	                int medicalRecordNumber = Integer.parseInt(fields[0]);
 	                Date visitDate = sdf.parse(fields[1]);
 	                String doctorName = unescapeField(fields[2]);
 	                String diagnosis = unescapeField(fields[3]);
 	                String treatment = unescapeField(fields[4]);
 	                String prescription = unescapeField(fields[5]);

 	                MedicalRecord medicalRecord = new MedicalRecord(diagnosis, treatment, prescription, medicalRecordNumber, doctorName, visitDate);
 	                medicalRecords.add(medicalRecord);
 	            } catch (ParseException | NumberFormatException e) {
 	                System.out.println("Error parsing line data: " + line);
 	                e.printStackTrace();
 	            }
 	        }
 	        System.out.println("Read " + medicalRecords.size() + " medical records from file.");
 	    } catch (IOException e) {
 	        System.out.println("Error reading medical records file: " + e.getMessage());
 	    }
     }

 	
 	private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (inQuotes) {
                if (c == '"') {
                    inQuotes = false;
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(currentField.toString());
                    currentField.setLength(0);
                } else {
                    currentField.append(c);
                }
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    private String unescapeField(String field) {
        // Reverse the special placeholder replacement
        field = field.replace("\\n", "\n");
        return field;
    }

       
       
       
    
    private void initUI() {
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.setBorder(BorderFactory.createEmptyBorder(17, 25, 21, 25)); // 四周留白

	    sdf = new SimpleDateFormat("yyyy-MM-dd");

	    JPanel mainPanel = new JPanel(new BorderLayout());

	    JLabel titleLabel = new JLabel("病歷資料庫", SwingConstants.CENTER);
	    titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
	    mainPanel.add(titleLabel, BorderLayout.NORTH);

	    JLabel welcomeLabel = new JLabel(currentUser.toString(), SwingConstants.CENTER);
	    welcomeLabel.setFont(new Font("Serif", Font.BOLD, 18));
	    mainPanel.add(welcomeLabel, BorderLayout.CENTER);

	    JPanel searchPanel = new JPanel(new FlowLayout());
	    JComboBox<String> searchField = new JComboBox<>(new String[]{"All", "Medical Record Number", "Birth Date", ">65歲患者", "藥物過敏患者", "高齡藥物過敏患者"});
	    JTextField searchInput = new JTextField(15);
	    JButton searchButton = new JButton("Search");
	    JButton addButton = new JButton("Add Patient");

	    searchPanel.add(new JLabel("Search by:"));
	    searchPanel.add(searchField);
	    searchPanel.add(searchInput);
	    searchPanel.add(searchButton);
	    searchPanel.add(addButton);

	    mainPanel.add(searchPanel, BorderLayout.SOUTH);

	    panel.add(mainPanel, BorderLayout.NORTH);

	    // 表格
	    String[] columnNames = {"病歷號", "姓名", "性别", "血型", "生日", "年齡", ">65歲", "藥物過敏"};
	    tableModel = new DefaultTableModel(columnNames, 0) {
	        @Override
	        public boolean isCellEditable(int row, int column) {
	            return false;
	        }
	    };

	    // 初始
	    if (patients.isEmpty()) {
	        tableModel.addRow(new Object[]{"尚未建立資料", "-", "-", "-", "-", "-", "-", "-"});
	    } else {
	        for (Patient patient : patients) {
	            tableModel.addRow(patient.toTableRow());
	        }
	    }

	    JTable patientTable = new JTable(tableModel);

	    // 禁止表格行列移動
	    patientTable.getTableHeader().setReorderingAllowed(false);
	    patientTable.getTableHeader().setResizingAllowed(false);

	    // 禁止表格排序
	    patientTable.setAutoCreateRowSorter(false);

	    // 表格大小
	    patientTable.setPreferredScrollableViewportSize(new Dimension(800, 200));
	    JScrollPane scrollPane = new JScrollPane(patientTable);
	    panel.add(scrollPane, BorderLayout.CENTER);

	    patientTable.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            if (e.getClickCount() == 2 && patientTable.getSelectedRow() != -1) {
	                int row = patientTable.getSelectedRow();
	                // 檢查資料是否存在
	                if (tableModel.getValueAt(row, 0).equals("尚未建立資料") || tableModel.getValueAt(row, 0).equals("尚無此資料")) {
	                    showActionSelectionDialog(null);
	                } else {
	                    // 獲取實際病人資料
	                    Patient patient = patients.get(row);
	                    showActionSelectionDialog(patient);
	                }
	            }
	        }
	    });

	    // 表格內容置中
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
	    patientTable.setDefaultRenderer(Object.class, centerRenderer);

	    setContentPane(panel);

	    // add 監聽器
	    addButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            addPatient(tableModel);
	        }
	    });
	    
	    // search 監視器
	    searchButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            String searchBy = (String) searchField.getSelectedItem();
	            String searchValue = searchInput.getText().trim();

	            if ((searchBy.equals("Medical Record Number") || searchBy.equals("Birth Date")) && searchValue.isEmpty()) {
	                JOptionPane.showMessageDialog(null, "請輸入關鍵字", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }

	            if (searchBy.equals("Medical Record Number")) {
	                if (!searchValue.matches("\\d{8}")) {
	                    JOptionPane.showMessageDialog(null, "請輸入正確病歷號格式", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
	            } else if (searchBy.equals("Birth Date")) {
	                try {
	                    Date searchDate = sdf.parse(searchValue);
	                    Date currentDate = new Date();
	                    if (!searchDate.before(currentDate)) {
	                        JOptionPane.showMessageDialog(null, "請輸入不小於當前日期", "Error", JOptionPane.ERROR_MESSAGE);
	                        return;
	                    }
	                } catch (ParseException ex) {
	                    JOptionPane.showMessageDialog(null, "輸入日期錯誤或不存在，請重新輸入", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
	            }

	            List<Patient> searchResults = new ArrayList<>();

	            try {
	                for (Patient patient : patients) {
	                    if (searchBy.equals("All")) {
	                        searchResults.add(patient);
	                    } else if (searchBy.equals("Medical Record Number") && patient.getFormattedRecordCounter().equals(searchValue)) {
	                        searchResults.add(patient);
	                    } else if (searchBy.equals("Birth Date")) {
	                        String birthDateStr = sdf.format(patient.getBirthDate());
	                        if (birthDateStr.equals(searchValue)) {
	                            searchResults.add(patient);
	                        }
	                    } else if (searchBy.equals(">65歲患者") && patient.getAge() > 65) {
	                        searchResults.add(patient);
	                    } else if (searchBy.equals("藥物過敏患者") && patient.hasAllergies()) {
	                        searchResults.add(patient);
	                    } else if (searchBy.equals("高齡藥物過敏患者") && patient.getAge() > 65 && patient.hasAllergies()) {
	                        searchResults.add(patient);
	                    }
	                }

	                updateSearchTableModel(searchResults);

	                // 清空搜索框
	                searchInput.setText("");
	            } catch (Exception ex) {
	                JOptionPane.showMessageDialog(null, "發生錯誤: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    });
	}
    
    // 添加病人
    private void addPatient(DefaultTableModel tableModel) {
        JTextField nameField = new JTextField(15);
        JComboBox<String> genderField = new JComboBox<>(new String[]{"男", "女"});
        JTextField idField = new JTextField(15);
        JTextField birthField = new JTextField(15);
        JTextField addressField = new JTextField(25);
        JTextField phoneField = new JTextField(20);
        JComboBox<String> bloodTypeField = new JComboBox<>(new String[]{"A", "B", "AB", "O"});
        JCheckBox allergyField = new JCheckBox();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("性別:"), gbc);
        gbc.gridx = 3;
        panel.add(genderField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("身分證字號:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("血型:"), gbc);
        gbc.gridx = 3;
        panel.add(bloodTypeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("出生日期 (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(birthField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("是否有過敏史:"), gbc);
        gbc.gridx = 3;
        panel.add(allergyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("電話:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("地址:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(addressField, gbc);

        JButton saveButton = new JButton("儲存");
        saveButton.setEnabled(false);

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveButton.setEnabled(isAllFieldsFilled());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveButton.setEnabled(isAllFieldsFilled());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveButton.setEnabled(isAllFieldsFilled());
            }

            private boolean isAllFieldsFilled() {
                return !nameField.getText().trim().isEmpty() &&
                       !idField.getText().trim().isEmpty() &&
                       !birthField.getText().trim().isEmpty() &&
                       !addressField.getText().trim().isEmpty() &&
                       !phoneField.getText().trim().isEmpty();
            }
        };

        nameField.getDocument().addDocumentListener(documentListener);
        idField.getDocument().addDocumentListener(documentListener);
        birthField.getDocument().addDocumentListener(documentListener);
        addressField.getDocument().addDocumentListener(documentListener);
        phoneField.getDocument().addDocumentListener(documentListener);

        // Gender, blood type, and allergy listeners don't affect the save button status
        genderField.addActionListener(e -> {});
        bloodTypeField.addActionListener(e -> {});
        allergyField.addActionListener(e -> {});
        
        // 姓名欄位的鍵盤監聽器
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入姓名", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        genderField.requestFocusInWindow(); // 將焦點移至性別欄位
                    }
                }
            }
        });

        // 性別欄位的鍵盤監聽器
        genderField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    idField.requestFocusInWindow(); // 將焦點移至身分證欄位
                }
            }
        });

        // 身分證欄位的鍵盤監聽器
        idField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String idNumber = idField.getText().trim();
                    if (idNumber.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入身分證字號", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else if (!isValidIdNumber(idNumber)) {
                        JOptionPane.showMessageDialog(panel, "請檢查輸入是否錯誤", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        // 將首字母轉為大寫
                        idField.setText(idNumber.substring(0, 1).toUpperCase() + idNumber.substring(1));
                        bloodTypeField.requestFocusInWindow(); // 將焦點移至血型欄位
                    }
                }
            }

            private boolean isValidIdNumber(String idNumber) {
                if (idNumber.length() != 10) {
                    return false;
                }
                char firstChar = idNumber.charAt(0);
                if (!Character.isLetter(firstChar)) {
                    return false;
                }
                for (int i = 1; i < idNumber.length(); i++) {
                    if (!Character.isDigit(idNumber.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        });

        // 血型欄位的鍵盤監聽器
        bloodTypeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    birthField.requestFocusInWindow(); // 將焦點移至生日欄位
                }
            }
        });

        // 生日欄位的鍵盤監聽器
        birthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String birthDateStr = birthField.getText().trim();
                    if (birthDateStr.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入出生日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false);
                        try {
                            Date birthDate = sdf.parse(birthDateStr);
                            if (birthDate.after(new Date())) {
                                JOptionPane.showMessageDialog(panel, "請輸入不小於當前的日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            } else {
                                allergyField.requestFocusInWindow(); // 將焦點移至過敏選擇欄位
                            }
                        } catch (ParseException ex) {
                            if (!birthDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                JOptionPane.showMessageDialog(panel, "輸入格式錯誤。請使用 yyyy-MM-dd 格式", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            } else {
                                JOptionPane.showMessageDialog(panel, "輸入日期錯誤或不存在，請重新輸入", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            }
                        }
                    }
                }
            }
        });

        // 過敏選擇欄位的鍵盤監聽器
        allergyField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    phoneField.requestFocusInWindow(); // 將焦點移至電話欄位
                }
            }
        });

        // 電話欄位的鍵盤監聽器
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String phoneNumber = phoneField.getText().trim();
                    if (phoneNumber.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入電話", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else if (!phoneNumber.matches("\\d{8,10}")) {
                        JOptionPane.showMessageDialog(panel, "請檢查輸入是否正確", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        addressField.requestFocusInWindow(); // 將焦點移至地址欄位
                    }
                }
            }
        });

        // 地址欄位的鍵盤監聽器
        addressField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveButton.doClick(); // 執行儲存按鈕的動作
                }
            }
        });

        // save 監聽器
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String gender = (String) genderField.getSelectedItem();
            String idNumber = idField.getText();
            String birthDateStr = birthField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String bloodType = (String) bloodTypeField.getSelectedItem();
            boolean hasAllergies = allergyField.isSelected();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date birthDate = sdf.parse(birthDateStr);
                if (birthDate.after(new Date())) {
                    JOptionPane.showMessageDialog(panel, "請輸入不小於當前的日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 在這裡應確保將新患者添加到 patients 列表中(多型)
                Patient patient;
                int age = Patient.calculateAge(birthDate)[0];
                if (age >= 65 && hasAllergies) {
                    patient = new SpecialElderlyPatient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                } else if (age >= 65) {
                    patient = new ElderlyPatient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                } else if (hasAllergies) {
                    patient = new SpecialPatient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                } else {
                    patient = new Patient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                }
                patients.add(patient);

                // 更新表格模型
                updateTableModel();

                JOptionPane.showMessageDialog(panel, "新增成功！");
                Window window = SwingUtilities.windowForComponent(panel);
                window.dispose();
            } catch (ParseException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "輸入格式錯誤。請使用 yyyy-MM-dd 格式", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // 在這裡處理視窗關閉事件，例如清理資源或顯示額外訊息
                System.out.println("視窗已關閉");
            }
        });

        JOptionPane.showOptionDialog(null, panel, "新增基本資料",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{}, null);
    }

    /*
    private void addMedicalRecord(Patient patient) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JTextField recordNumberField = new JTextField(patient.getFormattedRecordCounter());
        recordNumberField.setEditable(false);
        JTextField patientNameField = new JTextField(patient.getName(), 20);
        patientNameField.setEditable(false);
        JTextField visitDateField = new JTextField(sdf.format(new Date()), 20);
        visitDateField.setEditable(false);
        JTextField doctorField = new JTextField(currentUser.getName(), 20);
        doctorField.setEditable(false);
        JTextField bloodTypeField = new JTextField(patient.getBloodType(), 20);
        bloodTypeField.setEditable(false);
        JTextField birthDateField = new JTextField(patient.getBirthDateString(), 20);
        birthDateField.setEditable(false);
        JCheckBox allergyField = new JCheckBox("Has Allergies", patient.hasAllergies());
        allergyField.setEnabled(false);

        JTextArea diagnosisField = new JTextArea(5, 30);
        JTextArea treatmentField = new JTextArea(5, 30);
        JTextArea prescriptionField = new JTextArea(5, 30);

        JScrollPane diagnosisScrollPane = new JScrollPane(diagnosisField);
        JScrollPane treatmentScrollPane = new JScrollPane(treatmentField);
        JScrollPane prescriptionScrollPane = new JScrollPane(prescriptionField);

        diagnosisField.setLineWrap(true);
        diagnosisField.setWrapStyleWord(true);
        treatmentField.setLineWrap(true);
        treatmentField.setWrapStyleWord(true);
        prescriptionField.setLineWrap(true);
        prescriptionField.setWrapStyleWord(true);

        JButton saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            diagnosisField.setText("");
            treatmentField.setText("");
            prescriptionField.setText("");
            saveButton.setEnabled(false);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
            frame.dispose();
        });

        // Add document listeners to enable Save button when any text field changes
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveButton.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveButton.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Usually not needed for plain text components
            }
        };

        diagnosisField.getDocument().addDocumentListener(docListener);
        treatmentField.getDocument().addDocumentListener(docListener);
        prescriptionField.getDocument().addDocumentListener(docListener);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("診療紀錄");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST; // Align left
        panel.add(titleLabel, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Medical Record Number:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(recordNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(patientNameField, gbc);
        gbc.gridx = 3;
        panel.add(new JLabel("Visit Date:"), gbc);
        gbc.gridx = 4;
        panel.add(visitDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(doctorField, gbc);
        gbc.gridx = 3;
        panel.add(new JLabel("Blood Type:"), gbc);
        gbc.gridx = 4;
        panel.add(bloodTypeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Birth Date:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(birthDateField, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Has Allergies:"), gbc);
        gbc.gridx = 4;
        panel.add(allergyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Diagnosis(診斷):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(diagnosisScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Treatment(治療):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(treatmentScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Prescription(處方):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(prescriptionScrollPane, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Create scroll pane and frame
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(800, 550));

        JFrame frame = new JFrame("診療紀錄");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

        // save 監聽器
        saveButton.addActionListener(e -> {
            String diagnosis = diagnosisField.getText();
            String treatment = treatmentField.getText();
            String prescription = prescriptionField.getText();
            Date visitDate;

            try {
                visitDate = sdf.parse(sdf.format(new Date())); // Use current date as visit date
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "格式錯誤. 請使用 yyyy-MM-dd HH:mm:ss.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MedicalRecord record = new MedicalRecord(diagnosis, treatment, prescription, patient.getMedicalRecordNumber(), currentUser.getName(), visitDate);
            medicalRecords.add(record);
            JOptionPane.showMessageDialog(frame, "診療紀錄新增成功！");
            frame.dispose(); // Close the frame after saving
        });
    }
    */

    // 顯示病人基本資料
    private void showPatientDetails(Patient patient) {
        JTextField nameField = new JTextField(patient.getName(), 15);
        JComboBox<String> genderField = new JComboBox<>(new String[]{"男", "女"});
        genderField.setSelectedItem(patient.getGender());
        JTextField idField = new JTextField(patient.getIdNumber(), 15);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JTextField birthField = new JTextField(sdf.format(patient.getBirthDate()), 15);
        JTextField addressField = new JTextField(patient.getAddress(), 25);
        JTextField phoneField = new JTextField(patient.getPhone(), 20);
        JComboBox<String> bloodTypeField = new JComboBox<>(new String[]{"A", "B", "AB", "O"});
        bloodTypeField.setSelectedItem(patient.getBloodType());
        JCheckBox allergyField = new JCheckBox();
        allergyField.setSelected(patient.hasAllergies());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 3;
        panel.add(genderField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("身分證字號:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("血型:"), gbc);
        gbc.gridx = 3;
        panel.add(bloodTypeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("出生日期 (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        panel.add(birthField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("是否有過敏史:"), gbc);
        gbc.gridx = 3;
        panel.add(allergyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("電話:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("地址:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(addressField, gbc);

        JButton okButton = new JButton("Save");
        okButton.setEnabled(false);

        // Add change listeners to fields to enable OK button when data changes
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                okButton.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isAnyFieldModified()) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                okButton.setEnabled(true);
            }

            private boolean isAnyFieldModified() {
                return !nameField.getText().equals(patient.getName()) ||
                       !idField.getText().equals(patient.getIdNumber()) ||
                       !birthField.getText().equals(sdf.format(patient.getBirthDate())) ||
                       !addressField.getText().equals(patient.getAddress()) ||
                       !phoneField.getText().equals(patient.getPhone());
            }
        };

        // Add listeners to fields
        nameField.getDocument().addDocumentListener(documentListener);
        idField.getDocument().addDocumentListener(documentListener);
        birthField.getDocument().addDocumentListener(documentListener);
        addressField.getDocument().addDocumentListener(documentListener);
        phoneField.getDocument().addDocumentListener(documentListener);
        genderField.addActionListener(e -> okButton.setEnabled(true));
        bloodTypeField.addActionListener(e -> okButton.setEnabled(true));
        allergyField.addActionListener(e -> okButton.setEnabled(true));
        
        // 姓名欄位的鍵盤監聽器
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入姓名", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        genderField.requestFocusInWindow(); // 將焦點移至性別欄位
                    }
                }
            }
        });

        // 性別欄位的鍵盤監聽器
        genderField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    idField.requestFocusInWindow(); // 將焦點移至身分證欄位
                }
            }
        });

        // 身分證欄位的鍵盤監聽器
        idField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String idNumber = idField.getText().trim();
                    if (idNumber.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入身分證字號", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else if (!isValidIdNumber(idNumber)) {
                        JOptionPane.showMessageDialog(panel, "請檢查輸入是否錯誤", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        // 將首字母轉為大寫
                        idField.setText(idNumber.substring(0, 1).toUpperCase() + idNumber.substring(1));
                        bloodTypeField.requestFocusInWindow(); // 將焦點移至血型欄位
                    }
                }
            }

            private boolean isValidIdNumber(String idNumber) {
                if (idNumber.length() != 10) {
                    return false;
                }
                char firstChar = idNumber.charAt(0);
                if (!Character.isLetter(firstChar)) {
                    return false;
                }
                for (int i = 1; i < idNumber.length(); i++) {
                    if (!Character.isDigit(idNumber.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        });

        // 血型欄位的鍵盤監聽器
        bloodTypeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    birthField.requestFocusInWindow(); // 將焦點移至生日欄位
                }
            }
        });

        // 生日欄位的鍵盤監聽器
        birthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String birthDateStr = birthField.getText().trim();
                    if (birthDateStr.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入出生日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false);
                        try {
                            Date birthDate = sdf.parse(birthDateStr);
                            if (birthDate.after(new Date())) {
                                JOptionPane.showMessageDialog(panel, "請輸入不小於當前的日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            } else {
                                allergyField.requestFocusInWindow(); // 將焦點移至過敏選擇欄位
                            }
                        } catch (ParseException ex) {
                            if (!birthDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                JOptionPane.showMessageDialog(panel, "輸入格式錯誤。請使用 yyyy-MM-dd 格式", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            } else {
                                JOptionPane.showMessageDialog(panel, "輸入日期錯誤或不存在，請重新輸入", "錯誤", JOptionPane.ERROR_MESSAGE);
                                // 阻止焦點轉移
                                e.consume();
                            }
                        }
                    }
                }
            }
        });

        // 過敏選擇欄位的鍵盤監聽器
        allergyField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    phoneField.requestFocusInWindow(); // 將焦點移至電話欄位
                }
            }
        });

        // 電話欄位的鍵盤監聽器
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String phoneNumber = phoneField.getText().trim();
                    if (phoneNumber.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "請輸入電話", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else if (!phoneNumber.matches("\\d{8,10}")) {
                        JOptionPane.showMessageDialog(panel, "請檢查輸入是否正確", "錯誤", JOptionPane.ERROR_MESSAGE);
                        // 阻止焦點轉移
                        e.consume();
                    } else {
                        addressField.requestFocusInWindow(); // 將焦點移至地址欄位
                    }
                }
            }
        });

        // 地址欄位的鍵盤監聽器
        addressField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okButton.doClick(); // 執行儲存按鈕的動作
                }
            }
        });
        
        // ok 監聽器
        okButton.addActionListener(e -> {
            String name = nameField.getText();
            String gender = (String) genderField.getSelectedItem();
            String idNumber = idField.getText();
            String birthDateStr = birthField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String bloodType = (String) bloodTypeField.getSelectedItem();
            boolean hasAllergies = allergyField.isSelected();

            try {
                Date birthDate = sdf.parse(birthDateStr);

                patient.setName(name);
                patient.setGender(gender);
                patient.setIdNumber(idNumber);
                patient.setBirthDate(birthDate);
                patient.setAddress(address);
                patient.setPhone(phone);
                patient.setBloodType(bloodType);
                patient.setHasAllergies(hasAllergies);

                updateTableModel();

                Window window = SwingUtilities.getWindowAncestor(panel);
                window.dispose();

            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, "輸入格式錯誤。請使用 yyyy-MM-dd 格式。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(okButton, gbc);

        JOptionPane.showOptionDialog(null, panel, "詳細基本資料",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{}, null);
    }


    // Check if any field has been modified
    private boolean isAnyFieldModified() {
        // Implement logic to check if any field has been modified
        return true; // Modify this according to your implementation
    }

    
    /*
    // Update table model after modifying patient details
    private void updateTableModel() {
        // Implement logic to update the table model
    }
    */

    
    // something
    private String getPatientDisplayString(Patient patient) {
        String displayString = patient.medicalRecordNumber + ": " + patient.name;
        boolean isSpecialPatient = patient instanceof SpecialPatient;
        boolean isElderlyPatient = patient instanceof ElderlyPatient;

        // 可同時存在
        if (isSpecialPatient) {
            displayString += "*";
        }
        if (isElderlyPatient) {
            displayString += "#";
        }

        return displayString;
    }

    // 就診紀錄
    public void viewMedicalRecords(Patient patient) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("就診紀錄", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        infoPanel.add(new JLabel("病歷號: " + patient.getFormattedRecordCounter()));

        JPanel personalInfoPanel = new JPanel(new GridLayout(1, 6, 0, 0));
        personalInfoPanel.add(new JLabel("姓名: " + patient.getName().trim()));
        personalInfoPanel.add(new JLabel("性別: " + patient.getGender()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        personalInfoPanel.add(new JLabel("生日: " + dateFormat.format(patient.getBirthDate())));
        int[] age = patient.calculateAge(patient.getBirthDate());
        personalInfoPanel.add(new JLabel("年齡: " + age[0] + "歲 " + age[1] + "個月"));
        personalInfoPanel.add(new JLabel("血型: " + patient.getBloodType()));
        personalInfoPanel.add(new JLabel("是否藥物過敏: " + (patient.hasAllergies() ? "是" : "否")));
        infoPanel.add(personalInfoPanel);

        infoPanel.add(new JLabel(" "));

        panel.add(infoPanel, BorderLayout.CENTER);

        String[] columnNames = {"筆數", "就診日期", "看診醫生"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);

        List<MedicalRecord> medicalRecords = getMedicalRecordsForPatient(patient);

        if (medicalRecords.isEmpty()) {
            tableModel.addRow(new Object[]{"病歷資料尚未存在", "", ""});
        } else {
            int count = 1;
            for (MedicalRecord record : medicalRecords) {
                Object[] rowData = {
                    count++,
                    dateFormat.format(record.getVisitDate()),
                    record.getDoctorName()
                };
                tableModel.addRow(rowData);
            }
        }

        // JTable 的 MouseListener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        Object recordNumberObj = table.getValueAt(row, 0);
                        if (recordNumberObj instanceof Integer) {
                            Integer recordNumber = (Integer) recordNumberObj;
                            if (recordNumber == null) {
                                JOptionPane.showMessageDialog(null, "病歷資料尚未存在", "提示", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                showMedicalRecordDetails(medicalRecords.get(row), patient);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "病歷資料尚未存在", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        table.setRowHeight(30);
        table.setPreferredScrollableViewportSize(new Dimension(800, 400));
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("查看病歷資料");
        dialog.setContentPane(panel);
        dialog.setPreferredSize(new Dimension(1000, 580));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private List<MedicalRecord> getMedicalRecordsForPatient(Patient patient) {
        List<MedicalRecord> patientRecords = new ArrayList<>();
        for (MedicalRecord record : medicalRecords) {
            if (record.getMedicalRecordNumber() == patient.getMedicalRecordNumber()) {
                patientRecords.add(record);
            }
        }
        return patientRecords;
    }

    // 診療紀錄
    private void showMedicalRecordDetails(MedicalRecord record, Patient patient) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("診療紀錄");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(titleLabel, gbc);

        JTextField recordNumberField = new JTextField(patient.getFormattedRecordCounter());
        recordNumberField.setEditable(false);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Medical Record Number:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(recordNumberField, gbc);

        JTextField patientNameField = new JTextField(patient.getName(), 20);
        patientNameField.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(patientNameField, gbc);

        JTextField visitDateField = new JTextField(sdf.format(record.getVisitDate()), 20);
        visitDateField.setEditable(false);
        gbc.gridx = 3;
        panel.add(new JLabel("Visit Date:"), gbc);
        gbc.gridx = 4;
        panel.add(visitDateField, gbc);

        JTextField doctorField = new JTextField(record.getDoctorName(), 20);
        doctorField.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(doctorField, gbc);

        JTextField bloodTypeField = new JTextField(patient.getBloodType(), 20);
        bloodTypeField.setEditable(false);
        gbc.gridx = 3;
        panel.add(new JLabel("Blood Type:"), gbc);
        gbc.gridx = 4;
        panel.add(bloodTypeField, gbc);

        JTextField birthDateField = new JTextField(patient.getBirthDateString(), 20);
        birthDateField.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Birth Date:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(birthDateField, gbc);

        JCheckBox allergyField = new JCheckBox("Has Allergies", patient.hasAllergies());
        allergyField.setEnabled(false);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Has Allergies:"), gbc);
        gbc.gridx = 4;
        panel.add(allergyField, gbc);

        JTextArea diagnosisField = new JTextArea(record.getDiagnosis(), 5, 30);
        JTextArea treatmentField = new JTextArea(record.getTreatment(), 5, 30);
        JTextArea prescriptionField = new JTextArea(record.getPrescription(), 5, 30);

        diagnosisField.setEditable(false);
        diagnosisField.setBackground(panel.getBackground());
        treatmentField.setEditable(false);
        treatmentField.setBackground(panel.getBackground());
        prescriptionField.setEditable(false);
        prescriptionField.setBackground(panel.getBackground());

        // Scroll panes for text areas
        JScrollPane diagnosisScrollPane = new JScrollPane(diagnosisField);
        JScrollPane treatmentScrollPane = new JScrollPane(treatmentField);
        JScrollPane prescriptionScrollPane = new JScrollPane(prescriptionField);

        // Set line wrap and word wrap for text areas
        diagnosisField.setLineWrap(true);
        diagnosisField.setWrapStyleWord(true);
        treatmentField.setLineWrap(true);
        treatmentField.setWrapStyleWord(true);
        prescriptionField.setLineWrap(true);
        prescriptionField.setWrapStyleWord(true);

        Color darkGray = new Color(65, 65, 65); 
        recordNumberField.setForeground(darkGray);
        patientNameField.setForeground(darkGray);
        visitDateField.setForeground(darkGray);
        doctorField.setForeground(darkGray);
        bloodTypeField.setForeground(darkGray);
        birthDateField.setForeground(darkGray);
        diagnosisField.setForeground(darkGray);
        treatmentField.setForeground(darkGray);
        prescriptionField.setForeground(darkGray);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Diagnosis(診斷):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(diagnosisScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Treatment(治療):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(treatmentScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Prescription(處方):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        panel.add(prescriptionScrollPane, gbc);

        JButton cancelButton = new JButton("Close");

        // cancel 監聽器
        cancelButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
            frame.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(800, 550));

        JFrame frame = new JFrame("診療紀錄");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    // 刪除
    private void deleteMedicalRecord(Patient patient) {
        int confirmation = JOptionPane.showConfirmDialog(null, "確定要刪除病歷嗎？", "確認刪除", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            // 實現刪除病歷的邏輯
            // 這裡應該刪除 patient 相關的 medicalRecords，並從 patients 列表中刪除 patient
            patients.remove(patient);
            // 同步更新表格模型
            updateTableModel();
            JOptionPane.showMessageDialog(null, "病歷已刪除");
        }
    }

    // 更新表格
    private void updateTableModel() {
        // 清空表格模型
        tableModel.setRowCount(0);

        if (patients.isEmpty()) {
            // 初始
            tableModel.addRow(new Object[]{"尚未建立資料", "-", "-", "-", "-", "-", "-", "-"});
        } else {
            // 重新添加所有病人的資料到表格模型
            for (Patient patient : patients) {
                Object[] patientData = patient.getPatientData();
                // 确保病歷號
                patientData[0] = String.format("%08d", patient.getMedicalRecordNumber());
                tableModel.addRow(patientData);
            }
        }
    }
    
    // 更新表格
    private void updateSearchTableModel(List<Patient> searchResults) {
        tableModel.setRowCount(0);
        if (searchResults.isEmpty()) {
            tableModel.addRow(new Object[]{"尚無此資料", "-", "-", "-", "-", "-", "-", "-"});
        } else {
            for (Patient patient : searchResults) {
                Object[] patientData = patient.getPatientData();
                patientData[0] = String.format("%08d", patient.getMedicalRecordNumber());
                tableModel.addRow(patientData);
            }
        }
    }
    
    // initializeTableModel
    private void initializeTableModel() {
        tableModel = new DefaultTableModel(new String[]{"病歷號", "姓名", "性別", "出生日期", "電話", "地址", "血型", "是否藥物過敏"}, 0);
        updateTableModel();
    }
    
    // 選單(點擊表格跳出)
    private void showActionSelectionDialog(Patient patient) {
        if (patient == null) {
            JOptionPane.showMessageDialog(null, "尚無資料", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = {"查看詳細基本資料", "查看詳細就診資料", "刪除病歷"};
        String choice = (String) JOptionPane.showInputDialog(
                null,
                "請選擇操作：",
                "操作選擇",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != null) {
            switch (choice) {
                case "查看詳細基本資料":
                    showPatientDetails(patient);
                    break;
                /*
                case "TEST":
                    addMedicalRecord(patient);
                    break;
                */
                case "查看詳細就診資料":
                    viewMedicalRecords(patient);
                    break;
                case "刪除病歷":
                    deleteMedicalRecord(patient);
                    break;
                default:
                    break;
            }
        }
    }

    /*
    public static void main(String[] args) {
        // 假設有個Doctor用戶
        User Usernurse = new Nurse("Dr. JoJo", "drsmith", "password123");
        MedicalRecordSystem_Nurse system = new MedicalRecordSystem_Nurse(Usernurse);
        system.setVisible(true);
    }
    */
    
}

