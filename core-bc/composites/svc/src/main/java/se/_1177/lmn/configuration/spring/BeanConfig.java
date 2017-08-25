package se._1177.lmn.configuration.spring;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._1.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._1.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._1.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import se._1177.lmn.configuration.counties.CountiesConfiguration;
import se._1177.lmn.configuration.counties.County;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.service.LmnServiceImpl;
import se._1177.lmn.service.LmnServiceRoutingImpl;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:lakemedelsnara.properties")
public class BeanConfig {

    @Autowired
    private Environment env;

    private String sslTruststore;
    private String sslTruststorePassword;

    private String sslKeystore;
    private String sslKeystorePassword;

    @PostConstruct
    public void init() {
        sslTruststore = env.getProperty("ssl_truststore");
        sslTruststorePassword = env.getProperty("ssl_truststore_password");
        sslKeystore = env.getProperty("ssl_keystore");
        sslKeystorePassword = env.getProperty("ssl_keystore_password");
    }

    @Bean
    public LmnService getLmnService() {
        Map<String, LmnService> countyCodeToLmnService = new HashMap<>();

        CountiesConfiguration countiesConfiguration = parseCountiesConfiguration();

        for (Map.Entry<String, County> countyCodeToCountEntry : countiesConfiguration.getCounties().entrySet()) {
            County county = countyCodeToCountEntry.getValue();

            String getMedicalSupplyDeliveryPointsAddress = county.getGetMedicalSupplyDeliveryPointsAddress();
            String getMedicalSupplyPrescriptionsAddress = county.getGetMedicalSupplyPrescriptionsAddress();
            String registerMedicalSupplyOrderAddress = county.getRegisterMedicalSupplyOrderAddress();
            String rtjpLogicalAddress = county.getRtjpLogicalAddress();
            String customerServicePhoneNumber = county.getCustomerServicePhoneNumber();
            String customerServiceInfo = county.getCustomerServiceInfo();

            LmnService vgrLmnService = new LmnServiceImpl(
                    getMedicalSupplyDeliveryPointsResponderInterface(getMedicalSupplyDeliveryPointsAddress),
                    getMedicalSupplyPrescriptionsResponderInterface(getMedicalSupplyPrescriptionsAddress),
                    getRegisterMedicalSupplyOrderResponderInterface(registerMedicalSupplyOrderAddress),
                    rtjpLogicalAddress,
                    customerServicePhoneNumber,
                    customerServiceInfo
            );

            countyCodeToLmnService.put(countyCodeToCountEntry.getKey(), vgrLmnService);
        }

        return new LmnServiceRoutingImpl(countyCodeToLmnService);
    }

    CountiesConfiguration parseCountiesConfiguration() {

        ClassLoader classLoader = this.getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("counties-configuration.yml")) {

            Yaml yaml = new Yaml();

            return yaml.loadAs(inputStream, CountiesConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JaxWsProxyFactoryBean getJaxWsProxyFactoryBean(Class<?> serviceClass, String endpointUrl) {

        JaxWsProxyFactoryBean bean = new JaxWsProxyFactoryBean();
        bean.setServiceClass(serviceClass);
        bean.setAddress(endpointUrl);

        addLoggingInterceptors(bean);

        return bean;
    }

    private void addLoggingInterceptors(JaxWsProxyFactoryBean bean) {
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor(-1);
        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor(-1);

        loggingOutInterceptor.setPrettyLogging(true);
        loggingInInterceptor.setPrettyLogging(true);

        bean.getOutInterceptors().add(loggingOutInterceptor);
        bean.getInInterceptors().add(loggingInInterceptor);
    }

    private GetMedicalSupplyDeliveryPointsResponderInterface getMedicalSupplyDeliveryPointsResponderInterface(String endpointUrl) {
        Class<GetMedicalSupplyDeliveryPointsResponderInterface> serviceClass = GetMedicalSupplyDeliveryPointsResponderInterface.class;
        Object client = createClient(endpointUrl, serviceClass);
        return (GetMedicalSupplyDeliveryPointsResponderInterface) client;
    }

    private GetMedicalSupplyPrescriptionsResponderInterface getMedicalSupplyPrescriptionsResponderInterface(String endpointUrl) {
        Class<GetMedicalSupplyPrescriptionsResponderInterface> serviceClass = GetMedicalSupplyPrescriptionsResponderInterface.class;
        Object client = createClient(endpointUrl, serviceClass);
        return (GetMedicalSupplyPrescriptionsResponderInterface) client;
    }

    private RegisterMedicalSupplyOrderResponderInterface getRegisterMedicalSupplyOrderResponderInterface(String endpointUrl) {
        Class<RegisterMedicalSupplyOrderResponderInterface> serviceClass = RegisterMedicalSupplyOrderResponderInterface.class;
        Object client = createClient(endpointUrl, serviceClass);
        return (RegisterMedicalSupplyOrderResponderInterface) client;
    }

    private Object createClient(String endpointUrl, Class<?> serviceClass) {
        Object client = getJaxWsProxyFactoryBean(serviceClass, endpointUrl).create(serviceClass);

        Client clientProxy = ClientProxy.getClient(client);

        HTTPConduit httpConduit = (HTTPConduit) clientProxy.getConduit();
        httpConduit.setTlsClientParameters(setupTlsClientParameters());

        return client;
    }

    private TLSClientParameters setupTlsClientParameters() {

        try (InputStream sslTruststoreInput = new FileInputStream(sslTruststore);
             InputStream sslKeystoreInput = new FileInputStream(sslKeystore)) {

            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(sslTruststoreInput, sslTruststorePassword.toCharArray());

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(sslKeystoreInput, sslKeystorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustKeyStore);

            KeyManagerFactory trustKeyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            trustKeyManagerFactory.init(trustKeyStore, sslTruststorePassword.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, sslKeystorePassword.toCharArray());

            TLSClientParameters tlsClientParameters = new TLSClientParameters();
            tlsClientParameters.setTrustManagers(trustManagerFactory.getTrustManagers());
            tlsClientParameters.setDisableCNCheck(true);
            tlsClientParameters.setKeyManagers(keyManagerFactory.getKeyManagers());

            return tlsClientParameters;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}