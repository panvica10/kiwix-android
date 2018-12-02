package org.kiwix.kiwixmobile.data.local;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kiwix.kiwixmobile.utils.LanguageUtils;
import org.kiwix.kiwixmobile.utils.NetworkUtils;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UtilsTest {

    private final Context context;

    public UtilsTest() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testGetTypefaceString(){
        assertTrue(LanguageUtils.getTypeface("km").equals("fonts/KhmerOS.ttf"));
        assertTrue(LanguageUtils.getTypeface("guj").equals("fonts/Lohit-Gujarati.ttf"));
    }

    @Test
    public void testGetFileNameFromUrl(){
        String testUrl1 = "www.testurl.sk/testapi/endpoint2.xml";
        String testUrl2 = "www.testurl.sk/testapi/endpoint2.xml/?";
        assertTrue(NetworkUtils.getFileNameFromUrl(testUrl1).equals("endpoint2.xml"));
        assertFalse(NetworkUtils.getFileNameFromUrl(testUrl2).equals("endpoint2.xml"));
    }

    @Test
    public void testIso3ToLocale(){
        String textToLocale = "eng";
        assertTrue(LanguageUtils.ISO3ToLocale(textToLocale) == Locale.CANADA);

    }
}
