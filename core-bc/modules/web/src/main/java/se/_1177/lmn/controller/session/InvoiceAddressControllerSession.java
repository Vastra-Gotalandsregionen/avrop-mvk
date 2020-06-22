package se._1177.lmn.controller.session;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.controller.InvoiceAddressController;
import se._1177.lmn.controller.model.AddressModel;

import java.io.Serializable;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class InvoiceAddressControllerSession extends AbstractSessionData implements Serializable {

    private InvoiceAddressController.SameOrDifferent sameOrDifferent;

    private AddressModel addressModel;

}
