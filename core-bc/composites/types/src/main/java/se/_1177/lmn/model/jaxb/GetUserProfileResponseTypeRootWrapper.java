package se._1177.lmn.model.jaxb;

import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "container")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUserProfileResponseTypeRootWrapper", propOrder = {
        "getUserProfileResponseType"
})public class GetUserProfileResponseTypeRootWrapper {

    @XmlElement(required = true)
    private GetUserProfileResponseType getUserProfileResponseType;

    public GetUserProfileResponseTypeRootWrapper() {
    }

    public GetUserProfileResponseTypeRootWrapper(GetUserProfileResponseType getUserProfileResponseType) {
        this.getUserProfileResponseType = getUserProfileResponseType;
    }

    public GetUserProfileResponseType getObject() {
        return getUserProfileResponseType;
    }

    public void setObject(GetUserProfileResponseType getUserProfileResponseType) {
        this.getUserProfileResponseType = getUserProfileResponseType;
    }
}
