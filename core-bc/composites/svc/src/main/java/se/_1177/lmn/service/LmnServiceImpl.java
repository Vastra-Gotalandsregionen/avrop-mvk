package se._1177.lmn.service;

import org.apache.commons.lang3.builder.CompareToBuilder;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
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
import java.util.Collections;
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
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder() {
        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = getMedicalSupplyPrescriptions();

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

        List<DeliveryPointType> deliveryPoints = medicalSupplyDeliveryPoints.getDeliveryPoint();
        sort(deliveryPoints);

        return medicalSupplyDeliveryPoints;
    }

    /**
     * Sort by {@link DeliveryPointType#isIsClosest()} with highest priority - closer is less  and null translates to
     * isClosest == false). Second priority is address - first in alphabetical order is less and null is less than
     * non-null).
     *
     * @param deliveryPoints the list to be sorted
     */
    static void sort(List<DeliveryPointType> deliveryPoints) {
        Collections.sort(deliveryPoints, (o1, o2) -> {
            CompareToBuilder compareToBuilder = new CompareToBuilder();

            boolean isClosest1 = o1.isIsClosest() == null ? false : o1.isIsClosest();
            boolean isClosest2 = o2.isIsClosest() == null ? false : o2.isIsClosest();

            return compareToBuilder
                    .append(!isClosest1, !isClosest2) // Turn around to make closer "less".
                    .append(o1.getDeliveryPointAddress(), o2.getDeliveryPointAddress())
                    .append(o1.hashCode(), o2.hashCode())
                    .toComparison();

        });
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.getMedicalSupplyPrescriptions("", new GetMedicalSupplyPrescriptionsType());
    }

    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder() {
        return registerMedicalSupplyOrder.registerMedicalSupplyOrder("", new RegisterMedicalSupplyOrderType());
    }

}
