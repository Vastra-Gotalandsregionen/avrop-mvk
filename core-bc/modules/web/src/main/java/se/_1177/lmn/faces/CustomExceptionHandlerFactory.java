package se._1177.lmn.faces;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {
 
  private ExceptionHandlerFactory parent;
 
  public CustomExceptionHandlerFactory(ExceptionHandlerFactory parent) {
    this.parent = parent;
  }
 
  @Override
  public ExceptionHandler getExceptionHandler() {
    return new ViewExpiredExceptionExceptionWrapper(parent.getExceptionHandler());
  }
}