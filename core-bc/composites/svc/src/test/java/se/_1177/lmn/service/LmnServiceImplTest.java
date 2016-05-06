package se._1177.lmn.service;

import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import riv.crm.selfservice.medicalsupply._0.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._0.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._0.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Björk
 */
public class LmnServiceImplTest {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoints;
    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;
    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    @Before
    public void setup() {

        medicalSupplyPrescriptions = mock(GetMedicalSupplyPrescriptionsResponderInterface.class);

        GetMedicalSupplyPrescriptionsResponseType response = new GetMedicalSupplyPrescriptionsResponseType();

        SubjectOfCareType subjectOfCare = new SubjectOfCareType();

        // Orderable
        PrescriptionItemType item1 = new PrescriptionItemType();
        item1.setNextEarliestOrderDate(null);
        item1.setStatus(StatusEnum.AKTIV);
        item1.setNoOfRemainingOrders(1);
        item1.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Not orderable, MAKULERAD
        PrescriptionItemType item2 = new PrescriptionItemType();
        item2.setNextEarliestOrderDate(null);
        item2.setStatus(StatusEnum.MAKULERAD);
        item2.setNoOfRemainingOrders(1);
        item2.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Not orderable, UTGÅTT
        PrescriptionItemType item3 = new PrescriptionItemType();
        item3.setNextEarliestOrderDate(null);
        item3.setStatus(StatusEnum.UTGÅTT);
        item3.setNoOfRemainingOrders(1);
        item3.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Not orderable, LEVERERAD
        PrescriptionItemType item4 = new PrescriptionItemType();
        item4.setNextEarliestOrderDate(null);
        item4.setStatus(StatusEnum.LEVERERAD);
        item4.setNoOfRemainingOrders(1);
        item4.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Orderable (even though it's not really orderable (not yet) but still it's shown in the same table) - next
        // earliest order date in future
        PrescriptionItemType item5 = new PrescriptionItemType();

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, 1);

        item5.setNextEarliestOrderDate(getCalendar(calendar));
        item5.setStatus(StatusEnum.AKTIV);
        item5.setNoOfRemainingOrders(1);
        item5.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Not orderable - no remaining orders
        PrescriptionItemType item6 = new PrescriptionItemType();
        item6.setNextEarliestOrderDate(null);
        item6.setStatus(StatusEnum.AKTIV);
        item6.setNoOfRemainingOrders(0);
        item6.setLastValidDate(getCalendar(new GregorianCalendar()));

        // Not orderable - last valid date older than a year
        PrescriptionItemType item7 = new PrescriptionItemType();
        item7.setNextEarliestOrderDate(null);
        item7.setStatus(StatusEnum.AKTIV);
        item7.setNoOfRemainingOrders(1);
        item7.setLastValidDate(getCalendar(new GregorianCalendar()));

        calendar = new GregorianCalendar();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.DATE, -1);

        item7.setLastValidDate(getCalendar(calendar));


        subjectOfCare.getPrescriptionItem().add(item1);
        subjectOfCare.getPrescriptionItem().add(item2);
        subjectOfCare.getPrescriptionItem().add(item3);
        subjectOfCare.getPrescriptionItem().add(item4);
        subjectOfCare.getPrescriptionItem().add(item5);
        subjectOfCare.getPrescriptionItem().add(item6);
        subjectOfCare.getPrescriptionItem().add(item7);

        response.setSubjectOfCareType(subjectOfCare);

        when(medicalSupplyPrescriptions.getMedicalSupplyPrescriptions(
                anyString(),
                any(GetMedicalSupplyPrescriptionsType.class)))
                .thenReturn(response);

    }

    @Test
    public void getMedicalSupplyPrescriptionsHolder() throws Exception {

        LmnServiceImpl service = new LmnServiceImpl(
                medicalSupplyDeliveryPoints,
                medicalSupplyPrescriptions,
                registerMedicalSupplyOrder);

        MedicalSupplyPrescriptionsHolder holder = service.getMedicalSupplyPrescriptionsHolder("aslkjsdfljk");

        assertEquals(2, holder.getOrderable().size());
        assertEquals(5, holder.getNoLongerOrderable().size());
    }

    private XMLGregorianCalendar getCalendar(GregorianCalendar calendar) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        return xmlGregorianCalendar;
    }
}