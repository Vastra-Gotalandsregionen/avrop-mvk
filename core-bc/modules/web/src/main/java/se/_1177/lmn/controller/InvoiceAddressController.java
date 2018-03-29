package se._1177.lmn.controller;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.model.AddressModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InvoiceAddressController {

    public static final String VIEW_NAME = "Fakturaadress";

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceAddressController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private NavigationController navigationController;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private Cart cart;

    private SameOrDifferent sameOrDifferent;

    private AddressModel addressModel;

    @PostConstruct
    public void init() {
        addressModel = new AddressModel(userProfileController);
        addressModel.init();
    }

    public void setSameOrDifferent(SameOrDifferent sameOrDifferent) {
        this.sameOrDifferent = sameOrDifferent;
    }

    public SameOrDifferent getSameOrDifferent() {
        return sameOrDifferent;
    }

    public AddressModel getAddressModel() {
        return addressModel;
    }

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    public List<PrescriptionItemType> getOtherInvoiceAddressItems() {
        return prescriptionItemInfo.getChosenPrescriptionItemInfoList().stream()
                .filter(item -> item.isAllowOtherInvoiceAddress() != null && item.isAllowOtherInvoiceAddress())
                .collect(Collectors.toList());
    }

    public String toVerifyDelivery() {

        boolean useOtherAddress = sameOrDifferent.equals(SameOrDifferent.DIFFERENT);
        cart.getOrderRows().stream().filter(orderRow -> {
            PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(orderRow);
            Boolean allowOtherInvoiceAddress = prescriptionItem.isAllowOtherInvoiceAddress();

            return BooleanUtils.isTrue(allowOtherInvoiceAddress);
        }).forEach(orderRow -> {
            orderRow.getDeliveryChoice().setInvoiceAddress(useOtherAddress ? addressModel.toAddressType() : null);
        });

        return navigationController.gotoView("verifyDelivery" + ACTION_SUFFIX, VerifyDeliveryController.VIEW_NAME);
    }

    public static enum SameOrDifferent {
        SAME, DIFFERENT
    }
}
