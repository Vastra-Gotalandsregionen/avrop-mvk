package se._1177.lmn.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import mvk.crm.casemanagement.inbox._2.CaseTypeType;
import mvk.crm.casemanagement.inbox._2.HealthCareFacilityType;
import mvk.crm.casemanagement.inbox._2.MessageCaseType;
import mvk.crm.casemanagement.inbox.addmessage._2.rivtabp21.AddMessageResponderInterface;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageType;
import org.springframework.stereotype.Service;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import se._1177.lmn.model.DeliveryChoiceTypeWrapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Class for sending messages to a user's inbox in 1177 Vårdguiden e-tjänster.
 *
 * @author Patrik Björk
 */
@Service
public class MvkInboxService {

    private static final String SOURCE_SYSTEM = "ALAR-123LP10";

    private AddMessageResponderInterface addMessageResponderInterface;

    public MvkInboxService(AddMessageResponderInterface addMessageResponderInterface) {
        this.addMessageResponderInterface = addMessageResponderInterface;
    }

    /**
     * Send a message to the user's inbox. The message is composed by a freemarker template.
     *
     * @param pid
     * @param orderRows
     * @return
     * @throws MvkInboxServiceException
     */
    public AddMessageResponseType sendInboxMessage(String pid,
                                                   List<OrderRowType> orderRows,
                                                   String receptionHsaId)
            throws MvkInboxServiceException {

        AddMessageType request = new AddMessageType();

        CaseTypeType caseType = new CaseTypeType();
        caseType.setCaseTypeDescription("Beställning förskrivna förbrukningsprodukter");

        HealthCareFacilityType healthCareFacility = new HealthCareFacilityType();
        healthCareFacility.setHealthCareFacilityName("Förskrivna förbrukningsprodukter");
        healthCareFacility.setHsaId(receptionHsaId);

        MessageCaseType messageCase = new MessageCaseType();
        messageCase.setCaseType(caseType);
        messageCase.setHeaderText("Beställning förskrivna förbrukningsprodukter");
        messageCase.setHealthCareFacility(healthCareFacility);

        try {
            messageCase.setMsg(composeMsg(orderRows));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (TemplateException e) {
            throw new MvkInboxServiceException("Kvitto kunde inte skapas.", e);
        }

        request.setMessage(messageCase);
        request.setSubjectOfCareId(pid);
        request.setNotify(false);
        request.setSourceSystem(SOURCE_SYSTEM);

        AddMessageResponseType response = null;
        try {
            response = addMessageResponderInterface.addMessage(request);
        } catch (Exception e) {
            throw new MvkInboxServiceException("Kvitto kunde inte skapas.", e);
        }

        return response;
    }

    String composeMsg(List<OrderRowType> orderRows) throws IOException, TemplateException {

        // Use reduce function to group order rows by delivery choices.

        // Paramater 1
        SortedMap<DeliveryChoiceTypeWrapper, List<OrderRowType>> identity = new TreeMap<>();

        // Paramater 2
        BiFunction<SortedMap<DeliveryChoiceTypeWrapper, List<OrderRowType>>,
                OrderRowType,
                SortedMap<DeliveryChoiceTypeWrapper, List<OrderRowType>>> biFunction = (map, orderRow) -> {

            merge(map, orderRow);
            return map;
        };

        // Paramater 3
        BinaryOperator<SortedMap<DeliveryChoiceTypeWrapper, List<OrderRowType>>> binaryOperator = (map1, map2) -> {
            map1.entrySet().stream().flatMap(entry -> entry.getValue().stream()).forEach(orderRow -> {
                merge(map2, orderRow);
            });

            return map2;
        };

        // Here we use parameter 1, 2 and 3 to make the reduce operation.
        SortedMap<DeliveryChoiceTypeWrapper, List<OrderRowType>> reduce = orderRows.stream()
                .reduce(identity, biFunction, binaryOperator);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);

        cfg.setClassForTemplateLoading(this.getClass(), "/");

        Template template;
        try {
            // Needs prescriptionItems and deliveryChoices.
            template = cfg.getTemplate("inboxMessageTemplate.ftl", "UTF-8");
        } catch (IOException e) {
            // Should happen always or never.
            throw new RuntimeException("Unable to process freemarker template.");
        }

        Map<String, Object> root = new HashMap<>();
        root.put("deliveryChoiceMappedToOrderRows", reduce);

        StringWriter out = new StringWriter();

        template.process(root, out);

        return out.toString();
    }

    private void merge(Map<DeliveryChoiceTypeWrapper, List<OrderRowType>> map, OrderRowType orderRow) {
        DeliveryChoiceTypeWrapper wrapper = DeliveryChoiceTypeWrapper.of(orderRow.getDeliveryChoice());

        map.merge(wrapper, Arrays.asList(orderRow), (rows1, rows2) -> union(rows1, rows2));
    }

    public static <T> List<T> union(List<T>... collections) {
        return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toList());
    }

    static boolean allAreSame(Collection<DeliveryChoiceType> deliveryChoices) {
        if (deliveryChoices.size() == 0) {
            throw new IllegalArgumentException("There must be more than zero delivery choices");
        }

        DeliveryChoiceType first = deliveryChoices.iterator().next();

        DeliveryMethodEnum firstDeliveryMethod = first.getDeliveryMethod();
        DeliveryPointType firstDeliveryPoint = first.getDeliveryPoint();

        Iterator<DeliveryChoiceType> iterator = deliveryChoices.iterator();

        while (iterator.hasNext()) {
            DeliveryChoiceType next = iterator.next();

            if (!firstDeliveryMethod.equals(next.getDeliveryMethod())) {
                return false;
            } else {
                DeliveryPointType nextDeliveryPoint = next.getDeliveryPoint();
                if (firstDeliveryPoint == null && nextDeliveryPoint != null) {
                    return false;
                } else if (firstDeliveryPoint != null && nextDeliveryPoint == null) {
                    return false;
                } else if (firstDeliveryPoint == null && nextDeliveryPoint == null) {
                    continue;
                } else if (!(firstDeliveryPoint.getDeliveryPointId() + "")
                        .equals((nextDeliveryPoint.getDeliveryPointId() + ""))) {
                    return false;
                }
            }
        }

        return true;
    }
}
