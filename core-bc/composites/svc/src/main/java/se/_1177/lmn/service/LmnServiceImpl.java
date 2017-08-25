package se._1177.lmn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.OrderItemType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.OrderType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._1.StatusEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._1.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._1.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._1.GetMedicalSupplyDeliveryPointsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._1.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._1.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se._1177.lmn.service.util.Util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This implementation handles all communication with the source system which is responsible for prescriptions,
 * delivery places and registering orders. This class uses web service proxy interfaces to communicate with the external
 * system.
 *
 * @author Patrik Björk
 */
public class LmnServiceImpl implements LmnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LmnServiceImpl.class);

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint;

    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;

    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    private Map<String, DeliveryPointType> deliveryPointIdToDeliveryPoint = new HashMap<>();

    private String logicalAddress;

    private String customerServicePhoneNumber;
    private String customerServiceInfo;

    public LmnServiceImpl(
            GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint,
            GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions,
            RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder,
            String logicalAddress,
            String customerServicePhoneNumber,
            String customerServiceInfo) {
        this.medicalSupplyDeliveryPoint = medicalSupplyDeliveryPoint;
        this.medicalSupplyPrescriptions = medicalSupplyPrescriptions;
        this.registerMedicalSupplyOrder = registerMedicalSupplyOrder;
        this.logicalAddress = logicalAddress;
        this.customerServicePhoneNumber = customerServicePhoneNumber;
        this.customerServiceInfo = customerServiceInfo;
    }

    /**
     * Fetches {@link PrescriptionItemType}s for a specific person. The prescriptions are separated into orderable items
     * and no longer orderable items. A prescription item is no longer orderable if it either has status other than
     * AKTIV, has number of remaining orders less than or equal to zero, or last valid date is before today. The items
     * are sorted according to how "orderable" they are (see
     * {@link LmnServiceImpl#getSortNumber(riv.crm.selfservice.medicalsupply._1.PrescriptionItemType)}).
     *
     * @param subjectOfCareId the subject of care id of the user or the inhabitant the user is delegate for
     * @return the fetched, sorted and separated {@link PrescriptionItemType}s contained in a
     * {@link MedicalSupplyPrescriptionsHolder}
     */
    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsResponseType response = getMedicalSupplyPrescriptions(subjectOfCareId);

        MedicalSupplyPrescriptionsHolder holder = new MedicalSupplyPrescriptionsHolder();

        holder.supplyPrescriptionsResponse = response;

        // Separate those which have zero remaining items to order and those which have last valid date older than a
        // year.

        List<PrescriptionItemType> orderableItems = new ArrayList<>();
        List<PrescriptionItemType> noLongerOrderable = new ArrayList<>();

        if (response.getResultCode().equals(ResultCodeEnum.OK)) {
            List<PrescriptionItemType> prescriptions = response.getSubjectOfCareType().getPrescriptionItem();

            for (PrescriptionItemType item : prescriptions) {

                if (item.getNoOfRemainingOrders() <= 0 || !item.getStatus().equals(StatusEnum.AKTIV)) {
                    noLongerOrderable.add(item);
                } else {
                    // Check date
                    XMLGregorianCalendar lastValidDate = item.getLastValidDate();
                    boolean beforeToday = Util.isBeforeToday(lastValidDate);
                    if (beforeToday) {
                        noLongerOrderable.add(item);
                    } else {
                        // Neither old nor "out of stock" or status other than AKTIV.
                        orderableItems.add(item);
                    }
                }
            }

            sortByOrderableTodayAndArticleName(orderableItems);

            Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem =
                    latestOrderItemsByArticleNoAndPrescriptionItem(response);

            holder.orderable = orderableItems;
            holder.noLongerOrderable = noLongerOrderable;
            holder.latestOrderItemsByArticleNoAndPrescriptionItem = latestOrderItemsByArticleNoAndPrescriptionItem;
        }

        return holder;
    }

    static Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem(
            GetMedicalSupplyPrescriptionsResponseType prescriptionsResponseType) {

        Map<String, OrderItemType> latestOrderItemsByArticleNo = new HashMap<>();

        for (OrderItemType item : prescriptionsResponseType.getSubjectOfCareType().getOrderItem()) {
            String articleNo = item.getArticle().getArticleNo();
            if (!latestOrderItemsByArticleNo.containsKey(articleNo)) {
                latestOrderItemsByArticleNo.put(articleNo, item);
                continue;
            }

            OrderItemType storedInMap = latestOrderItemsByArticleNo.get(articleNo);
            if (storedInMap.getOrderDate().compare(item.getOrderDate()) < 0) {
                // The storedInMap is older (less) -> replace with the newer
                latestOrderItemsByArticleNo.put(articleNo, item);
            }
        }

        Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem =
                latestOrderItemsByArticleNo.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getPrescriptionItemId(),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        // Go through each prescriptionItem and remove those articles which weren't ordered last (there may be articles
        // ordered last out the the order items of exactly that article, but the article may not have been ordered at
        // all at the last order. Then it should be removed. So we first find out the last date for each prescription
        // item.

        for (Map.Entry<String, Map<String, OrderItemType>> entry
                : latestOrderItemsByArticleNoAndPrescriptionItem.entrySet()) {

            XMLGregorianCalendar latestOrderDate;
            Optional<XMLGregorianCalendar> max = entry.getValue().values().stream().map(OrderItemType::getOrderDate).max(XMLGregorianCalendar::compare);

            if (max.isPresent()) {
                latestOrderDate = max.get();
            } else {
                throw new RuntimeException("Order dates are expected.");
            }

            // Remove all which are not ordered at the last order date.
            entry.getValue().entrySet().removeIf(e -> !e.getValue().getOrderDate().equals(latestOrderDate));
        }

        return latestOrderItemsByArticleNoAndPrescriptionItem;
    }

    static void sortByOrderableTodayAndArticleName(List<PrescriptionItemType> orderableItems) {

        Comparator<PrescriptionItemType> comparator = Comparator.comparing(o -> getSortNumber(o));
        comparator = comparator.thenComparing(o -> o.getArticle().getArticleName());

        orderableItems.sort(comparator);
    }

    private static Integer getSortNumber(PrescriptionItemType item) {
        if (!item.getArticle().isIsOrderable()) {
            return 2;
        }

        if (isAfterToday(item.getNextEarliestOrderDate())) {
            return 1;
        }

        return 0;
    }

    /**
     * This method's only purpose is to delay the external web service from going into "sleep mode" where it becomes
     * slower than preferred. We allow it to go into sleep mode when time is 3.XX in the night only. This method is
     * executed by schedule.
     */
    // Every fifteen minutes all the time except when time is 3-something in the night.
    @Scheduled(cron = "0 0/15 0-2,4-23 * * ?")
    public void keepWebServiceAwake() {
        // This delays the external web service going into "sleep mode" where it becomes slower than preferred. We allow
        // it to go into sleep mode when time is 3.XX in the night only.
        try {
            LOGGER.info("Scheduled operation...");
            getMedicalSupplyDeliveryPoints(ServicePointProviderEnum.POSTNORD, "41648");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Fetches {@link DeliveryPointType}s. It also has the side-effect of storing entries in a {@link Map} where the id
     * of a {@link DeliveryPointType} is mapped to the {@link DeliveryPointType} itself, for reuse at a later stage.
     *
     * @param provider the {@link ServicePointProviderEnum}
     * @param postalCode the postal code
     * @return a {@link GetMedicalSupplyDeliveryPointsResponseType} containing {@link DeliveryPointType}s.
     */
    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider,
                                                                                     String postalCode) {
        GetMedicalSupplyDeliveryPointsType parameters = new GetMedicalSupplyDeliveryPointsType();

        parameters.setPostalCode(postalCode);
        parameters.setServicePointProvider(provider);

        GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints = medicalSupplyDeliveryPoint
                .getMedicalSupplyDeliveryPoints(logicalAddress, parameters);

        for (DeliveryPointType deliveryPoint : medicalSupplyDeliveryPoints.getDeliveryPoint()) {
            deliveryPointIdToDeliveryPoint.put(deliveryPoint.getDeliveryPointId(), deliveryPoint);
        }

        return medicalSupplyDeliveryPoints;
    }

    /**
     * Fetches the {@link PrescriptionItemType}s available to a user or the inhabitant the user is delegate for.
     *
     * @param subjectOfCareId the subject of care id of the user or the inhabitant the user is delegate for.
     * @return a {@link GetMedicalSupplyDeliveryPointsResponseType} containing the {@link PrescriptionItemType}
     */
    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsType parameters = new GetMedicalSupplyPrescriptionsType();

        parameters.setSubjectOfCareId(subjectOfCareId);

        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = this.medicalSupplyPrescriptions
                .getMedicalSupplyPrescriptions(logicalAddress, parameters);

        return medicalSupplyPrescriptions;
    }

    /**
     * Sends a request to the source system to register a medical supply order. It assembles a
     * {@link RegisterMedicalSupplyOrderType} which is sent to the source system.
     *
     * @param subjectOfCareId the subject of care id of the user or the inhabitant the user is delegate for
     * @param orderByDelegate whether the order is made by a delegate
     * @param orderer the name of the person who makes the order
     * @param orderRows the {@link OrderRowType}s which are ordered
     * @param deliveryChoicePerItem the {@link DeliveryChoiceType} for each {@link PrescriptionItemType}
     * @return the {@link RegisterMedicalSupplyOrderResponseType}
     */
    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<OrderRowType> orderRows,
            Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {
        RegisterMedicalSupplyOrderType parameters = new RegisterMedicalSupplyOrderType();

        OrderType order = new OrderType();

        order.setSubjectOfCareId(subjectOfCareId);
        order.setOrderByDelegate(orderByDelegate);
        order.setOrderer(orderer);

        order.getOrderRow().addAll(orderRows);

        parameters.setOrder(order);

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        RegisterMedicalSupplyOrderResponseType response = registerMedicalSupplyOrder
                .registerMedicalSupplyOrder(logicalAddress, parameters);

        stopWatch.stop();

        LOGGER.info("Time to registerMedicalSupplyOrder: " + stopWatch.getTotalTimeMillis() + " millis.");

        return response;
    }

    @Override
    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return deliveryPointIdToDeliveryPoint.get(deliveryPointId);
    }

    @Override
    public String getCustomerServicePhoneNumber() {
        return customerServicePhoneNumber;
    }

    public void setCustomerServicePhoneNumber(String customerServicePhoneNumber) {
        this.customerServicePhoneNumber = customerServicePhoneNumber;
    }

    @Override
    public String getCustomerServiceInfo() {
        return customerServiceInfo;
    }

    public void setCustomerServiceInfo(String customerServiceInfo) {
        this.customerServiceInfo = customerServiceInfo;
    }

    public static boolean isAfterToday(XMLGregorianCalendar date) {
        return Util.isAfterToday(date);
    }
}

