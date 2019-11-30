package se._1177.lmn.controller.session;

import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.ObjectFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.service.util.JaxbUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import static se._1177.lmn.service.util.JaxbUtil.objectToXML;
import static se._1177.lmn.service.util.JaxbUtil.readObject;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserProfileSession implements Serializable {

    private GetUserProfileResponseType userProfileResponse;
    private GetUserProfileResponseType userProfileResponseLoggedInUser;
    private GetUserProfileByAgentResponseType userProfileByAgentResponse;

    private boolean delegate;
    private String objectId;
    private UserProfileType userProfile;

   /* @Override
    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(utilController);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        XMLEncoder xmlEncoder = new XMLEncoder(baos);

        out.writeObject(objectToXML(new ObjectFactory().createGetUserProfileResponse(userProfileResponse)));
        out.writeObject(objectToXML(new ObjectFactory().createGetUserProfileResponse(userProfileResponseLoggedInUser)));
        out.writeObject(objectToXML(new mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.ObjectFactory().createGetUserProfileByAgentResponse(userProfileByAgentResponse)));
//        out.writeObject(objectToXML(new mvk.itintegration.userprofile.getuserprofileresponder._2.ObjectFactory().createGetUserProfile(userProfile)));
//        out.writeObject(objectToXML(new GetUserProfileResponseTypeRootWrapper(userProfileResponseLoggedInUser)));
//        out.writeObject(objectToXML(new GetUserProfileResponseTypeRootWrapper(userProfileByAgentResponse)));
//        out.writeObject(objectToXML(new GetUserProfileResponseTypeRootWrapper(userProfile)));
        out.writeBoolean(delegate);
        out.writeObject(objectId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        userProfileResponse = readObject(in, GetUserProfileResponseType.class);
        userProfileResponseLoggedInUser = readObject(in, GetUserProfileResponseType.class);
        userProfileByAgentResponse = readObject(in, GetUserProfileByAgentResponseType.class);
//        userProfile = (UserProfileType) readObject(in);
        delegate = in.readBoolean();
        objectId = (String) in.readObject();
//        updateUserProfile();
    }*/



    public GetUserProfileResponseType getUserProfileResponse() {
        return userProfileResponse;
    }

    public void setUserProfileResponse(GetUserProfileResponseType userProfileResponse) {
        this.userProfileResponse = userProfileResponse;
    }

    public GetUserProfileResponseType getUserProfileResponseLoggedInUser() {
        return userProfileResponseLoggedInUser;
    }

    public void setUserProfileResponseLoggedInUser(GetUserProfileResponseType userProfileResponseLoggedInUser) {
        this.userProfileResponseLoggedInUser = userProfileResponseLoggedInUser;
    }

    public GetUserProfileByAgentResponseType getUserProfileByAgentResponse() {
        return userProfileByAgentResponse;
    }

    public void setUserProfileByAgentResponse(GetUserProfileByAgentResponseType userProfileByAgentResponse) {
        this.userProfileByAgentResponse = userProfileByAgentResponse;
    }

    public boolean isDelegate() {
        return delegate;
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public UserProfileType getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfileType userProfile) {
        this.userProfile = userProfile;
    }
}
