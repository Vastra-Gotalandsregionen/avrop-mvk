package se._1177.lmn.service.mock;

import riv.crm.selfservice.medicalsupply._2.CVType;
import se._1177.lmn.model.ServicePointProvider;

public class MockUtil {

    public static CVType toCvType(MockProductAreaEnum value) {
        CVType cvType = new CVType();
        cvType.setOriginalText(value.name());
        return cvType;
    }

    public static CVType toCvType(MockServicePointProviderEnum value) {
        CVType cvType = new CVType();
        cvType.setOriginalText(value.name());
        return cvType;
    }

    public static CVType toCvType(ServicePointProvider provider) {
        CVType cvType = new CVType();
        cvType.setOriginalText(provider.getName());
        return cvType;
    }
}
