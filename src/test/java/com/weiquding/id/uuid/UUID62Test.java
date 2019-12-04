package com.weiquding.id.uuid;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author beliveyourself
 */
public class UUID62Test {

    @Test
    public void testUUID62() {
        UUID uuid = UUID.randomUUID();
        String uuid62 = UUID62.toUUID62(uuid);
        UUID revertUUID = UUID62.toUUID(uuid62);
        Assert.assertEquals("An error occurred while converting the uuid", uuid, revertUUID);
    }
}
