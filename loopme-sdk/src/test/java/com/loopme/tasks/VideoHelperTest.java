package com.loopme.tasks;

import com.loopme.tasks.VideoHelper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.loopme.Logging;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Logging.class})
public class VideoHelperTest {

    @Test
    public void detectFileName() {
        PowerMockito.mockStatic(Logging.class);

        String videoUrl = "http://i.loopme.me/2fff7714fe30079d.mp4";
        VideoHelper helper = new VideoHelper();
        String result = helper.detectFileName(videoUrl);

        assertEquals(result, "2fff7714fe30079d");
    }
}