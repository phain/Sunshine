package com.example.user.sunshine;

import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.user.sunshine.data.WeatherContract;
import com.example.user.sunshine.sync.SunshineSyncAdapter;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public final static int FORECAST_LOADER_ID = 0;
    public final static String POSITION_KEY = "position";
    public final static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private ForecastAdapter forecastAdapter;
    private int mPosition = -1;
    private ListView listView;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;
    private boolean useSpecialLayout;


    public MainActivityFragment()
    {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        forecastAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        forecastAdapter.swapCursor(data);
        if (mPosition >= 0)
        {
            listView.smoothScrollToPosition(mPosition);
        }
        if (!useSpecialLayout && mPosition == -1)
        {
            mPosition = 0;
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid)
                {
                    listView.performItemClick(listView.getChildAt(mPosition), mPosition, listView.getItemIdAtPosition(mPosition));
                    super.onPostExecute(aVoid);
                }
            }.execute();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        if (menu.findItem(R.id.action_refresh) == null)
        {
            inflater.inflate(R.menu.forcast_fragment, menu);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, savedInstanceState, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY))
        {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        forecastAdapter.setUseSpecialViewType(useSpecialLayout);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l)
            {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null && cursor.moveToFirst())
                {
                    mPosition = position;
                    WeatherDetailCallback wdc = (WeatherDetailCallback) getActivity();
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    wdc.onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                }
            }
        });

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(POSITION_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    public void updateWeather()
    {
//        Bundle syncSettingsBundle = new Bundle();
//        syncSettingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        syncSettingsBundle.putString(SunshineSyncAdapter.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
//        ContentResolver.requestSync(SunshineSyncAdapter.getSyncAccount(getActivity()), getString(R.string.content_authority), syncSettingsBundle);
        SunshineSyncAdapter.syncImmediately(getActivity());

    }

    public void setUseSpecialLayout(boolean useSpecialLayout)
    {
        this.useSpecialLayout = useSpecialLayout;
        if (forecastAdapter != null)
        {
            forecastAdapter.setUseSpecialViewType(this.useSpecialLayout);
        }
    }

}

