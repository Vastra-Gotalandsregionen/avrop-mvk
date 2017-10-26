package se._1177.lmn.configuration.counties;

public class County {
    private String countyCode;
    private String getMedicalSupplyDeliveryPointsAddress;
    private String getMedicalSupplyPrescriptionsAddress;
    private String registerMedicalSupplyOrderAddress;
    private String customerServicePhoneNumber;
    private String customerServiceInfo;
    private String rtjpLogicalAddress;
    private String receptionHsaId;
    private String confirmationMessage;

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getGetMedicalSupplyDeliveryPointsAddress() {
        return getMedicalSupplyDeliveryPointsAddress;
    }

    public void setGetMedicalSupplyDeliveryPointsAddress(String getMedicalSupplyDeliveryPointsAddress) {
        this.getMedicalSupplyDeliveryPointsAddress = getMedicalSupplyDeliveryPointsAddress;
    }

    public String getGetMedicalSupplyPrescriptionsAddress() {
        return getMedicalSupplyPrescriptionsAddress;
    }

    public void setGetMedicalSupplyPrescriptionsAddress(String getMedicalSupplyPrescriptionsAddress) {
        this.getMedicalSupplyPrescriptionsAddress = getMedicalSupplyPrescriptionsAddress;
    }

    public String getRegisterMedicalSupplyOrderAddress() {
        return registerMedicalSupplyOrderAddress;
    }

    public void setRegisterMedicalSupplyOrderAddress(String registerMedicalSupplyOrderAddress) {
        this.registerMedicalSupplyOrderAddress = registerMedicalSupplyOrderAddress;
    }

    public String getCustomerServicePhoneNumber() {
        return customerServicePhoneNumber;
    }

    public void setCustomerServicePhoneNumber(String customerServicePhoneNumber) {
        this.customerServicePhoneNumber = customerServicePhoneNumber;
    }

    public String getCustomerServiceInfo() {
        return customerServiceInfo;
    }

    public void setCustomerServiceInfo(String customerServiceInfo) {
        this.customerServiceInfo = customerServiceInfo;
    }

    public String getRtjpLogicalAddress() {
        return rtjpLogicalAddress;
    }

    public void setRtjpLogicalAddress(String rtjpLogicalAddress) {
        this.rtjpLogicalAddress = rtjpLogicalAddress;
    }

    public String getReceptionHsaId() {
        return receptionHsaId;
    }

    public void setReceptionHsaId(String receptionHsaId) {
        this.receptionHsaId = receptionHsaId;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}