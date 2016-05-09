package se._1177.lmn.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import mvk.crm.casemanagement.inbox._2.CaseTypeType;
import mvk.crm.casemanagement.inbox._2.MessageCaseType;
import mvk.crm.casemanagement.inbox.addmessage._2.rivtabp21.AddMessageResponderInterface;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageType;
import org.springframework.stereotype.Service;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
@Service
public class MvkInboxService {

    private AddMessageResponderInterface addMessageResponderInterface;

    public MvkInboxService(AddMessageResponderInterface addMessageResponderInterface) {
        this.addMessageResponderInterface = addMessageResponderInterface;
    }

    public AddMessageResponseType sendInboxMessage(String pid,
                                                   List<PrescriptionItemType> prescriptionItems,
                                                   List<DeliveryChoiceType> deliveryChoices)
            throws MvkInboxServiceException {

        AddMessageType request = new AddMessageType();

        CaseTypeType caseType = new CaseTypeType();
        caseType.setCaseTypeDescription("Läkemedelsnära produkter");

        MessageCaseType messageCase = new MessageCaseType();
        messageCase.setBusinessObjectId("Unikt id för denna applikation?");
//        messageCase.setActionTime(Util.toXmlGregorianCalendar(new GregorianCalendar()));
        messageCase.setCaseType(caseType);
//        messageCase.setCaseReadStatus(CaseReadStatusType.UNREAD);
//        messageCase.setBooking(null);
        messageCase.setHeaderText("Centrum Läkemedelsnära Produkter");

        try {
            messageCase.setMsg(composeMsg(prescriptionItems, deliveryChoices));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (TemplateException e) {
            throw new MvkInboxServiceException("Kvitto kunde inte skapas.", e);
        }

        request.setMessage(messageCase);
        request.setSubjectOfCareId(pid);
        request.setNotify(false);
        request.setSourceSystem("HSA-ID för Läkemedelsnära produktor");

        AddMessageResponseType response = null;
        try {
            response = addMessageResponderInterface.addMessage(request);
        } catch (Exception e) {
            throw new MvkInboxServiceException("Kvitto kunde inte skapas.", e);
        }

        return response;
    }

    String composeMsg(List<PrescriptionItemType> prescriptionItems,
                      List<DeliveryChoiceType> deliveryChoices) throws IOException, TemplateException {

        Configuration cfg = new Configuration();

        cfg.setClassForTemplateLoading(this.getClass(), "/");

        Template template;
        try {
            // Needs prescriptionItems and deliveryChoices.
            template = cfg.getTemplate("inboxMessageTemplate.ftl");
        } catch (IOException e) {
            // Should happen always or never.
            throw new RuntimeException("Unable to process freemarker template.");
        }

        Map<String, Object> root = new HashMap<>();
        root.put("prescriptionItems", prescriptionItems);
        root.put("deliveryChoices", deliveryChoices);

        StringWriter out = new StringWriter();

        template.process(root, out);

        return out.toString();
    }
}
