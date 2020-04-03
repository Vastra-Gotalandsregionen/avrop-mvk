package se._1177.lmn.controller.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class AbstractSessionData implements Serializable {
    private boolean inited;
}
