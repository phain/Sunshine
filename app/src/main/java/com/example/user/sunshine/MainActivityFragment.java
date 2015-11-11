package com.example.user.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
{
    public final static String LOG_TAG = "MainActivityFragment";
    ArrayAdapter<String> arrayAdapter;

    public MainActivityFragment()
    {

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
        inflater.inflate(R.menu.forcast_fragment, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_refresh)
        {
            String postcode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.pref_postcode_key), getResources().getString(R.string.pref_postcode_default));
            new FetchWeatherClass().execute(postcode + ",HU");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forcast, R.id.list_item_forcast_textview);

        String postcode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.pref_postcode_key), getResources().getString(R.string.pref_postcode_default));
        String units = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.pref_units_key), getResources().getString(R.string.pref_units_default));
        new FetchWeatherClass().execute(postcode + ",HU", units);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String text = parent.getAdapter().getItem(position).toString();
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchWeatherClass extends AsyncTask<String, Void, String[]>
    {
        public static final String OWM_LIST = "list";
        public static final String OWM_TEMP = "main";
        public static final String OWM_MIN = "temp_min";
        public static final String OWM_MAX = "temp_max";
        public static final String OWM_WEATHER = "weather";
        public static final String OWM_DESCRIPTION = "main";

        private String getReadableDateString(long time)
        {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low, String units)
        {
            double high_t = high;
            double low_t = low;
            //convert celsius to fahrenheit if config tells us to
            if (units.equals("imperial"))
            {
                high_t = high_t*9/5+32;
                low_t = low_t*9/5+32;
            }
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high_t);
            long roundedLow = Math.round(low_t);


            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        private String[] getWeatherDataFromJson(String data)
                throws JSONException
        {
            String[] resultArray;

            JSONObject jsonData = new JSONObject(data);
            JSONArray dataList = jsonData.getJSONArray(OWM_LIST);

            resultArray = new String[dataList.length()];


            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            JSONObject forecast;
            String units = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.pref_units_key), getResources().getString(R.string.pref_units_default));
            for (int i = 0; i < dataList.length(); i++)
            {
                String day;
                String description;
                String highAndLow;


                long dateTime;

                dateTime = gregorianCalendar.getTimeInMillis();
                day = getReadableDateString(dateTime);

                gregorianCalendar.add(GregorianCalendar.DATE, 1);

                forecast = dataList.getJSONObject(i);

                JSONArray weatherTmp = forecast.getJSONArray(OWM_WEATHER);
                JSONObject weather = weatherTmp.getJSONObject(0);
                description = weather.getString(OWM_DESCRIPTION);

                JSONObject temps = forecast.getJSONObject(OWM_TEMP);
                highAndLow = formatHighLows(temps.getDouble(OWM_MAX), temps.getDouble(OWM_MIN),units);

                resultArray[i] = day + " - " + description + " - " + highAndLow;

            }


            return resultArray;
        }

        @Override
        protected String[] doInBackground(String... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String data = null;
            String[] result;

            try
            {
                if (params.length <= 1)
                {
                    return null;
                }
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http").authority("api.openweathermap.org").appendPath("data").appendPath("2.5").appendPath("forecast");
                uriBuilder.appendQueryParameter("q", params[0]);
                uriBuilder.appendQueryParameter("mode", "json");
                uriBuilder.appendQueryParameter("units", "metric");
                uriBuilder.appendQueryParameter("cnt", "7");
                uriBuilder.appendQueryParameter("appid", "6fecad98a2db511b678e238e17de7cc5");

                URL url = new URL(uriBuilder.toString());
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=7625,HU&mode=json&units=metric&cnt=7&appid=bd82977b86bf27fb59a04b61b657fb6f");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                line = reader.readLine();
                while (line != null && line != "")
                {
                    buffer.append(line + "\n");
                    line = reader.readLine();
                }

                if (buffer.length() == 0)
                {
                    return null;
                }

                data = buffer.toString();

                result = getWeatherDataFromJson(data);

                return result;

            }
            catch (IOException ex)
            {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                System.out.println(ex.getMessage());
                return null;
            }
            catch (JSONException ex)
            {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                return null;
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException ex)
                    {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                    }
                }
            }
        }


        @Override
        protected void onPostExecute(String[] result)
        {
            if (result != null)
            {
                ArrayList<String> data = new ArrayList<>(Arrays.asList(result));
                arrayAdapter.clear();
                arrayAdapter.addAll(data);
            }
        }
    }
}

