package com.iitd.se.busappconductor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class ConductActivity extends AppCompatActivity {

    private JSONObject user, bus;
    private ImageButton emptyButton, standButton, fullButton;
    private TextView seatAvailTextView;
    private Spinner busStopSpinner;
    private Switch busStatusSwitch;
    private Button nextBusStopButton, prevBusStopButton;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private enum SeatAvail {Empty, Stand, Full}

    ;
    private SeatAvail seatAvail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduct);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Initialize user and bus json
        try {
            user = new JSONObject(getIntent().getExtras().getString("user"));
            bus = new JSONObject(getIntent().getExtras().getString("bus"));
            seatAvail = SeatAvail.values()[bus.getInt("seat_avail")];
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Set onClick listeners for buttons
        emptyButton = (ImageButton) findViewById(R.id.imageButtonEmpty);
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSeatAvailTask updateSeatAvailTask = new UpdateSeatAvailTask(SeatAvail.Empty);
                updateSeatAvailTask.execute((Void) null);
            }
        });

        standButton = (ImageButton) findViewById(R.id.imageButtonStand);
        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSeatAvailTask updateSeatAvailTask = new UpdateSeatAvailTask(SeatAvail.Stand);
                updateSeatAvailTask.execute((Void) null);
            }
        });

        fullButton = (ImageButton) findViewById(R.id.imageButtonFull);
        fullButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSeatAvailTask updateSeatAvailTask = new UpdateSeatAvailTask(SeatAvail.Full);
                updateSeatAvailTask.execute((Void) null);
            }
        });

        //Display Seat availability text
        seatAvailTextView = (TextView) findViewById(R.id.textViewSeatAvail);
        updateSeatAvailability();

        //Initialize switches
        busStatusSwitch = (Switch) findViewById(R.id.switchBusStatus);
        updateBusStatus();
        busStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String newStatus;
                if (b)
                    newStatus = "on";
                else
                    newStatus = "off";
                UpdateBusStatusTask updateBusStatusTask = new UpdateBusStatusTask(newStatus);
                updateBusStatusTask.execute((Void) null);
            }
        });

        //Initialize spinner
        busStopSpinner = (Spinner) findViewById(R.id.spinnerBusStop);
        ArrayList<String> busStops = getBusStopNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, R.id.textViewNextBusStop, busStops);
        busStopSpinner.setAdapter(adapter);
        busStopSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String busStop = adapterView.getItemAtPosition(i).toString();
                String busStopId = findBusStopId(busStop);
                UpdateNextBusStop updateNextBusStop = new UpdateNextBusStop(busStopId);
                updateNextBusStop.execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Initialize buttons
        nextBusStopButton = (Button) findViewById(R.id.buttonNextBusStop);
        nextBusStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int currentPosition = busStopSpinner.getSelectedItemPosition();
                int totalPositions = busStopSpinner.getAdapter().getCount();
                int nextSelection = (currentPosition + 1) % totalPositions;
                busStopSpinner.setSelection(nextSelection);
            }
        });

        prevBusStopButton = (Button) findViewById(R.id.buttonPrevBusStop);
        prevBusStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = busStopSpinner.getSelectedItemPosition();
                int totalPositions = busStopSpinner.getAdapter().getCount();
                int nextSelection = (totalPositions + currentPosition - 1) % totalPositions;
                busStopSpinner.setSelection(nextSelection);
            }
        });

        //Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        //if(checkPermissionsForLocation())
        //    requestLocationUpdates();
    }

    private boolean checkPermissionsForLocation() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 1);
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermissionsForLocation())
            requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if(checkPermissionsForLocation())
        //    locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(checkPermissionsForLocation())
            locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    requestLocationUpdates();
                break;
        }
    }


    private void requestLocationUpdates() {
        Log.e("Nothing", "doInBackground1: ");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void updateBusStatus() {
        try {
            String busStatus = bus.getString("status");
            if(busStatus.equals("on")) {
                busStatusSwitch.setChecked(true);
                busStatusSwitch.setText("Bus Status (On)");
            } else {
                busStatusSwitch.setChecked(false);
                busStatusSwitch.setText("Bus Status (Off)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateSeatAvailability() {
        try {
            String seatAvailText = "";
            seatAvail = SeatAvail.values()[bus.getInt("seat_avail")];
            switch (seatAvail) {
                case Empty:
                    seatAvailText = "Seats available";
                    break;
                case Stand:
                    seatAvailText = "Space to stand only";
                    break;
                case Full:
                    seatAvailText = "No space available";
                    break;
            }
            seatAvailTextView.setText(seatAvailText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getBusStopNames() {
        ArrayList<String> busStops = new ArrayList<String>();
        try {
            JSONArray busStopsJson = bus.getJSONArray("route");
            for(int i = 0; i < busStopsJson.length(); i++) {
                busStops.add(busStopsJson.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return busStops;
    }

    private String findBusStopId(String busStop) {
        String busStopId = "";
        try {
            JSONArray busStopsJson = bus.getJSONArray("route");
            for(int i = 0; i < busStopsJson.length(); i++) {
                if(busStop.equals(busStopsJson.getJSONObject(i).getString("name"))) {
                    busStopId = busStopsJson.getJSONObject(i).getString("id");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return busStopId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.conduct_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout_action:
                // TODO: Logout and switch to login activity
                UserLogoutTask userLogoutTask = new UserLogoutTask();
                userLogoutTask.execute((Void)null);
                Intent getActivityLoginIntent = new Intent(this, LoginActivity.class);
                startActivity(getActivityLoginIntent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public class UserLogoutTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
                httpHelper.deleteJson("/users/sign_out.json", user.getString("email"), user.getString("authentication_token"));
                user.put("email", "");
                user.put("authentication_token", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }
    }

    public class UpdateSeatAvailTask extends AsyncTask<Void, Void, Void> {

        private JSONObject params;
        private String toast;

        public UpdateSeatAvailTask(SeatAvail seatAvail) {
            params = new JSONObject();
            toast = null;
            try {
                params.put("id", bus.getString("id"));
                params.put("seat_avail", seatAvail.ordinal());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
                JSONObject jsonResonse = httpHelper.putJson("/buses/" + bus.getString("id") + ".json", params, user.getString("email"), user.getString("authentication_token"));
                if(jsonResonse != null) {
                    if(bus.has("bus"))
                        bus = jsonResonse.getJSONObject("bus");
                    else
                        toast = "You need to log in...";
                } else {
                    toast = "Connection problem...";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(toast != null)
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_LONG).show();
            else
                updateSeatAvailability();
        }
    }

    public class UpdateBusStatusTask extends AsyncTask<Void, Void, Void> {

        private JSONObject params;
        private String toast;

        public UpdateBusStatusTask(String status) {
            params = new JSONObject();
            toast = null;
            try {
                params.put("id", bus.getString("id"));
                params.put("status", status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
                JSONObject jsonResonse = httpHelper.putJson("/buses/" + bus.getString("id") + ".json", params, user.getString("email"), user.getString("authentication_token"));
                if(jsonResonse != null) {
                    if(jsonResonse.has("bus"))
                        bus = jsonResonse.getJSONObject("bus");
                    else
                        toast = "You need to log in...";
                } else {
                    toast = "Connection problem...";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(toast != null)
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_LONG).show();
            else
                updateBusStatus();
        }
    }

    public class UpdateNextBusStop extends AsyncTask<Void, Void, Void> {

        private JSONObject params;
        private String toast;

        public UpdateNextBusStop(String busStopId) {
            params = new JSONObject();
            toast = null;
            try {
                params.put("id", bus.getString("id"));
                params.put("bus_stop_id", busStopId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
                JSONObject jsonResonse = httpHelper.putJson("/buses/" + bus.getString("id") + ".json", params, user.getString("email"), user.getString("authentication_token"));
                if(jsonResonse != null) {
                    if(jsonResonse.has("bus"))
                        bus = jsonResonse.getJSONObject("bus");
                    else
                        toast = "You need to log in...";
                } else {
                    toast = "Connection problem...";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(toast != null)
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_LONG).show();
            //updateBusStatus();
        }
    }

    public class UpdateBusPosition extends AsyncTask<Void, Void, Void> {

        private JSONObject params;
        private String toast;

        public UpdateBusPosition(double lat, double lng) {
            params = new JSONObject();
            toast = null;
            try {
                params.put("latitude", lat);
                params.put("longitude", lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
                JSONObject jsonResonse = httpHelper.putJson("/buses/" + bus.getString("id") + ".json", params, user.getString("email"), user.getString("authentication_token"));
                if(jsonResonse != null) {
                    if(jsonResonse.has("bus"))
                        bus = jsonResonse.getJSONObject("bus");
                    else
                        toast = "You need to log in...";
                } else {
                    toast = "Connection problem...";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(toast != null)
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_LONG).show();
            //updateBusStatus();
        }
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Toast.makeText(getBaseContext(), "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                UpdateBusPosition updateBusPosition = new UpdateBusPosition(location.getLatitude(), location.getLongitude());
                updateBusPosition.execute((Void)null);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            String statusString = "";
            switch (i) {
                case android.location.LocationProvider.AVAILABLE:
                    statusString = "available";
                case android.location.LocationProvider.OUT_OF_SERVICE:
                    statusString = "out of service";
                case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "temporarily unavailable";
            }
            Toast.makeText(getBaseContext(),
                    s + " " + statusString,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(getBaseContext(),
                    "Provider: " + s + " enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
}
