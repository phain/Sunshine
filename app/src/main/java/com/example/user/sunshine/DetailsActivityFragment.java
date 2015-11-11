package com.example.user.sunshine;

import android.content.Intent;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment
{

    private ShareActionProvider shareActionProvider;
    private String forcastDetails;

    public DetailsActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        forcastDetails = this.getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

        ((TextView)rootView.findViewById(R.id.forecast_details)).setText(forcastDetails);


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

        Log.v("DetailsActivity", getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT) + "  #SunshineApp");
        setShareIntent(createShareIntent());
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, forcastDetails + " #SunshineApp");

        return shareIntent;
    }


}
