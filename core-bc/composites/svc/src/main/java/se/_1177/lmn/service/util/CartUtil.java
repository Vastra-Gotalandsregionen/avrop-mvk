package se._1177.lmn.service.util;

import riv.crm.selfservice.medicalsupply._2.OrderRowType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;

import java.util.Optional;

/**
 * @author Patrik Björk
 */
public class CartUtil {

    public static Optional<OrderRowType> createOrderRow(PrescriptionItemType prescriptionItem) {

        if (prescriptionItem.getSubArticle() != null && prescriptionItem.getSubArticle().size() > 0) {
            // We can't create an order row yet since this prescription item may result in multiple order rows.
            return Optional.empty();
        }

        OrderRowType orderRow = new OrderRowType();
        orderRow.setArticle(prescriptionItem.getArticle());
        orderRow.setPrescriptionId(prescriptionItem.getPrescriptionId());
        orderRow.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
        orderRow.setSource(prescriptionItem.getSource());

        // If we don't have any sub-articles we know all packages per order for this item
        orderRow.setNoOfPackages(prescriptionItem.getNoOfPackagesPerOrder());
        orderRow.setNoOfPcs(prescriptionItem.getNoOfArticlesPerOrder());

        return Optional.of(orderRow);
    }

}
