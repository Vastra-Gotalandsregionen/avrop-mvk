package se._1177.lmn.service;

import mvk.crm.casemanagement.inbox._2.MessageCaseType;
import mvk.crm.casemanagement.inbox.addmessage._2.rivtabp21.AddMessageResponderInterface;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageType;
import org.springframework.stereotype.Service;

/**
 * @author Patrik Björk
 */
@Service
public class MvkInboxService {

    private AddMessageResponderInterface addMessageResponderInterface;

    public MvkInboxService(AddMessageResponderInterface addMessageResponderInterface) {
        this.addMessageResponderInterface = addMessageResponderInterface;
    }

    public AddMessageResponseType getSubjectOfCare(String pid) {

        AddMessageType request = new AddMessageType();

        MessageCaseType messageCase = new MessageCaseType();

        request.setMessage(messageCase);

        AddMessageResponseType response = addMessageResponderInterface.addMessage(request);

        return response;
    }
}
