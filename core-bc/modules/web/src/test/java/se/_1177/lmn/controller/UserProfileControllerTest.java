package se._1177.lmn.controller;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class UserProfileControllerTest {

    @Test
    public void serializeDeserialize() throws IOException, ClassNotFoundException {
        UserProfileController userProfileController = new UserProfileController();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(userProfileController);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        UserProfileController deserialized = (UserProfileController) ois.readObject();

        assertNotNull(deserialized);
    }

}
