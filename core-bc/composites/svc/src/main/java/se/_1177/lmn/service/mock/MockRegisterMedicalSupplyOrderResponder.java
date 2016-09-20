package se._1177.lmn.service.mock;

import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.OrderItemType;
import riv.crm.selfservice.medicalsupply._0.ProductAreaEnum;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._0.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.ObjectFactory;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderType;
import se._1177.lmn.service.util.Util;

import javax.jws.WebService;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:riv:crm:selfservice:medicalsupply:RegisterMedicalSupplyOrder:0:rivtabp21", name = "RegisterMedicalSupplyOrderResponderInterface")
public class MockRegisterMedicalSupplyOrderResponder
        implements RegisterMedicalSupplyOrderResponderInterface {

    private final ObjectFactory objectFactory = new ObjectFactory();

    private Random random = new Random();

    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(
            String logicalAddress,
            RegisterMedicalSupplyOrderType parameters) {

        RegisterMedicalSupplyOrderResponseType response = objectFactory.createRegisterMedicalSupplyOrderResponseType();

        /*response.setResultCode(ResultCodeEnum.ERROR);

        response.setComment("Det gick bara inte...");*/

        /*try {
            Thread.sleep(21000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        response.setResultCode(ResultCodeEnum.OK);

        for (int i = 0; i <= random.nextInt(5); i++) {
            OrderItemType orderItem = new OrderItemType();
            orderItem.setDeliveredDate(null);
            orderItem.setOrderDate(Util.toXmlGregorianCalendar(new GregorianCalendar()));

            ArticleType article = new ArticleType();
            article.setArticleName("Artikelnamn" + random.nextInt(100));
            article.setArticleNo(random.nextInt(100000) + "");
            article.setIsOrderable(random.nextBoolean());
            article.setPackageSize(random.nextInt(100));
            article.setPackageSizeUnit("Enhet" + random.nextInt(100));
            article.setProductArea(ProductAreaEnum.values()[random.nextInt(ProductAreaEnum.values().length)]);

            orderItem.setArticle(article);

            response.getOrder().add(orderItem);
        }

        return response;
    }

}
