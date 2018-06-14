package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._1.AddressType;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Objects;

public class DeliveryChoiceTypeWrapper extends DeliveryChoiceType implements Comparable<DeliveryChoiceTypeWrapper> {

    private DeliveryChoiceType wrapped;

    private DeliveryChoiceTypeWrapper(DeliveryChoiceType deliveryChoiceType) {
        this.wrapped = deliveryChoiceType;
    }

    public static DeliveryChoiceTypeWrapper of(DeliveryChoiceType deliveryChoiceType) {
        return new DeliveryChoiceTypeWrapper(deliveryChoiceType);
    }

    @Override
    public String getDeliveryMethodId() {
        return wrapped.getDeliveryMethodId();
    }

    @Override
    public void setDeliveryMethodId(String value) {
        wrapped.setDeliveryMethodId(value);
    }

    @Override
    public DeliveryMethodEnum getDeliveryMethod() {
        return wrapped.getDeliveryMethod();
    }

    @Override
    public void setDeliveryMethod(DeliveryMethodEnum value) {
        wrapped.setDeliveryMethod(value);
    }

    @Override
    public AddressType getHomeDeliveryAddress() {
        return wrapped.getHomeDeliveryAddress();
    }

    @Override
    public void setHomeDeliveryAddress(AddressType value) {
        wrapped.setHomeDeliveryAddress(value);
    }

    @Override
    public DeliveryPointType getDeliveryPoint() {
        return wrapped.getDeliveryPoint();
    }

    @Override
    public void setDeliveryPoint(DeliveryPointType value) {
        wrapped.setDeliveryPoint(value);
    }

    @Override
    public JAXBElement<DeliveryNotificationMethodEnum> getDeliveryNotificationMethod() {
        return wrapped.getDeliveryNotificationMethod();
    }

    @Override
    public void setDeliveryNotificationMethod(JAXBElement<DeliveryNotificationMethodEnum> value) {
        wrapped.setDeliveryNotificationMethod(value);
    }

    @Override
    public String getDeliveryNotificationReceiver() {
        return wrapped.getDeliveryNotificationReceiver();
    }

    @Override
    public void setDeliveryNotificationReceiver(String value) {
        wrapped.setDeliveryNotificationReceiver(value);
    }

    @Override
    public AddressType getInvoiceAddress() {
        return wrapped.getInvoiceAddress();
    }

    @Override
    public void setInvoiceAddress(AddressType value) {
        wrapped.setInvoiceAddress(value);
    }

    @Override
    public String getDeliveryComment() {
        return wrapped.getDeliveryComment();
    }

    @Override
    public void setDeliveryComment(String value) {
        wrapped.setDeliveryComment(value);
    }

    @Override
    public String getContactPerson() {
        return wrapped.getContactPerson();
    }

    @Override
    public void setContactPerson(String value) {
        wrapped.setContactPerson(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryChoiceType that = (DeliveryChoiceType) o;
        return getDeliveryMethod() == that.getDeliveryMethod() &&
                Objects.equals(wrap(getHomeDeliveryAddress()), wrap(that.getHomeDeliveryAddress())) &&
                Objects.equals(wrap(getDeliveryPoint()), wrap(that.getDeliveryPoint())) &&
                Objects.equals(extract(getDeliveryNotificationMethod()), extract(that.getDeliveryNotificationMethod())) &&
                Objects.equals(getDeliveryNotificationReceiver(), that.getDeliveryNotificationReceiver()) &&
                Objects.equals(getContactPerson(), that.getContactPerson()) &&
                Objects.equals(getDeliveryComment(), that.getDeliveryComment()) &&
                Objects.equals(wrap(getInvoiceAddress()), wrap(that.getInvoiceAddress()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getDeliveryMethod(),
                wrap(getHomeDeliveryAddress()),
                wrap(getDeliveryPoint()),
                extract(getDeliveryNotificationMethod()),
                getDeliveryNotificationReceiver(),
                getContactPerson(),
                getDeliveryComment(),
                wrap(getInvoiceAddress())
        );
    }

    @Override
    public String toString() {
        return "{\"DeliveryChoiceType\":{"
                + "\"deliveryMethod\":\"" + getDeliveryMethod() + "\""
                + ", \"homeDeliveryAddress\":" + wrap(getHomeDeliveryAddress())
                + ", \"deliveryPoint\":" + wrap(getDeliveryPoint())
                + ", \"deliveryNotificationMethod\":" + extract(getDeliveryNotificationMethod())
                + ", \"deliveryNotificationReceiver\":\"" + getDeliveryNotificationReceiver() + "\""
                + ", \"contactPerson\":" + getContactPerson()
                + ", \"deliveryComment\":" + getDeliveryComment()
                + ", \"invoiceAddress\":" + wrap(getInvoiceAddress())
                + "}}";
    }

    @Override
    public int compareTo(DeliveryChoiceTypeWrapper o) {
        return toString().compareTo(o.toString());
    }

    private DeliveryNotificationMethodEnum extract(JAXBElement<DeliveryNotificationMethodEnum> element) {
        return element != null ? element.getValue() : null;
    }

    private DeliveryPointTypeWrapper wrap(DeliveryPointType deliveryPoint) {
        return DeliveryPointTypeWrapper.of(deliveryPoint);
    }

    private AddressTypeWrapper wrap(AddressType addressType) {
        return AddressTypeWrapper.of(addressType);
    }

    /*@Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return wrapped.equals(obj);
    }*/
}
