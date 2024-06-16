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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import java.io.*;


class MedicalRecordSystem_Doctor extends JFrame {
    private DefaultTableModel tableModel;
    private JTable patientTable;
    private List<Patient> patients;
    private ArrayList<MedicalRecord> medicalRecords;
    private User currentUser;
    private SimpleDateFormat sdf;
    // private static int recordCounter = 0;
    private static final String PATIENTS_FILE = "C:\OOP專題\patients.txt";
    private static final String MEDICAL_RECORDS_FILE = "C:\OOP專題\medical_records.txt";

    
    public MedicalRecordSystem_Doctor(User user) {
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
                saveMedicalRecords();
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

    // 有 enter 的儲存辦法
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
        fields.add(currentField.toString()); // add last field
        return fields.toArray(new String[0]);
    }

    private String unescapeField(String field) {
        // Reverse the special placeholder replacement
        field = field.replace("\\n", "\n");
        return field;
    }
    
    
    
    // 初始化界面方法
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
        addButton.setEnabled(false);

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

        // 表格監聽器
        patientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && patientTable.getSelectedRow() != -1) {
                    int row = patientTable.getSelectedRow();
                    // 檢查點擊資料
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
        
        /*
        patientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && patientTable.getSelectedRow() != -1) {
                    int row = patientTable.getSelectedRow();
                    // 检查表格中的数据是否为“尚未建立資料”或者“尚無資料”
                    if (tableModel.getValueAt(row, 0).equals("尚未建立資料") || tableModel.getValueAt(row, 0).equals("尚無此資料")) {
                        JOptionPane.showMessageDialog(null, "尚無資料", "提示", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // 获取实际病人对象
                        Patient patient = patients.get(row);
                        showActionSelectionDialog(patient);
                    }
                }
            }
        });
        */

        // 表格內容置中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        patientTable.setDefaultRenderer(Object.class, centerRenderer);

        setContentPane(panel);
        /*
        // add 監聽器
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient(tableModel);
            }
        });
        */
        // search 監聽器
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchBy = (String) searchField.getSelectedItem();
                String searchValue = searchInput.getText().trim();

                if ((searchBy.equals("Medical Record Number") || searchBy.equals("Birth Date")) && searchValue.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "請輸入關鍵字", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
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

                    // 清空搜尋框
                    searchInput.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "發生錯誤: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /*
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

        JButton saveButton = new JButton("儲存");
        saveButton.setEnabled(false);

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveButton.setEnabled(!isAnyFieldEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveButton.setEnabled(!isAnyFieldEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveButton.setEnabled(!isAnyFieldEmpty());
            }

            private boolean isAnyFieldEmpty() {
                return nameField.getText().trim().isEmpty() ||
                       idField.getText().trim().isEmpty() ||
                       birthField.getText().trim().isEmpty() ||
                       addressField.getText().trim().isEmpty() ||
                       phoneField.getText().trim().isEmpty();
            }
        };
        
        nameField.getDocument().addDocumentListener(documentListener);
        idField.getDocument().addDocumentListener(documentListener);
        birthField.getDocument().addDocumentListener(documentListener);
        addressField.getDocument().addDocumentListener(documentListener);
        phoneField.getDocument().addDocumentListener(documentListener);
        genderField.addActionListener(e -> saveButton.setEnabled(true));
        bloodTypeField.addActionListener(e -> saveButton.setEnabled(true));
        allergyField.addActionListener(e -> saveButton.setEnabled(true));

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
                Patient patient = new Patient(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
                patients.add(patient);  // 确保将新患者添加到 patients 列表中

                updateTableModel(); // 添加病人后更新表格模型
                JOptionPane.showMessageDialog(panel, "已成功新增！");
                Window window = SwingUtilities.windowForComponent(panel);
                window.dispose();
            } catch (ParseException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "輸入格式錯誤。請使用 yyyy-MM-dd 格式。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        JOptionPane.showOptionDialog(null, panel, "新增病人",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{}, null);
    }
	*/

    // 添加診療紀錄
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
            private void checkFields() {
                if (!diagnosisField.getText().trim().isEmpty() &&
                    !treatmentField.getText().trim().isEmpty() &&
                    !prescriptionField.getText().trim().isEmpty()) {
                    saveButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkFields();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkFields();
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
        gbc.anchor = GridBagConstraints.WEST;
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
                visitDate = sdf.parse(sdf.format(new Date()));
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "格式錯誤. 請使用 yyyy-MM-dd HH:mm:ss.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MedicalRecord record = new MedicalRecord(diagnosis, treatment, prescription, patient.getMedicalRecordNumber(), currentUser.getName(), visitDate);
            medicalRecords.add(record);
            JOptionPane.showMessageDialog(frame, "診療紀錄新增成功！");
            frame.dispose(); 
        });
    }

    // 顯示基本資料
    private static void showPatientDetails(Patient patient) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        JTextField nameField = createNonEditableTextField(patient.getName(), 15);
        JComboBox<String> genderField = createNonEditableComboBox(new String[]{"男", "女"});
        genderField.setSelectedItem(patient.getGender());
        JTextField idField = createNonEditableTextField(patient.getIdNumber(), 15);
        JTextField birthField = createNonEditableTextField(sdf.format(patient.getBirthDate()), 15);
        JTextField addressField = createNonEditableTextField(patient.getAddress(), 25);
        JTextField phoneField = createNonEditableTextField(patient.getPhone(), 20);
        JComboBox<String> bloodTypeField = createNonEditableComboBox(new String[]{"A", "B", "AB", "O"});
        bloodTypeField.setSelectedItem(patient.getBloodType());
        JCheckBox allergyField = new JCheckBox();
        allergyField.setSelected(patient.hasAllergies());
        allergyField.setEnabled(false);

        nameField.setForeground(Color.GRAY);
        idField.setForeground(Color.GRAY);
        birthField.setForeground(Color.GRAY);
        addressField.setForeground(Color.GRAY);
        phoneField.setForeground(Color.GRAY);
        genderField.setForeground(Color.GRAY);
        bloodTypeField.setForeground(Color.GRAY);
        allergyField.setForeground(Color.GRAY);

        /*
        // Set foreground color for combo box renderers
        Color darkGray = new Color(90, 90, 90); // Custom dark gray color
        ((JLabel) genderField.getRenderer()).setForeground(darkGray);
        ((JLabel) bloodTypeField.getRenderer()).setForeground(darkGray);
        allergyField.setForeground(darkGray); // Set foreground color for checkbox
        */

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

        JButton closeButton = new JButton("Close");

        // close 監聽器
        closeButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(panel);
            window.dispose();
        });

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(closeButton, gbc);

        JOptionPane.showOptionDialog(null, panel, "詳細基本資料",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{}, null);
    }

    // Helper method to create non-editable text fields with specified columns
    private static JTextField createNonEditableTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setEditable(false);
        return textField;
    }

    // Helper method to create non-editable combo box
    private static JComboBox<String> createNonEditableComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setEnabled(false);
        return comboBox;
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

        // JTable 的 MouseListener (點擊監聽器)
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

        JTextField recordNumberField = new JTextField(patient.getFormattedRecordCounter());
        recordNumberField.setEditable(false);
        JTextField patientNameField = new JTextField(patient.getName(), 20);
        patientNameField.setEditable(false);
        JTextField visitDateField = new JTextField(sdf.format(record.getVisitDate()), 20);
        visitDateField.setEditable(false);
        JTextField doctorField = new JTextField(record.getDoctorName(), 20);
        doctorField.setEditable(false);
        JTextField bloodTypeField = new JTextField(patient.getBloodType(), 20);
        bloodTypeField.setEditable(false);
        JTextField birthDateField = new JTextField(patient.getBirthDateString(), 20);
        birthDateField.setEditable(false);
        JCheckBox allergyField = new JCheckBox("Has Allergies", patient.hasAllergies());
        allergyField.setEnabled(false);

        JTextArea diagnosisField = new JTextArea(record.getDiagnosis(), 5, 30);
        JTextArea treatmentField = new JTextArea(record.getTreatment(), 5, 30);
        JTextArea prescriptionField = new JTextArea(record.getPrescription(), 5, 30);

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
        JButton cancelButton = new JButton("Cancel");

        saveButton.setEnabled(false);

        // cancel 監聽器
        cancelButton.addActionListener(e -> {
            diagnosisField.setText("");
            treatmentField.setText("");
            prescriptionField.setText("");
            saveButton.setEnabled(false);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
            frame.dispose();
        });

        // Document listener to enable Save button when text fields change
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
                saveButton.setEnabled(true);
            }
        };

        // Add document listeners to text areas
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
        gbc.anchor = GridBagConstraints.WEST;
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
            record.setDiagnosis(diagnosisField.getText());
            record.setTreatment(treatmentField.getText());
            record.setPrescription(prescriptionField.getText());

            JOptionPane.showMessageDialog(frame, "診斷紀錄儲存成功！", "儲存成功", JOptionPane.INFORMATION_MESSAGE);

            saveButton.setEnabled(false);
        });
    }

    /*
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
    */

    // add 更新表格
    private void updateTableModel() {
        // 清空表格
        tableModel.setRowCount(0);

        if (patients.isEmpty()) {
            // 如果沒有資料，就會顯示這行
            tableModel.addRow(new Object[]{"尚未建立資料", "-", "-", "-", "-", "-", "-", "-"});
        } else {
            // 重新添加病人資料
            for (Patient patient : patients) {
                Object[] patientData = patient.getPatientData();
                // 確保病歷號
                patientData[0] = String.format("%08d", patient.getMedicalRecordNumber());
                tableModel.addRow(patientData);
            }
        }
    }

    // search 更新表格
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

    // 選單(點擊表格後跳出)
    private void showActionSelectionDialog(Patient patient) {
        if (patient == null) {
            JOptionPane.showMessageDialog(null, "尚無資料", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = {"查看詳細基本資料", "新增就診紀錄", "查看詳細就診資料"};
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
                case "新增就診紀錄":
                    addMedicalRecord(patient);
                    break;
                /*
                case "刪除病歷":
                    deleteMedicalRecord(patient);
                    break;
                */
                case "查看詳細就診資料":
                    viewMedicalRecords(patient);
                    break;
                default:
                    break;
            }
        }
    }

    /*
    public static void main(String[] args) {
        // 假設有個Doctor用戶
        User doctorUser = new Doctor("Dr. John", "drsmith", "password123");
        MedicalRecordSystem_Doctor system = new MedicalRecordSystem_Doctor(doctorUser);
        system.setVisible(true);
    }
    */
}

