package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.AdressType;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.OrderType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._0.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._0.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

import static se._1177.lmn.service.util.Util.isOlderThanAYear;

/**
 * @author Patrik Björk
 */
public class LmnServiceImpl implements LmnService {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint;

    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;

    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    public LmnServiceImpl(
            GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint,
            GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions,
            RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder) {
        this.medicalSupplyDeliveryPoint = medicalSupplyDeliveryPoint;
        this.medicalSupplyPrescriptions = medicalSupplyPrescriptions;
        this.registerMedicalSupplyOrder = registerMedicalSupplyOrder;
    }

    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = getMedicalSupplyPrescriptions(subjectOfCareId);

        MedicalSupplyPrescriptionsHolder holder = new MedicalSupplyPrescriptionsHolder();

        holder.supplyPrescriptionsResponse = medicalSupplyPrescriptions;

        // Separate those which have zero remaining items to order and those which have valid date older than a year

        List<PrescriptionItemType> orderableItems = new ArrayList<>();
        List<PrescriptionItemType> noLongerOrderable = new ArrayList<>();

        for (PrescriptionItemType item : medicalSupplyPrescriptions.getSubjectOfCareType().getPrescriptionItem()) {

            if (item.getNoOfRemainingOrders() <= 0 || !item.getStatus().equals(StatusEnum.AKTIV)) {
                noLongerOrderable.add(item);
            } else {
                // Check date
                XMLGregorianCalendar lastValidDate = item.getLastValidDate();
                boolean olderThanAYear = isOlderThanAYear(lastValidDate);
                if (olderThanAYear) {
                    noLongerOrderable.add(item);
                } else {
                    // Neither old nor "out of stock" or status other than AKTIV.
                    orderableItems.add(item);
                }
            }
        }

        holder.orderable = orderableItems;
        holder.noLongerOrderable = noLongerOrderable;

        return holder;
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(String postalCode) {
        GetMedicalSupplyDeliveryPointsType parameters = new GetMedicalSupplyDeliveryPointsType();

        parameters.setPostalCode(postalCode);
        parameters.setServicePointProvider(ServicePointProviderEnum.POSTNORD); // TODO Hard-coded or choice?

        GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints = medicalSupplyDeliveryPoint
                .getMedicalSupplyDeliveryPoints("", parameters);

        return medicalSupplyDeliveryPoints;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsType parameters = new GetMedicalSupplyPrescriptionsType();

        parameters.setSubjectOfCareId(subjectOfCareId);

        return medicalSupplyPrescriptions.getMedicalSupplyPrescriptions("", parameters);
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderCollectDelivery(
            DeliveryPointType deliveryPoint,
            DeliveryNotificationMethodEnum deliveryNotificationMethod,
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<String> articleNumbers) {
        RegisterMedicalSupplyOrderType parameters = new RegisterMedicalSupplyOrderType();

        OrderType order = new OrderType();

        order.setSubjectOfCareId(subjectOfCareId);
        order.setOrderByDelegate(orderByDelegate);
        order.setOrderer(orderer);

        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.getDeliveryNotificationMethod().add(deliveryNotificationMethod);
        deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryChoice.setDeliveryPoint(deliveryPoint);

        addOrderRows(articleNumbers, order, deliveryChoice);

        parameters.getOrder().add(order);


        return registerMedicalSupplyOrder.registerMedicalSupplyOrder("", parameters);
    }

    void addOrderRows(List<String> articleNumbers, OrderType order, DeliveryChoiceType deliveryChoice) {
        for (String articleNo : articleNumbers) {
            OrderRowType orderRow = new OrderRowType();

            orderRow.setDeliveryChoice(deliveryChoice);

            ArticleType article = new ArticleType();

            article.setArticleNo(articleNo);
            orderRow.setArticle(article);

            order.getOrderRow().add(orderRow);
        }
    }

    /**
     *
     * @param deliveryNotificationReceiver Should match deliveryNotificationMethod. If email is chosen
     *                                     deliveryNotificationReceiver should be an email value. If SMS is chosen
     *                                     the deliveryNotificationReceiver should be a mobile phone number. If letter
     *                                     is chosen no value is needed.
     * @param articleNumbers
     * @param
     * @return
     */
    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderHomeDelivery(
            String receiverFullName,
            String phone,
            String postalCode,
            String street,
            String doorCode,
            String city,
            String careOfAddress,
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<String> articleNumbers) {

        RegisterMedicalSupplyOrderType parameters = new RegisterMedicalSupplyOrderType();

        AdressType address = new AdressType();
        address.setCareOfAddress(careOfAddress);
        address.setCity(city);
        address.setDoorCode(doorCode);
        address.setPhone(phone);
        address.setPostalCode(postalCode);
        address.setReciever(receiverFullName); // todo Korrekt att detta är mottagarens namn?
        address.setStreet(street);

        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        deliveryChoice.setHomeDeliveryAdress(address);

        OrderType order = new OrderType();

        order.setSubjectOfCareId(subjectOfCareId);
        order.setOrderByDelegate(orderByDelegate);
        order.setOrderer(orderer);

        addOrderRows(articleNumbers, order, deliveryChoice);

        parameters.getOrder().add(order);

        return registerMedicalSupplyOrder.registerMedicalSupplyOrder("", parameters);
    }
}
