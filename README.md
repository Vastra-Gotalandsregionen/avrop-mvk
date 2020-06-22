# Avrop förskrivna förbrukningsprodukter för patient

This is a web application developed by the Region of Västra Götaland, Sweden. The target users are patients who have any prescription of products available in the application. The application is a part of [1177 Vårdguiden E-tjänster](https://minavardkontakter.se).

The application flow is described further down in this document.

## Architecture overview
The below image illustrates the architecture overview.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/Avrop_MVK_architecture_overview.png)

What's in the scope of this project is the application marked in red. The arrows are also marked in red to imply that the web service communication is also included in the scope of this project.

The application is run in a Tomcat behind an Apache Web Server protected by a Shibboleth service provider module. The Apache Web Server communicates with Tomcat by the AJP protocol. The Shibboleth module sends headers to the application providing information about the authenticated user.

Even though the user's experience is that the application is a web page within "1177 Vårdguiden e-tjänster" it is located on a separate domain on separate infrastructure but utilize single-sign-on by a shared identity provider.

The Inbox service and User Profile service are explained in depth at [Stödfunktioner för virtuell portal](https://invanartjanster.atlassian.net/wiki/pages/viewpage.action?pageId=58163458). In short, the Inbox service exposes methods to send messages to the user which can be read in the inbox found in "1177 Vårdguiden e-tjänster". The User Profile service exposes methods to retrieve information about the user, e.g. address, phone number. Also inhabitants for whom the user is delegate can be retrived.

The Medical Supply service provides the following features:

* Retrieval of subscriptions for the user.
* Retrieval of delivery points.
* Placing orders.

## General technical description
The application is written in the Java language for backend and Facelets for view layer and runs in a servlet container such as e.g. Apache Tomcat. 

Prerequsites in order to build the application:

* Java 8
* Maven 3.x

The primary frameworks/libraries used are:

* Java Server Faces 2.2
* Spring Framework 4.2.5
* Apache CXF 3.1.6 (For JAX-WS support)

## Getting started

Build and dependencies are managed by Maven. To build the project, perform a ``git clone`` and run ``mvn package``.

In order to run the application, deploy it to a servlet container, e.g. Tomcat 8. Make sure the file application.properties is found on the classpath. It is possible to have endpoints configured for localhost if mocked services are started.

### Running locally without SAML service provider reverse proxy
Normally the application checks a header to check which user is authenticated. In a local environment it may be necessary to use the application without a service provider. There are two options to achieve this:

* Apply the relevant headers by e.g. a plugin in the browser. The primary header is "AJP_Subject_SerialNumber" which corresponds to the personal identity number.
* Start the JVM with VM property -Denv=dev. This relies on the mock services not caring about the identity of the user but instead create responses independent of the identity.

### Preparing test certificates
To run in a test environment with authentication protection the easiest way is to issue test BankId at https://demo.bankid.com. Issue BandIds with personal identity numbers as the people listed in the Region of Västra Götaland in [https://softronic.atlassian.net/wiki/pages/viewpage.action?pageId=66191421](https://softronic.atlassian.net/wiki/pages/viewpage.action?pageId=66191421). That is necessary in order to properly use the User Profile service and the Inbox service.

### Web services
All integrations are run over SOAP over HTTPS with mutual authentication. The contracts are included in the schema maven module and source code for proxy service interfaces is generated during maven build. Apache CXF is leveraged for the web service communication. During build classes are generated which are used as web service proxy interfaces, as well as related domain classes.

During development a local web server can be started which mocks all web services. Some endpoints, or all, may be configured to point at localhost on port 18080. They can then be accessed at http://localhost:18080/{name} (see MockWebServiceServer.java).

## Application flow
A typical flow is as follows:

* The user logs in to [https://minavardkontakter.se](https://minavardkontakter.se) with a valid certificate (the application is not available if user logs in with password and SMS).

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow0.png)

* The user clicks on "Alla övriga tjänster".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow1.png)

* The user clicks on "Beställ förskrivna förbrukningsprodukter".

![](https://github.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/raw/java11-compatibility/core-bc/modules/web/doc/flow2.PNG)

* The user chooses the products he/she wants to order, from available products.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow4.png)

* If any of the chosen products has subarticles, e.g. with different tastes, the user chooses distribution of those.

![](https://github.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/raw/java11-compatibility/core-bc/modules/web/doc/flow4b.PNG)

* The user chooses delivery method.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow5.png)

* If the user chose home delivery he/she fills in the form.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow6a.png)

* If the user chose collect delivery he/she chooses delivery point and notification method.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow6b.png)

* If any of the chosen products allows invoice to another address the user chooses whether the invoice should be sent to the same as the shipping address or a custom adress.

![](https://github.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/raw/java11-compatibility/core-bc/modules/web/doc/flow6c.PNG)

* The user verifies the order and clicks "Beställ / Bekräfta beställning".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow7.png)

* The user sees a confirmation and if he/she wishes can go to the inbox by clicking "Till inkorgen".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow8.png)

* The user clicks on the unread mail.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow9.png)

* The confirmation mail is shown.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow10.png)

## Configuration per county
The application is used across multiple independent counties in Sweden where each county uses its own source system and has separate contracts with suppliers. Each county can therefore be configured according to their needs. Some needs are configured in the application and some are decided by the SOAP messages. Here the settings which are configured, per county in counties-configuration-20.yml, in the application are listed:

* ``getMedicalSupplyDeliveryPointsAddress`` - the endpoint address for fetching delivery points (optional - not all counties have delivery points)
* ``getMedicalSupplyPrescriptionsAddress`` - the endpoint address for fetching prescriptions for a patient
* ``registerMedicalSupplyOrderAddress`` - the endpoint address for registering the order
* ``rtjpLogicalAddress`` - HSA ID for the county
* ``receptionHsaId`` - HSA ID for the reception
* ``defaultSelectedPrescriptions`` - whether all orderable articles should be preselected or the patient needs to pick all he/she wishes to order 

In addition, a number of text strings, are configured per county in each application_sv_SE_NN.properties file where NN corresponds to the county code. These are examples: 

* ``customer.service.phone``=XXX-XXX XX XX
* ``customer.service.info``=Kundtjänst Skövdedepån: Telefon XXX-XXX XX XX. Öppet vardagar kl 08:00 till 16:30. E-post <a href="mailto:kundtjanst@example.com">kundtjanst@example.com</a>
* ``products.fetch.default.error``=Dina förskrivna produkter kunde inte visas.
* ``products.fetch.none.found``=Inga förskrivna produkter hittades. Om du förväntat dig beställningsbara produkter, kontakta kundtjänst på XXX-XXX XX XX.
* ``order.confirmation``=Tack för din beställning, produkterna levereras inom 5 arbetsdagar

Moreover, each county can send instructions via the SOAP messages. See https://github.com/Vastra-Gotalandsregionen/oppna-program-icc-crm-selfservice-medicalsupply-schemas for a complete reference. Examples of elements which can be seen as "configuration-ish" are:

* ``AllowOtherInvoiceAddress`` - If the patient chooses any article with this set to true he/she will need to choose another address for the invoice or same as delivery address.
* ``AllowChioceOfDeliveryPoints`` - Whether the patient can choose delivery point or the supplier will choose the closest.
* ``AllowContactPerson`` - Whether an input field for contact person should be displayed for collect delivery. May be usable especially when people aged 13-18 years are concerned, so the package may be collected.
