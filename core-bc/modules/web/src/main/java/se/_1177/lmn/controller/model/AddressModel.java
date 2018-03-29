package se._1177.lmn.controller.model;

import mvk.itintegration.userprofile._2.UserProfileType;
import riv.crm.selfservice.medicalsupply._1.AddressType;
import se._1177.lmn.controller.UserProfileController;

import javax.annotation.PostConstruct;

public class AddressModel {

    private UserProfileController userProfileController;

    private String fullName;
    private String coAddress;
    private String address;
    private String zip;
    private String city;
    private String doorCode;
    private String phoneNumber;

    public AddressModel(UserProfileController userProfileController) {
        this.userProfileController = userProfileController;
    }

    @PostConstruct
    public void init() {
        if (address == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                address = userProfile.getStreetAddress();
            }
        }

        if (fullName == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                fullName = userProfile.getFirstName() + " " + userProfile.getLastName();
            }
        }

        if (zip == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                zip = userProfile.getZip();
            }
        }

        if (city == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                city = userProfile.getCity();
            }
        }

        if (phoneNumber == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                phoneNumber = userProfile.getPhoneNumber();

                if (phoneNumber == null || "".equals(phoneNumber)) {
                    phoneNumber = userProfile.getMobilePhoneNumber();
                }
            }
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCoAddress() {
        return coAddress;
    }

    public void setCoAddress(String coAddress) {
        this.coAddress = coAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDoorCode() {
        return doorCode;
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AddressType toAddressType() {
        AddressType address = new AddressType();

        address.setReceiver(this.getFullName());
        address.setCareOfAddress(this.getCoAddress());
        address.setStreet(this.getAddress());
        address.setPostalCode(this.getZip());
        address.setCity(this.getCity());
        address.setPhone(this.getPhoneNumber());
        address.setDoorCode(this.getDoorCode());

        return address;
    }
}
