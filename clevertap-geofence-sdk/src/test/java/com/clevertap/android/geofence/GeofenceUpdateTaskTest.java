package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.fakes.GeofenceJSON;
import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28,
        application = TestApplication.class
)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "androidx.*", "org.json.*"})
@PrepareForTest({CTGeofenceAPI.class, FileUtils.class})
public class GeofenceUpdateTaskTest extends BaseTestCase {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Logger logger;
    @Mock
    public CTGeofenceAPI ctGeofenceAPI;
    @Mock
    public CTGeofenceAdapter ctGeofenceAdapter;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(CTGeofenceAPI.class, FileUtils.class);

        super.setUp();

        when(CTGeofenceAPI.getInstance(application)).thenReturn(ctGeofenceAPI);
        logger = new Logger(Logger.DEBUG);
        when(CTGeofenceAPI.getLogger()).thenReturn(logger);

        WhiteboxImpl.setInternalState(ctGeofenceAPI, "ctGeofenceAdapter", ctGeofenceAdapter);

    }

    @Test
    public void executeTestTC1() throws Exception {

        // when old geofence is empty and geofence monitor count is less than new geofence list size

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn("");

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(1)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application, GeofenceJSON.getGeofence());

        updateTask.execute();

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OnSuccessListener> argumentCaptorOnSuccessListener = ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(ctGeofenceAdapter).addAllGeofence(argumentCaptor.capture(), argumentCaptorOnSuccessListener.capture());
        assertEquals(argumentCaptor.getValue().size(), 1);

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<JSONObject> argumentCaptorJson = ArgumentCaptor.forClass(JSONObject.class);

        verifyStatic(FileUtils.class);
        FileUtils.writeJsonToFile(any(Context.class), anyString(), anyString(), argumentCaptorJson.capture());

        JSONAssert.assertEquals(GeofenceJSON.getFirst(), argumentCaptorJson.getValue(), true);
    }

    @Test
    public void executeTestTC2() throws Exception {

        // when old geofence is empty and geofence monitor count is greater than new geofence list size

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn("");

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(1)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application, GeofenceJSON.getEmptyGeofence());

        updateTask.execute();

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OnSuccessListener> argumentCaptorOnSuccessListener = ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(ctGeofenceAdapter).addAllGeofence(argumentCaptor.capture(),argumentCaptorOnSuccessListener.capture());
        assertEquals(argumentCaptor.getValue().size(), 0);

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<JSONObject> argumentCaptorJson = ArgumentCaptor.forClass(JSONObject.class);

        verifyStatic(FileUtils.class);
        FileUtils.writeJsonToFile(any(Context.class), anyString(), anyString(), argumentCaptorJson.capture());

        JSONAssert.assertEquals(GeofenceJSON.getEmptyGeofence(), argumentCaptorJson.getValue(), true);
    }

    @Test
    public void executeTestTC3() throws Exception {

        // when old geofence is empty and new geofence json is invalid

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn("");

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(1)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application, GeofenceJSON.getEmptyJson());

        updateTask.execute();

        ArgumentCaptor<JSONObject> argumentCaptorJson = ArgumentCaptor.forClass(JSONObject.class);

        verifyStatic(FileUtils.class,never());
        FileUtils.writeJsonToFile(any(Context.class), anyString(), anyString(), argumentCaptorJson.capture());

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(ctGeofenceAdapter,never()).addAllGeofence(argumentCaptor.capture(), any(OnSuccessListener.class));
    }

    @Test
    public void executeTestTC4() throws Exception {

        // when old geofence is not empty and new geofence list is not empty
        // A = new geofence, B = old geofence
        // test Set operations A - B and B - A when A == B

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn(GeofenceJSON.getFirst().toString());

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(1)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application, GeofenceJSON.getGeofence());

        updateTask.execute();

        ArgumentCaptor<List<String>> argumentCaptorOldGeofence = ArgumentCaptor.forClass(List.class);

        // since both sets have identical geofences removeAllGeofence() must pass empty geofence list
        verify(ctGeofenceAdapter).removeAllGeofence(argumentCaptorOldGeofence.capture(), any(OnSuccessListener.class));
        assertEquals(argumentCaptorOldGeofence.getValue().size(),0);

    }

    @Test
    public void executeTestTC5() throws Exception {

        // when old geofence is not empty and new geofence list is not empty
        // A = new geofence, B = old geofence
        // test Set operations A - B and B - A when A = 1, B = 2 and B contains geofence in A

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn( GeofenceJSON.getGeofenceString());

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(1)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application,GeofenceJSON.getFirst());

        updateTask.execute();

        ArgumentCaptor<List<String>> argumentCaptorOldGeofence = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OnSuccessListener> argumentCaptorOnSuccessListener = ArgumentCaptor.forClass(OnSuccessListener.class);

        // B is having extra geofence which must be removed
        verify(ctGeofenceAdapter).removeAllGeofence(argumentCaptorOldGeofence.capture(), argumentCaptorOnSuccessListener.capture());
        assertThat(argumentCaptorOldGeofence.getValue(),is(Arrays.asList("310002")));

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // A is having 1 geofence which is already there in B so no need to add any geofence
        verify(ctGeofenceAdapter).addAllGeofence(argumentCaptor.capture(), argumentCaptorOnSuccessListener.capture());
        assertEquals(argumentCaptor.getValue().size(), 0);

    }

    @Test
    public void executeTestTC6() throws Exception {

        // when old geofence is not empty and new geofence list is not empty
        // A = new geofence, B = old geofence
        // test Set operations A - B and B - A when A = 2, B = 1 and A contains geofence in B

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn( GeofenceJSON.getLast().toString());

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(2)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application,GeofenceJSON.getGeofence());

        updateTask.execute();

        ArgumentCaptor<List<String>> argumentCaptorOldGeofence = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OnSuccessListener> argumentCaptorOnSuccessListener = ArgumentCaptor.forClass(OnSuccessListener.class);

        // B is having 1 geofence which is also there in A so no need to remove any geofence
        verify(ctGeofenceAdapter).removeAllGeofence(argumentCaptorOldGeofence.capture(), argumentCaptorOnSuccessListener.capture());
        assertEquals(0,argumentCaptorOldGeofence.getValue().size());

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        // A is having 1 new geofence which is not there in B so 1 must be added
        verify(ctGeofenceAdapter).addAllGeofence(argumentCaptor.capture(), argumentCaptorOnSuccessListener.capture());
        assertEquals(argumentCaptor.getValue().size(), 1);

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<JSONObject> argumentCaptorJson = ArgumentCaptor.forClass(JSONObject.class);

        // all geofences in A must be written to file
        verifyStatic(FileUtils.class);
        FileUtils.writeJsonToFile(any(Context.class), anyString(), anyString(), argumentCaptorJson.capture());

        JSONAssert.assertEquals(GeofenceJSON.getGeofence(), argumentCaptorJson.getValue(), true);

    }

    @Test
    public void executeTestTC9() throws Exception {

        // when old geofence is not empty and new geofence list is null

        when(FileUtils.getCachedDirName(application)).thenReturn("");
        when(FileUtils.getCachedFullPath(any(Context.class), anyString())).thenReturn("");
        when(FileUtils.readFromFile(any(Context.class),
                anyString())).thenReturn(GeofenceJSON.getGeofenceString());

        CTGeofenceSettings currentGeofenceSettings = new CTGeofenceSettings.Builder()
                .setGeofenceMonitoringCount(2)
                .build();

        when(ctGeofenceAPI.getGeofenceSettings()).thenReturn(currentGeofenceSettings);

        GeofenceUpdateTask updateTask = new GeofenceUpdateTask(application, null);

        updateTask.execute();

        ArgumentCaptor<List<CTGeofence>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OnSuccessListener> argumentCaptorOnSuccessListener = ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(ctGeofenceAdapter).addAllGeofence(argumentCaptor.capture(), argumentCaptorOnSuccessListener.capture());
        assertEquals(argumentCaptor.getValue().size(), 2);

        argumentCaptorOnSuccessListener.getValue().onSuccess(null);

        ArgumentCaptor<JSONObject> argumentCaptorJson = ArgumentCaptor.forClass(JSONObject.class);

        verifyStatic(FileUtils.class);
        FileUtils.writeJsonToFile(any(Context.class), anyString(), anyString(), argumentCaptorJson.capture());

        JSONAssert.assertEquals(GeofenceJSON.getGeofence(), argumentCaptorJson.getValue(), true);
    }

}
