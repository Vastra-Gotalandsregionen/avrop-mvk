package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import se._1177.lmn.service.LmnService;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CollectDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectDeliveryController.class);

    private String zip;

    @Autowired
    private LmnService lmnService;

    @Autowired
    private UserProfileController userProfileController;

    private String chosenDeliveryPoint;
    private List<DeliveryPointType> deliveryPoints;

    @PostConstruct
    public void init() {
        // Default zip is from user profile. It may be overridden if user chooses so.
        zip = userProfileController.getUserProfile().getUserProfile().getZip();

        List<SelectItemGroup> deliverySelectItems = getDeliverySelectItems();

        if (deliverySelectItems != null
                && deliverySelectItems.size() > 0
                && deliverySelectItems.get(0).getSelectItems() != null
                && deliverySelectItems.get(0).getSelectItems().length > 0) {

            chosenDeliveryPoint = (String) deliverySelectItems.get(0).getSelectItems()[0].getValue();
        }
    }

    public List<DeliveryPointType> getAllDeliveryPoints() {

        // Loads these once per session. Be careful about memory consumption under load.
        if (deliveryPoints == null) {
            deliveryPoints = lmnService.getMedicalSupplyDeliveryPoints(zip).getDeliveryPoint();
        }

        return deliveryPoints;
    }

    public List<SelectItemGroup> getDeliverySelectItems() {

        SelectItemGroup group1 = new SelectItemGroup("Närmaste ombud");
        SelectItemGroup group2 = new SelectItemGroup("Övriga ombud till ditt postnummer");

        List<SelectItem> toGroup1 = new ArrayList<>();
        List<SelectItem> toGroup2 = new ArrayList<>();

        for (DeliveryPointType deliveryPoint : getAllDeliveryPoints()) {

            String label = deliveryPoint.getDeliveryPointAddress()
                    + ", " + deliveryPoint.getDeliveryPointName()
                    + ", " + deliveryPoint.getDeliveryPointCity();

            SelectItem selectItem = new SelectItem(deliveryPoint.getDeliveryPointId(), label);

            if (deliveryPoint.isIsClosest()) {
                toGroup1.add(selectItem);
            } else {
                toGroup2.add(selectItem);
            }
        }

        group1.setSelectItems(toGroup1.toArray(new SelectItem[0]));
        group2.setSelectItems(toGroup2.toArray(new SelectItem[0]));

        List<SelectItemGroup> toReturn = new ArrayList<>();

        toReturn.add(group1);
        toReturn.add(group2);

        return toReturn;
    }

    public String getChosenDeliveryPoint() {
        return chosenDeliveryPoint;
    }

    public void setChosenDeliveryPoint(String chosenDeliveryPoint) {
        this.chosenDeliveryPoint = chosenDeliveryPoint;
    }

    public String toVerifyDelivery() {
        return "collectDelivery";
    }
}
