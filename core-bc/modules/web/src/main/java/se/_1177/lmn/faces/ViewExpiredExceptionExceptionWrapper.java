package se._1177.lmn.faces;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;

public class ViewExpiredExceptionExceptionWrapper extends ExceptionHandlerWrapper {

    private ExceptionHandler wrapped;

    public ViewExpiredExceptionExceptionWrapper(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext(); ) {

            ExceptionQueuedEvent event = i.next();

            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext)
                    event.getSource();

            Throwable t = context.getException();

            if (t instanceof ViewExpiredException) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
                try {
                    navigationHandler.handleNavigation(facesContext, null, "/order.xhtml?faces-redirect=true&amp;includeViewParams=true");
                    facesContext.renderResponse();
                } finally {
                    i.remove();
                }
            }
        }

        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();
    }
}