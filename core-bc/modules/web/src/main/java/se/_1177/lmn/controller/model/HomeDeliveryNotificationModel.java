package se._1177.lmn.controller.model;

import riv.crm.selfservice.medicalsupply._1.ArticleType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.service.util.Util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeDeliveryNotificationModel implements Serializable {

    private final List<PrescriptionItemType> prescriptionItemTypes;
    private final List<DeliveryNotificationMethodEnum> overlappingDeliveryNotificationMethods;
    private final DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod;
    private final String componentIdPrefix;

    private Map<PrescriptionItemType, List<String>> deliveryNotificationMethodsPerItem;
    private Map<PrescriptionItemType, String> chosenDeliveryNotificationMethod;

    private String smsNumber;
    private String email;
    private String phoneNumber;

    private PrescriptionItemType fictionalItem = new PrescriptionItemType();

    {
        ArticleType fictionalArticle = new ArticleType();
        fictionalArticle.setArticleName(null);
        fictionalArticle.setArticleNo("fictionalArticleNo");
        fictionalItem.setArticle(fictionalArticle);
    }

    public HomeDeliveryNotificationModel(List<PrescriptionItemType> prescriptionItemTypes,
                                         DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod,
                                         String smsNumber,
                                         String email,
                                         String phoneNumber,
                                         String componentIdPrefix) {
        this.prescriptionItemTypes = prescriptionItemTypes;

        this.overlappingDeliveryNotificationMethods = findOverlappingDeliveryNotificationMethods(prescriptionItemTypes);
        this.preferredDeliveryNotificationMethod = preferredDeliveryNotificationMethod;

        this.smsNumber = smsNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;

        this.componentIdPrefix = componentIdPrefix;
    }

    public Map<PrescriptionItemType, List<String>> getAvailableDeliveryNotificationMethodsPerItem() {
        if (deliveryNotificationMethodsPerItem == null) {
            List<DeliveryNotificationMethodEnum> overlapping = this.overlappingDeliveryNotificationMethods;

            if (overlapping.size() > 0) {

                List<String> commonDeliveryNotificationMethods = overlapping
                        .stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());

                deliveryNotificationMethodsPerItem = new HashMap<>();

                deliveryNotificationMethodsPerItem.put(this.fictionalItem, commonDeliveryNotificationMethods);
            } else {

                // It's important to preserve order as validation uses a count variable which is dependent on the order
                // being the same.
                deliveryNotificationMethodsPerItem = new LinkedHashMap<>();

                // We want to present all items which have HEMLEVERANS as delivery method.
                this.prescriptionItemTypes
                        .stream()
                        .filter(prescriptionItemType -> prescriptionItemType.getDeliveryAlternative() // Keep those with at least one home delivery alternative.
                                .stream()
                                .filter(deliveryAlternativeType -> deliveryAlternativeType.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS))
                                .count() > 0)
                        .collect(Collectors.toList())
                        .forEach(prescriptionItemType -> {
                            prescriptionItemType.getDeliveryAlternative()
                                    .stream()
                                    .filter(alternative ->
                                            alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)
                                                    && alternative.getDeliveryNotificationMethod() != null
                                                    && alternative.getDeliveryNotificationMethod().size() > 0)
                                    .forEach(alternative -> {
                                        // We found the HEMLEVERANS delivery alternative WITH delivery notifications (we assume there's only one).
                                        deliveryNotificationMethodsPerItem.put(prescriptionItemType,
                                                alternative.getDeliveryNotificationMethod()
                                                        .stream()
                                                        .map(Enum::name)
                                                        .collect(Collectors.toCollection(ArrayList<String>::new)));
                                    });
                        });
            }
        }

        return deliveryNotificationMethodsPerItem;
    }

    public String getChosenDeliveryNotificationMethod(PrescriptionItemType prescriptionItemType) {

        if (overlappingDeliveryNotificationMethods.size() > 0) {
            if (chosenDeliveryNotificationMethod.size() != 1) {
                throw new IllegalStateException("If there are overlapping notification methods only one chosenDeliveryNotificationMethod is expected.");
            }

            String chosenForAll = chosenDeliveryNotificationMethod.values().iterator().next();

            return chosenForAll;
        } else {
            return getChosenDeliveryNotificationMethod().get(prescriptionItemType);
        }

    }

    public Map<PrescriptionItemType, String> getChosenDeliveryNotificationMethod() {
        if (chosenDeliveryNotificationMethod == null) {
            initChosenDeliveryNotificationMethod();
        }

        return chosenDeliveryNotificationMethod;
    }

    private void initChosenDeliveryNotificationMethod() {
        chosenDeliveryNotificationMethod = new HashMap<>();

        Map<PrescriptionItemType, List<String>> availableDeliveryNotificationMethodsPerItem =
                getAvailableDeliveryNotificationMethodsPerItem();

        availableDeliveryNotificationMethodsPerItem.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().size() > 0)
                .forEach(entry -> {
                    if (entry.getValue().contains(preferredDeliveryNotificationMethod.name())) {
                        chosenDeliveryNotificationMethod.put(entry.getKey(), preferredDeliveryNotificationMethod.name());
                    } else {
                        // In case the preferred one isn't available.
                        String firstAvailable = entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                        chosenDeliveryNotificationMethod.put(entry.getKey(), firstAvailable);
                    }
                });
    }

    private List<DeliveryNotificationMethodEnum> findOverlappingDeliveryNotificationMethods(
            List<PrescriptionItemType> prescriptionItemTypes) {

        List<DeliveryNotificationMethodEnum> remaining = new ArrayList<>(
                Arrays.asList(DeliveryNotificationMethodEnum.values()));

        if (prescriptionItemTypes.size() == 0) {
            remaining.clear();
        }

        // We want to present all items which have HEMLEVERANS as chosen delivery method.
        for (PrescriptionItemType entry : prescriptionItemTypes) {

            // Now we want to find the delivery alternative with the delivery method we are looking for (HEMLEVERANS)
            // and also only alternatives which have at least one notification method.
            // When found the intersection is retained.
            entry.getDeliveryAlternative()
                    .stream()
                    .filter(alternative -> alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)
                            && alternative.getDeliveryNotificationMethod() != null
                            && alternative.getDeliveryNotificationMethod().size() > 0)
                    .forEach(alternative -> {

                        // We found the HEMLEVERANS delivery alternative WITH delivery notifications (we assume there's only one).
                        remaining.retainAll(alternative.getDeliveryNotificationMethod());
                    });
        }

        return remaining;
    }

    public boolean validateNotificationInput() {

        final int[] count = {0};

        final boolean[] validationSuccess = {true};

        List<DeliveryNotificationMethodEnum> alreadyAddedErrorMessageFor = new ArrayList<>();

        prescriptionItemTypes.forEach(entry -> {
            String chosenDeliveryMethod = getChosenDeliveryNotificationMethod(entry);

            if (DeliveryNotificationMethodEnum.E_POST.name().equals(chosenDeliveryMethod)) {
                String email = this.email;

                if (!alreadyAddedErrorMessageFor.contains(DeliveryNotificationMethodEnum.E_POST)) {
                    if (email == null || "".equals(email)) {

                        addMessage("Epost för avisering saknas", "emailInput", count[0]);
                        validationSuccess[0] = false;
                    } else if (!Util.isValidEmailAddress(email)) {
                        addMessage("Epost för avisering är ogiltig.", "emailInput", count[0]);
                        validationSuccess[0] = false;
                    }
                    alreadyAddedErrorMessageFor.add(DeliveryNotificationMethodEnum.E_POST);
                }
            } else if (DeliveryNotificationMethodEnum.BREV.name().equals(chosenDeliveryMethod)) {
                // Do nothing
            } else if (DeliveryNotificationMethodEnum.SMS.name().equals(chosenDeliveryMethod)) {
                String smsNumber = this.smsNumber;

                if (!alreadyAddedErrorMessageFor.contains(DeliveryNotificationMethodEnum.SMS)) {
                    if (smsNumber == null || "".equals(smsNumber)) {
                        addMessage("Mobiltelefon för avisering saknas", "smsInput", count[0]);
                        validationSuccess[0] = false;
                    } else if (smsNumber.length() < 10) {
                        addMessage("Mobiltelefon för avisering är ogiltig.", "smsInput", count[0]);
                        validationSuccess[0] = false;
                    }
                    alreadyAddedErrorMessageFor.add(DeliveryNotificationMethodEnum.SMS);
                }
            } else if (DeliveryNotificationMethodEnum.TELEFON.name().equals(chosenDeliveryMethod)) {
                String phoneNumber = this.phoneNumber;

                if (!alreadyAddedErrorMessageFor.contains(DeliveryNotificationMethodEnum.TELEFON)) {
                    if (phoneNumber == null || "".equals(phoneNumber)) {
                        addMessage("Telefon för avisering saknas", "phoneInput", count[0]);
                        validationSuccess[0] = false;
                    } else if (phoneNumber.length() < 8) {
                        addMessage("Telefon för avisering är ogiltig.", "phoneInput", count[0]);
                        validationSuccess[0] = false;
                    }
                    alreadyAddedErrorMessageFor.add(DeliveryNotificationMethodEnum.TELEFON);
                }
            } else {
                throw new IllegalStateException("No match for chosen notification method found.");
            }

            count[0]++;
        });

        return validationSuccess[0];
    }

    private void addMessage(String summary, String componentId, int count) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage(componentIdPrefix + count + ":" + componentId, msg);
    }

    public List<PrescriptionItemType> getPrescriptionItemTypes() {
        return prescriptionItemTypes;
    }

    public List<DeliveryNotificationMethodEnum> getOverlappingDeliveryNotificationMethods() {
        return overlappingDeliveryNotificationMethods;
    }

    public DeliveryNotificationMethodEnum getPreferredDeliveryNotificationMethod() {
        return preferredDeliveryNotificationMethod;
    }

    public Map<PrescriptionItemType, List<String>> getDeliveryNotificationMethodsPerItem() {
        return deliveryNotificationMethodsPerItem;
    }

    public void setDeliveryNotificationMethodsPerItem(Map<PrescriptionItemType, List<String>> deliveryNotificationMethodsPerItem) {
        this.deliveryNotificationMethodsPerItem = deliveryNotificationMethodsPerItem;
    }

    public void setChosenDeliveryNotificationMethod(Map<PrescriptionItemType, String> chosenDeliveryNotificationMethod) {
        this.chosenDeliveryNotificationMethod = chosenDeliveryNotificationMethod;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
