package com.loopme.common;

import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.Test;

@RunWith(PowerMockRunner.class)
public class LoopMeErrorTest {

    @Test
    public void errorTest() {
        String errMess = "error mess";
        LoopMeError error = new LoopMeError(errMess);
        Assert.assertTrue(error.getMessage().equalsIgnoreCase(errMess));
    }

    @Test
    public void errorEmptyTest() {
        LoopMeError error = new LoopMeError("");
        String defMess = "Unknown error";
        Assert.assertTrue(error.getMessage().equalsIgnoreCase(defMess));
    }

    @Test
    public void errorNullTest() {
        LoopMeError error = new LoopMeError(null);
        String defMess = "Unknown error";
        Assert.assertTrue(error.getMessage().equalsIgnoreCase(defMess));
    }
}
