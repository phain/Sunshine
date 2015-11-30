package com.example.user.sunshine;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.user.sunshine.sync.SunshineSyncAdapter;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WeatherDetailCallback
{

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private String location;
    private final static String FORECASTFRAGMENT_TAG = "FFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        location = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_postcode_key), getResources().getString(R.string.pref_postcode_default));
        MainActivityFragment ff;
        if (findViewById(R.id.weather_detail_container) != null)
        {
            twoPane = true;
            if (savedInstanceState == null)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailsActivityFragment(), DETAILFRAGMENT_TAG).commit();
            }
            ff =  (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

        }
        else
        {
            ff =  (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
            twoPane = false;
            getSupportActionBar().setElevation(0);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
        ff.setUseSpecialLayout(!twoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_showmap)
        {
            double lat, lon;
            final Geocoder geocoder = new Geocoder(this);
            final String postcode = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_postcode_key), getResources().getString(R.string.pref_postcode_default));
            try
            {
                List<Address> addresses = geocoder.getFromLocationName(postcode, 1);
                if (addresses != null && !addresses.isEmpty())
                {
                    Log.v(LOG_TAG, addresses.get(0).toString());
                    lat = addresses.get(0).getLatitude();
                    lon = addresses.get(0).getLongitude();
                    String uriString = "geo:" + lat + "," + lon + "?z=12";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(uriString));
                    if (intent.resolveActivity(getPackageManager()) != null)
                    {
                        startActivity(intent);
                    }
                }
                else
                {
                    throw new Exception("Location not found");
                }
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!location.equals(Utility.getPreferredLocation(this)))
        {
            MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            Log.v(LOG_TAG, "fragment exists: " + (ff != null));
            location = Utility.getPreferredLocation(this);
            if (ff != null)
            {
                onLocationChanged(ff);
            }
        }
    }

    public void onLocationChanged(MainActivityFragment ff)
    {
        ff.updateWeather();
        ff.getLoaderManager().restartLoader(MainActivityFragment.FORECAST_LOADER_ID, null, ff);
        if(twoPane)
        {
            DetailsActivityFragment df = (DetailsActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            df.onLocationChanged(location);
        }
    }

    @Override
    public void onItemSelected(Uri url)
    {
        if (twoPane)
        {
            DetailsActivityFragment df = DetailsActivityFragment.createNewInstance(url);
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container,df,DETAILFRAGMENT_TAG).commit();
        }
        else
        {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(url);
            startActivity(intent);
        }
    }

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent()
    {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
