package OOP專題;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MedicalRecord implements Serializable {
	private final int medicalRecordNumber;
    private Date visitDate;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String prescription;
    private List<MedicalRecord> medicalRecords;

    public MedicalRecord(String diagnosis, String treatment, String prescription, int medicalRecordNumber, String doctorName, Date visitDate) {
        this.medicalRecordNumber = medicalRecordNumber;
        this.visitDate = visitDate;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
    }
    
    // 添加新病歷紀錄的方法，明確指定病歷號
    public void addMedicalRecord(String diagnosis, String treatment, String prescription, int medicalRecordNumber, String doctorName, Date visitDate) {
        MedicalRecord record = new MedicalRecord(diagnosis, treatment, prescription, medicalRecordNumber, doctorName, visitDate);
        medicalRecords.add(record);
    }

    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
    
    /*
    public void setMedicalRecordNumber(int medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }
    */
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }
    
    public int getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public String getPrescription() {
        return prescription;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("Medical Record Number: %d, Visit Date: %s, Doctor: %s, Diagnosis: %s, Treatment: %s, Prescription: %s",
                medicalRecordNumber, dateFormat.format(visitDate), doctorName, diagnosis, treatment, prescription);
    }
}

