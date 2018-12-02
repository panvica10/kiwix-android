package org.kiwix.kiwixmobile.tests;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickBack;
import static com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn;
import static com.schibsted.spain.barista.interaction.BaristaSwipeRefreshInteractions.refresh;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.kiwix.kiwixmobile.testutils.TestUtils.TEST_PAUSE_MS;
import static org.kiwix.kiwixmobile.testutils.TestUtils.withContent;
import static org.kiwix.kiwixmobile.utils.StandardActions.enterSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import android.Manifest;
import android.content.Context;
import android.preference.Preference;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import com.schibsted.spain.barista.interaction.BaristaMenuClickInteractions;
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.kiwix.kiwixmobile.KiwixApplication;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.data.ZimContentProvider;
import org.kiwix.kiwixmobile.di.components.DaggerTestComponent;
import org.kiwix.kiwixmobile.di.components.TestComponent;
import org.kiwix.kiwixmobile.di.modules.ApplicationModule;
import org.kiwix.kiwixmobile.main.MainActivity;
import org.kiwix.kiwixmobile.testutils.TestUtils;
import org.kiwix.kiwixmobile.utils.KiwixIdlingResource;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookmarksTest {

    private static final String KIWIX_BOOKMARKS_TEST = "kiwixBookmarksTest";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(
            MainActivity.class, true, false);

    //    @Rule
    //    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule readPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule writePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Inject
    MockWebServer mockWebServer;

    @BeforeClass
    public static void beforeClass() {
        IdlingPolicies.setMasterPolicyTimeout(180, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(180, TimeUnit.SECONDS);
        IdlingRegistry.getInstance().register(KiwixIdlingResource.getInstance());
    }

    @Before
    public void setUp() {

        TestComponent component = DaggerTestComponent.builder().applicationModule
                (new ApplicationModule(
                        (KiwixApplication) getInstrumentation().getTargetContext().getApplicationContext())).build();

        KiwixApplication.setApplicationComponent(component);

        new ZimContentProvider().setupDagger();
        component.inject(this);
        InputStream library = BookmarksTest.class.getClassLoader().getResourceAsStream("library.xml");
        InputStream metalinks = BookmarksTest.class.getClassLoader().getResourceAsStream("test.zim.meta4");
        InputStream testzim = BookmarksTest.class.getClassLoader().getResourceAsStream("testzim.zim");
        try {
            byte[] libraryBytes = IOUtils.toByteArray(library);
            mockWebServer.enqueue(new MockResponse().setBody(new String(libraryBytes)));
            byte[] metalinkBytes = IOUtils.toByteArray(metalinks);
            mockWebServer.enqueue(new MockResponse().setBody(new String(metalinkBytes)));
            mockWebServer.enqueue(new MockResponse().setHeader("Content-Length", 357269));
            Buffer buffer = new Buffer();
            buffer.write(IOUtils.toByteArray(testzim));
            buffer.close();
            mockWebServer.enqueue(new MockResponse().setBody(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    //    public void clickOnBookmark() {
    //        mActivityTestRule.launchActivity(null);
    //
    //        clickOn(R.id.bottom_toolbar_bookmark);
    //    }

    @Test
    public void bookmarksAddTest() {
        mActivityTestRule.launchActivity(null);

        enterSettings();

        onData(allOf(
                is(instanceOf(Preference.class)),
                withKey("pref_bottomtoolbar")))
                .perform(click());

        clickBack();

        BaristaSleepInteractions.sleep(TEST_PAUSE_MS);
        BaristaMenuClickInteractions.clickMenu(getResourceString(R.string.menu_zim_manager));

        TestUtils.allowPermissionsIfNeeded();

        try {
            onView(withId(R.id.network_permission_button)).perform(click());
        } catch (RuntimeException e) {
            Log.i(KIWIX_BOOKMARKS_TEST,
                    "Permission dialog was not shown, we probably already have required permissions");
        }

        onData(withContent("wikipedia_ab_all_2017-03")).inAdapterView(withId(R.id.library_list)).perform(click());

        try {
            onView(withId(android.R.id.button1)).perform(click());
        } catch (RuntimeException e) {
        }

        clickOn(R.string.local_zims);

        try {
            onData(allOf(withId(R.id.zim_swiperefresh)));
            refresh(R.id.zim_swiperefresh);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Commented out the following which assumes only 1 match - not always safe to assume as there may
        // already be a similar file on the device.
        // onData(withContent("wikipedia_ab_all_2017-03")).inAdapterView(withId(R.id.zimfilelist)).perform(click());

        // Find matching zim files on the device
        try {
            DataInteraction dataInteraction = onData(withContent("wikipedia_ab_all_2017-03")).inAdapterView(withId(R.id.zimfilelist));
            // TODO how can we get a count of the items matching the dataInteraction?
            dataInteraction.atPosition(0).perform(click());
        } catch (Exception e) {
            Log.w(KIWIX_BOOKMARKS_TEST, "failed to interact with local ZIM file: " + e.getLocalizedMessage());
        }

        BaristaSleepInteractions.sleep(1000);
        //BaristaClickInteractions.clickOn(R.id.bottom_toolbar_bookmark);
        clickOn(R.id.bottom_toolbar_bookmark);

        BaristaSleepInteractions.sleep(250);
        BaristaMenuClickInteractions.clickMenu(getResourceString(R.string.menu_bookmarks));

        //clickOn(R.string.menu_bookmarks_list);

    }

    @Test
    public void searchBookmarksText() {
        mActivityTestRule.launchActivity(null);
        BaristaSleepInteractions.sleep(TEST_PAUSE_MS);
        BaristaMenuClickInteractions.clickMenu(getResourceString(R.string.menu_bookmarks));
        BaristaSleepInteractions.sleep(250);
        onView(withId(R.id.menu_bookmarks_search)).perform(click());
        BaristaSleepInteractions.sleep(250);
        onView(isAssignableFrom(AutoCompleteTextView.class)).perform(typeText("mongoloid_super"));
        // typeSearchViewText("Mongol");
        BaristaSleepInteractions.sleep(1000);
    }

    @Test
    public void clickBookmarksTest() {
        mActivityTestRule.launchActivity(null);
        enterSettings();
        onData(allOf(
                is(instanceOf(Preference.class)),
                withKey("pref_bottomtoolbar")))
                .perform(click());

        clickBack();
        BaristaSleepInteractions.sleep(TEST_PAUSE_MS);
        BaristaMenuClickInteractions.clickMenu(getResourceString(R.string.menu_bookmarks));
    }


    public static String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

}
