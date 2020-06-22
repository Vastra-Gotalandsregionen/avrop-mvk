package se._1177.lmn.controller.model;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;

public class PrescriptionItemInfoTest {

    @Test
    public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
        String name = "at1";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();

        ArticleType at1 = new ArticleType();
        at1.setArticleName(name);

        PrescriptionItemType pit1 = new PrescriptionItemType();
        pit1.setArticle(at1);

        prescriptionItemInfo.getChosenPrescriptionItemInfo().put("key1", pit1);
        prescriptionItemInfo.getChosenPrescriptionItemInfo().put("key2", pit1);


        // Serialize
        oos.writeObject(prescriptionItemInfo);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        PrescriptionItemInfo deserialized = (PrescriptionItemInfo) ois.readObject();

        assertEquals(name, deserialized.getPrescriptionItem("key1").getArticle().getArticleName());
    }

}
