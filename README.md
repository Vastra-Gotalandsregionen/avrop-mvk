# Avrop läkemedelsnära produkter för patient

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

* The user clicks on "Läkemedelsnära produkter".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow2.png)

* The user clicks on "Till beställning".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow3.png)

* The user chooses the products he/she wants to order, from available products, and clicks "Till leverans".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow4.png)

* The user chooses delivery method and clicks "Till adressinformation".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow5.png)

* If the user chose home delivery he/she fills in the form and clicks "Kontrollera beställning".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow6a.png)

* If the user chose collect delivery he/she chooses delivery point and notification method and clicks "Kontrollera beställning".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow6b.png)

* The user verifies the order and clicks "Beställ / Bekräfta beställning".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow7.png)

* The user sees a confirmation and if he/she wishes can go to the inbox by clicking "Till inkorgen".

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow8.png)

* The user clicks on the unread mail.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow9.png)

* The confirmation mail is shown.

![](https://raw.githubusercontent.com/Vastra-Gotalandsregionen/avrop-lakemedelsnara-produkter/release/1.0/core-bc/modules/web/doc/flow10.png)