package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._2.CountryCodeEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryPointType;

import java.util.List;
import java.util.Objects;

public class DeliveryPointTypeWrapper extends DeliveryPointType {

    private DeliveryPointType wrapped;

    private DeliveryPointTypeWrapper(DeliveryPointType deliveryPointType) {
        this.wrapped = deliveryPointType;
    }

    public static DeliveryPointTypeWrapper of(DeliveryPointType deliveryPointType) {
        if (deliveryPointType == null) {
            return null;
        }

        return new DeliveryPointTypeWrapper(deliveryPointType);
    }

    @Override
    public String getDeliveryPointId() {
        return wrapped.getDeliveryPointId();
    }

    @Override
    public void setDeliveryPointId(String value) {
        wrapped.setDeliveryPointId(value);
    }

    @Override
    public String getDeliveryPointName() {
        return wrapped.getDeliveryPointName();
    }

    @Override
    public void setDeliveryPointName(String value) {
        wrapped.setDeliveryPointName(value);
    }

    @Override
    public String getDeliveryPointAddress() {
        return wrapped.getDeliveryPointAddress();
    }

    @Override
    public void setDeliveryPointAddress(String value) {
        wrapped.setDeliveryPointAddress(value);
    }

    @Override
    public String getDeliveryPointPostalCode() {
        return wrapped.getDeliveryPointPostalCode();
    }

    @Override
    public void setDeliveryPointPostalCode(String value) {
        wrapped.setDeliveryPointPostalCode(value);
    }

    @Override
    public String getDeliveryPointCity() {
        return wrapped.getDeliveryPointCity();
    }

    @Override
    public void setDeliveryPointCity(String value) {
        wrapped.setDeliveryPointCity(value);
    }

    @Override
    public CountryCodeEnum getCountryCode() {
        return wrapped.getCountryCode();
    }

    @Override
    public void setCountryCode(CountryCodeEnum value) {
        wrapped.setCountryCode(value);
    }

    @Override
    public List<Object> getAny() {
        return wrapped.getAny();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryPointType that = (DeliveryPointType) o;
        return Objects.equals(getDeliveryPointName(), that.getDeliveryPointName()) &&
                Objects.equals(getDeliveryPointAddress(), that.getDeliveryPointAddress()) &&
                Objects.equals(getDeliveryPointPostalCode(), that.getDeliveryPointPostalCode()) &&
                Objects.equals(getDeliveryPointCity(), that.getDeliveryPointCity()) &&
                getCountryCode() == that.getCountryCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeliveryPointName(), getDeliveryPointAddress(), getDeliveryPointPostalCode(),
                getDeliveryPointCity(), getCountryCode());
    }

    @Override
    public String toString() {
        return "{\"DeliveryPointType\":{"
                + "\"deliveryPointName\":\"" + getDeliveryPointName() + "\""
                + ", \"deliveryPointAddress\":\"" + getDeliveryPointAddress() + "\""
                + ", \"deliveryPointPostalCode\":\"" + getDeliveryPointPostalCode() + "\""
                + ", \"deliveryPointCity\":\"" + getDeliveryPointCity() + "\""
                + ", \"countryCode\":\"" + getCountryCode() + "\""
                + "}}";
    }
}
