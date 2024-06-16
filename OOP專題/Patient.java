package OOP專題;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Patient implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String name;
    protected String gender;
    protected String idNumber;
    protected Date birthDate;
    protected String address;
    protected boolean hasAllergies;
    protected int medicalRecordNumber;
    protected String phone;
    protected String bloodType;
    private static int recordCounter = 1;
    private String formattedRecordCounter;
    private List<MedicalRecord> medicalRecords;
    
    public Patient(String name, String gender, String idNumber, Date birthDate, String address, String phone, String bloodType, boolean hasAllergies) {
        this.name = name;
        this.gender = gender;
        this.idNumber = idNumber;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
        this.bloodType = bloodType;
        this.hasAllergies = hasAllergies;
        this.medicalRecordNumber = recordCounter++;
        this.formattedRecordCounter = String.format("%08d", this.medicalRecordNumber);
        this.medicalRecords = new ArrayList<>();
    }

    
    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    public void addMedicalRecord(MedicalRecord medicalRecord) {
        medicalRecords.add(medicalRecord);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public void setHasAllergies(boolean hasAllergies) {
        this.hasAllergies = hasAllergies;
    }

    /*
    public void setMedicalRecordNumber(int medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }
    */
    
    public void setFormattedRecordCounter(String formattedRecordCounter) {
        this.formattedRecordCounter = formattedRecordCounter;
    }
    
    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getBloodType() {
        return bloodType;
    }

    public boolean hasAllergies() {
        return hasAllergies;
    }

    public int getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public String getFormattedRecordCounter() {
        return formattedRecordCounter;
    }
    
    public String generateMedicalRecordNumber() {
        return String.format("%08d", recordCounter);
    }
    
    public static int[] calculateAge(Date birthDate) {
        Date now = new Date();
        long ageInMillis = now.getTime() - birthDate.getTime();

        // 一年和一個月的毫秒數
        long millisPerYear = 1000L * 60 * 60 * 24 * 365;
        long millisPerMonth = 1000L * 60 * 60 * 24 * 30;

        // 初步計算年和月
        int years = (int) (ageInMillis / millisPerYear);
        int months = (int) ((ageInMillis % millisPerYear) / millisPerMonth);

        // 調整月數
        long currentMillis = birthDate.getTime() + (years * millisPerYear) + (months * millisPerMonth);
        while (currentMillis > now.getTime()) {
            if (months > 0) {
                months--;
            } else {
                years--;
                months = 11;
            }
            currentMillis = birthDate.getTime() + (years * millisPerYear) + (months * millisPerMonth);
        }
        return new int[]{years, months};
    }
    
    public int getAge() {
        return calculateAge(this.birthDate)[0];
    }

    public String getBirthDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(this.birthDate);
    }
    
    public Object[] toTableRow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String elderlyMark = (calculateAge(this.birthDate)[0] >= 65) ? "#" : "";
        String allergyMark = this.hasAllergies() ? "*" : "";
        int[] age = calculateAge(this.birthDate);
        return new Object[]{
        	this.formattedRecordCounter,
            this.name,
            this.gender,
            this.bloodType,
            sdf.format(this.birthDate),
            age[0],
            elderlyMark,
            allergyMark
        };
    }
    
    public Object[] getPatientData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int[] age = calculateAge(this.birthDate);
        return new Object[]{
            this.getMedicalRecordNumber(),
            this.getName(),
            this.getGender(),
            this.getBloodType(),
            sdf.format(this.getBirthDate()),
            age[0],
            (age[0] >= 65) ? "#" : "",
            (this.hasAllergies()) ? "*" : ""
        };
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int[] ageArray = calculateAge(this.birthDate);
        return "Medical Record Number: " + formattedRecordCounter + "\n" +
                "Name: " + name + "\n" +
                "Gender: " + gender + "\n" +
                "ID Number: " + idNumber + "\n" +
                "Birth Date: " + sdf.format(birthDate) + "\n" +
                "Age: " + ageArray[0] + " years " + ageArray[1] + " months\n" +
                "Blood Type: " + bloodType + "\n" +
                "Phone: " + phone + "\n" +
                "Address: " + address + "\n" +
                "Has Allergies: " + (hasAllergies ? "Yes" : "No");
    }
}


class ElderlyPatient extends Patient implements Serializable {
    public ElderlyPatient(String name, String gender, String idNumber, Date birthDate, String address, String phone, String bloodType, boolean hasAllergies) {
        super(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
    }

    @Override
    public String toString() {
        return super.toString() + " (Elderly, no registration fee)";
    }
}

class SpecialPatient extends Patient implements Serializable {
    public SpecialPatient(String name, String gender, String idNumber, Date birthDate, String address, String phone, String bloodType, boolean hasAllergies) {
        super(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
    }

    @Override
    public String toString() {
        return super.toString() + " (Special patient)";
    }
}

class SpecialElderlyPatient extends Patient implements Serializable {
    public SpecialElderlyPatient(String name, String gender, String idNumber, Date birthDate, String address, String phone, String bloodType, boolean hasAllergies) {
        super(name, gender, idNumber, birthDate, address, phone, bloodType, hasAllergies);
    }

    @Override
    public String toString() {
        return super.toString() + " (Elderly, no registration fee, Special patient)";
    }
}

