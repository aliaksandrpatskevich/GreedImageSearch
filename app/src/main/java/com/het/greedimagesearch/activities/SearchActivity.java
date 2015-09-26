package com.het.greedimagesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.het.greedimagesearch.R;
import com.het.greedimagesearch.SQLite.DatabaseHelper;
import com.het.greedimagesearch.adapters.ImageResultsAdapter;
import com.het.greedimagesearch.dialogs.FilterDialog;
import com.het.greedimagesearch.listeners.EndlessScrollListener;
import com.het.greedimagesearch.models.Filter;
import com.het.greedimagesearch.models.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity
        implements FilterDialog.FilterDialogListener {

    private StaggeredGridView gvResults;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    Filter filter = new Filter();
    String query = "";
    private SearchView searchView;
    int startPage = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.icon);

        setupViews();

        imageResults = new ArrayList<ImageResult>();
        aImageResults = new ImageResultsAdapter(this, imageResults);
        gvResults.setAdapter(aImageResults);

        //        GET FILTER FROM SQLITE
        // Get singleton instance of database
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        // Get saved filter
        filter = databaseHelper.getFilter();
    }

    private void setupViews() {
//        gvResults = (GridView) findViewById(R.id.gvResults);
        gvResults = (StaggeredGridView) findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                ImageResult result = imageResults.get(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });

        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                onImageSearch(query, page - 1);
                // or customLoadMoreDataFromApi(totalItemsCount);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(false);
        searchView.onActionViewExpanded();
        searchView.requestFocus();
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                // perform query here
                query = queryText;
                searchView.clearFocus();//hide keyboard
//                new search
                aImageResults.clear();
                onImageSearch(query, 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void onImageSearch(String query, int start) {
        if (isNetworkAvailable()) {
            //        create network client
            AsyncHttpClient client = new AsyncHttpClient();
            startPage = start;

            String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + query + "&rsz=8&start=" + startPage * 8;
            if (filter.size != null && !filter.size.equals("any")) {
                searchUrl = searchUrl + "&imgsz=" + filter.size;
            }
            if (filter.color != null && !filter.color.equals("any")) {
                searchUrl = searchUrl + "&imgcolor=" + filter.color;
            }
            if (filter.type != null && !filter.type.equals("any")) {
                searchUrl = searchUrl + "&imgtype=" + filter.type;
            }

            if (filter.site != null) {
                searchUrl = searchUrl + "&as_sitesearch=" + filter.site;
            }

            client.get(searchUrl, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("debug", response.toString());
                    JSONArray imageResultsJSON = null;
                    try {
                        imageResultsJSON = response.getJSONObject("responseData").getJSONArray("results");
                        aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJSON));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Check internet connection", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void onSettings(MenuItem mi) {
//
////        showFilterDialog();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
////            builder.setMessage(Html.fromHtml(comments));
//        builder.setTitle("Advanced filters");
//        builder.setMessage(Html.fromHtml("2342342342342343423423434342q34"));
//        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.show();
//    }

    public void showFilterDialog(MenuItem mi) {
        FragmentManager fm = getSupportFragmentManager();
        FilterDialog filterDialog = FilterDialog.newInstance(filter);
        filterDialog.show(fm, "fragment_filter");
    }


    @Override
    public void onFinishFilterDialogListener(Filter filter) {
        this.filter = filter;

        //                new search
        aImageResults.clear();
        searchView.clearFocus();//hide keyboard
        onImageSearch(query, 0); //new search with updated filter
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}

