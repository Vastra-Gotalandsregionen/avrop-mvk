package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
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
public class LmnServiceFacadeImpl implements LmnServiceFacade {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint;

    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;

    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    public LmnServiceFacadeImpl(
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

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints() {
        return medicalSupplyDeliveryPoint.getMedicalSupplyDeliveryPoints("", new GetMedicalSupplyDeliveryPointsType());
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.getMedicalSupplyPrescriptions("", new GetMedicalSupplyPrescriptionsType());
    }

    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder() {
        return registerMedicalSupplyOrder.registerMedicalSupplyOrder("", new RegisterMedicalSupplyOrderType());
    }

}
