package com.example.user.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public final static int DETAILS_LOADER_ID = 1;

    private final static String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    private ShareActionProvider shareActionProvider;
    private String forecastData;
    private String forecastString;

    TextView dayView;
    TextView dateView;
    TextView highTempView;
    TextView lowTempView;
    ImageView iconView;
    TextView weatherDescView;
    TextView humidityView;
    TextView windView;
    TextView pressureView;

    private static final String[] FORECAST_COLUMNS = {
                            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                            WeatherContract.WeatherEntry.COLUMN_DATE,
                            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                            WeatherContract.WeatherEntry.COLUMN_DEGREES,
                            WeatherContract.WeatherEntry.COLUMN_PRESSURE
                        };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_CONDITION_ID = 3;
    private static final int COL_WEATHER_MAX_TEMP = 4;
    private static final int COL_WEATHER_MIN_TEMP = 5;
    private static final int COL_WEATHER_HUM = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_WIND_DIR = 8;
    private static final int COL_WEATHER_PRESSURE = 9;

    public DetailsActivityFragment()
    {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAILS_LOADER_ID,savedInstanceState, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        dayView = (TextView)rootView.findViewById(R.id.details_day);
        dateView = (TextView)rootView.findViewById(R.id.details_date);
        highTempView = (TextView)rootView.findViewById(R.id.details_temp_high);
        lowTempView = (TextView)rootView.findViewById(R.id.details_temp_low);
        iconView = (ImageView)rootView.findViewById(R.id.details_icon);
        weatherDescView = (TextView)rootView.findViewById(R.id.details_weather_desc);
        humidityView = (TextView)rootView.findViewById(R.id.details_weather_hum);
        windView = (TextView)rootView.findViewById(R.id.details_weather_wind);
        pressureView = (TextView)rootView.findViewById(R.id.details_weather_pressure);

        if (getActivity().getIntent() != null) {
            forecastString = getActivity().getIntent().getDataString();
        }

        return rootView;
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
        inflater.inflate(R.menu.menu_share, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(forecastData != null)
        {
            setShareIntent(createShareIntent());
        }
    }

    private void setShareIntent(Intent shareIntent)
    {
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Intent createShareIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastData + " #SunshineApp");

        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(getActivity(), getActivity().getIntent().getData(), FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if(!data.moveToFirst())
        {
            return;
        }

        dayView.setText(Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE)));
        dateView.setText(Utility.getFormattedMonthDay(getActivity(), data.getLong(COL_WEATHER_DATE)));
        highTempView.setText(Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity())));
        lowTempView.setText(Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity())));
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));
        weatherDescView.setText(data.getString(COL_WEATHER_DESC));
        humidityView.setText(Utility.formatHumidity(getActivity(), data.getDouble(COL_WEATHER_HUM)));
        windView.setText(Utility.formatWind(getActivity(), data.getDouble(COL_WEATHER_WIND_SPEED), data.getDouble(COL_WEATHER_WIND_DIR)));
        pressureView.setText(Utility.formatPressure(getActivity(),data.getDouble(COL_WEATHER_PRESSURE)));

        forecastData = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        forecastData += " - " + data.getString(COL_WEATHER_DESC);
        forecastData += " - " + Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity()));
        forecastData += "/" + Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP),Utility.isMetric(getActivity()));

        setShareIntent(createShareIntent());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}
