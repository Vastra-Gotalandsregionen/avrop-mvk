package se._1177.lmn.service.mock;

import mvk.crm.casemanagement.inbox.addmessage._2.rivtabp21.AddMessageResponderInterface;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageType;

import javax.jws.WebService;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:mvk:crm:casemanagement:inbox:AddMessage:2:rivtabp21", name = "AddMessageResponderInterface")
public class MockAddMessageResponder
        implements AddMessageResponderInterface {

    @Override
    public AddMessageResponseType addMessage(AddMessageType request) {
        return null;
    }
}
