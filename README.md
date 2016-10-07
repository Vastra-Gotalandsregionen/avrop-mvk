# Avrop läkemedelsnära produkter för patient

This is a web application developed by the Region of Västra Götaland, Sweden. The target users are patients who have any prescription of products available in the application. The application is a part of [1177 Vårdguiden E-tjänster](https://minavardkontakter.se).

## Architecture overview
The below image illustrates the architecture overview.

What's in the scope of this project is the application marked in red. The arrows are also marked in red to imply that the web service communication is also included in the scope of this project.

Even though the user's experience is that the application is a web page within "1177 Vårdguiden e-tjänster" it is located on a separate domain on separate infrastructure but utilize single-sign-on by a shared identity provider.

The Inbox service and User Profile service are explained in depth at [Stödfunktioner för virtuell portal](https://invanartjanster.atlassian.net/wiki/pages/viewpage.action?pageId=58163458). In short, the Inbox service exposes methods to send messages to the user which can be read in the inbox found in "1177 Vårdguiden e-tjänster". The User Profile service exposes methods to retrieve information about the user, e.g. address, phone number. Also inhabitants for whom the user is delegate can be retrived.

The Medical Supply service provides the following features:

* Retrieval of subscriptions for the user.
* Retrieval of delivery points.
* Placing orders.

## General technical description
The application is written in the Java language and runs in a servlet container such as e.g. Apache Tomcat. 

Prerequsites in order to build the application:

* Java 8
* Maven 3.x

The primary frameworks/libraries used are:

* Java Server Faces 2.2
* Spring Framework 4.2.5
* Apache CXF 3.1.6 (For JAX-WS support)

Build and dependencies are managed by Maven. To build the project, perform a ``git clone`` and run ``mvn package``.

### Web services
All integrations are run over SOAP over HTTPS with mutual authentication. The contracts are included in the schema maven module and source code for proxy service interfaces is generated during maven build.