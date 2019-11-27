package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._2.AddressType;

import java.util.Objects;

public class AddressTypeWrapper extends AddressType {

    private AddressType wrapped;

    private AddressTypeWrapper(AddressType addressType) {
        this.wrapped = addressType;
    }

    public static AddressTypeWrapper of(AddressType addressType) {
        if (addressType == null) {
            return null;
        }

        return new AddressTypeWrapper(addressType);
    }

    @Override
    public String getReceiver() {
        return wrapped.getReceiver();
    }

    @Override
    public void setReceiver(String value) {
        wrapped.setReceiver(value);
    }

    @Override
    public String getCareOfAddress() {
        return wrapped.getCareOfAddress();
    }

    @Override
    public void setCareOfAddress(String value) {
        wrapped.setCareOfAddress(value);
    }

    @Override
    public String getStreet() {
        return wrapped.getStreet();
    }

    @Override
    public void setStreet(String value) {
        wrapped.setStreet(value);
    }

    @Override
    public String getPostalCode() {
        return wrapped.getPostalCode();
    }

    @Override
    public void setPostalCode(String value) {
        wrapped.setPostalCode(value);
    }

    @Override
    public String getCity() {
        return wrapped.getCity();
    }

    @Override
    public void setCity(String value) {
        wrapped.setCity(value);
    }

    @Override
    public String getDoorCode() {
        return wrapped.getDoorCode();
    }

    @Override
    public void setDoorCode(String value) {
        wrapped.setDoorCode(value);
    }

    @Override
    public String getPhone() {
        return wrapped.getPhone();
    }

    @Override
    public void setPhone(String value) {
        wrapped.setPhone(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressType that = (AddressType) o;
        return Objects.equals(getReceiver(), that.getReceiver()) &&
                Objects.equals(getCareOfAddress(), that.getCareOfAddress()) &&
                Objects.equals(getStreet(), that.getStreet()) &&
                Objects.equals(getPostalCode(), that.getPostalCode()) &&
                Objects.equals(getCity(), that.getCity()) &&
                Objects.equals(getDoorCode(), that.getDoorCode()) &&
                Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getReceiver(), getCareOfAddress(), getStreet(), getPostalCode(), getCity(), getDoorCode(),
                getPhone());
    }

    @Override
    public String toString() {
        return "{\"AddressType\":{"
                + "\"receiver\":\"" + getReceiver() + "\""
                + ", \"careOfAddress\":\"" + getCareOfAddress() + "\""
                + ", \"street\":\"" + getStreet() + "\""
                + ", \"postalCode\":\"" + getPostalCode() + "\""
                + ", \"city\":\"" + getCity() + "\""
                + ", \"doorCode\":\"" + getDoorCode() + "\""
                + ", \"phone\":\"" + getPhone() + "\""
                + "}}";
    }
}
