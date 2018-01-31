package com.github.imageviewer;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.imageviewer.helper.FlickrImage;
import com.github.imageviewer.helper.GridSpacingItemDecoration;
import com.github.imageviewer.helper.IVConstants;
import com.github.imageviewer.helper.IVHelper;
import com.github.imageviewer.helper.OnItemClickListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LIST_STATE = "listState";

    private RecyclerView mRecyclerView = null;
    private ProgressBar mProgressBar = null;
    private GridLayoutAdapter mAdapter = null;

    private List<FlickrImage> mFlickrImageList = null;
    private Parcelable mListState = null;
    private MaterialSearchView mSearchView;
    private JSONDownloaderTask mJsonTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"onCreate() " );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.content_progress_bar);
        mSearchView = findViewById(R.id.search_view);

        mAdapter = new GridLayoutAdapter(this, mFlickrImageList, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, IVHelper.getInstance().dpToPx(this, 8), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        //Log.e(TAG, "Checking network : " + IVHelper.getInstance().isNetworkAvailable(this));

        LinearLayout noNetMsg = findViewById(R.id.no_internet_layout);
        if (!IVHelper.getInstance().isNetworkAvailable(this)) {
            noNetMsg.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mJsonTask = new JSONDownloaderTask(this);
            mJsonTask.execute();
        }

        setupSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mFlickrImageList != null && !mFlickrImageList.isEmpty()) {
            menu.findItem(R.id.action_search).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    // Write list state to bundle
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(LIST_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState());
        //Log.e(TAG, "onSaveInstanceState : " + state);
    }

    // Restore list state from bundle
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Log.e(TAG, "onRestoreInstanceState : " + state);
        if(state != null) {
            mListState = state.getParcelable(LIST_STATE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mJsonTask != null && !mJsonTask.isCancelled()) {
            mJsonTask.cancel(true);
        }
        mRecyclerView = null;
        mProgressBar = null;
        mAdapter = null;
        mSearchView = null;
        mFlickrImageList = null;
        super.onDestroy();
    }

    private void setupSearch() {
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processQuery(newText);
                return true;
            }
        });

        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                mAdapter.setImageList(mFlickrImageList);
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void processQuery(String query) {
        List<FlickrImage> result = new ArrayList<>();

        if (mFlickrImageList == null || mFlickrImageList.isEmpty() || query.equals("")) {
            return;
        }
        // case insensitive search
        for (FlickrImage flickrImage : mFlickrImageList) {
            String title = flickrImage.getTitle();
            if (title.toLowerCase().contains(query.toLowerCase())) {
                result.add(flickrImage);
            }
        }
        Log.e(TAG, "got search result : " + result.size());

        LinearLayout errorMsgLayout = findViewById(R.id.no_internet_layout);
        TextView errorTextView = findViewById(R.id.error_message);

        if ((result.isEmpty() ||  result.size() == 0)) {
            mRecyclerView.setVisibility(View.GONE);
            errorMsgLayout.setVisibility(View.VISIBLE);
            errorTextView.setText(getText(R.string.no_result_found_msg));
        } else {
            errorTextView.setText(getText(R.string.internet_error_msg));
            errorMsgLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setImageList(result);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (pos >= 0 && mFlickrImageList != null && !mFlickrImageList.isEmpty()) {
            FlickrImage flickrImage = mFlickrImageList.get(pos);
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_view);
            dialog.setTitle("");
            dialog.setCancelable(true);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int heightPixels = metrics.heightPixels;
            int widthPixels = metrics.widthPixels;
            int width;
            int height;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                width = (int) (widthPixels * 0.8);
                height = (int) (width * 0.6);
            } else {
                width = (int) (widthPixels * 0.5);
                height = (int) (heightPixels * 0.6);
            }

            ImageView imageView = dialog.findViewById(R.id.dialog_image);
            imageView.getLayoutParams().height = height;
            imageView.getLayoutParams().width = width;

            TextView textView = dialog.findViewById(R.id.dialog_title);
            textView.setEllipsize(null);
            textView.setSingleLine(false);

            textView.setText(flickrImage.getTitle());

            Picasso.with(MainActivity.this)
                    .load(flickrImage.getFlickrPhotoURI())
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .into(imageView);

            dialog.show();
        }
    }

    static  class JSONDownloaderTask extends AsyncTask<Void, Void, List<FlickrImage>> {
        private WeakReference<MainActivity> mContext;
        //Context mContext;

        JSONDownloaderTask(MainActivity context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            MainActivity mActivity = mContext.get();
            if (mActivity == null || mActivity.isFinishing() || isCancelled()) {
                return;
            }
            LinearLayout errorMsgLayout = mActivity.findViewById(R.id.no_internet_layout);
            if (mActivity.mRecyclerView.getVisibility() == View.VISIBLE) {
                mActivity.mRecyclerView.setVisibility(View.GONE);
                mActivity.mProgressBar.setVisibility(View.VISIBLE);
            }

            if (errorMsgLayout.getVisibility() == View.VISIBLE &&
                    IVHelper.getInstance().isNetworkAvailable(mActivity)) {
                mActivity.mProgressBar.setVisibility(View.VISIBLE);
                errorMsgLayout.setVisibility(View.GONE);
            }
        }

        @Override
        protected List<FlickrImage> doInBackground(Void... params) {
            String url = IVConstants.FLICKER_QUERY_URL
                    + IVConstants.FLICKR_QUERY_PER_PAGE
                    + IVConstants.FLICKR_QUERY_PAGES
                    + IVConstants.FLICKR_QUERY_NOJSONCALLBACK
                    + IVConstants.FLICKR_QUERY_FORMAT
                    + IVConstants.FLICKR_QUERY_KEY + IVConstants.FLICKER_API_KEY;
            List<FlickrImage> postsArray = null;
            try {
                URL postsURL = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) postsURL.openConnection();


                InputStream ins = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(ins));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                String queryResult = sb.toString();
                postsArray = parseJSON(queryResult);

                br.close();
                ins.close();
                conn.disconnect();
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL : " + e.getLocalizedMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return postsArray;
        }

        private List<FlickrImage> parseJSON(String json){

            List<FlickrImage> flickrImageList = new ArrayList<>();


            String flickrId;
            String flickrSecret;
            String flickrServer;
            String flickrFarm;
            String flickrTitle;

            try {
                JSONObject JsonObject = new JSONObject(json);
                JSONObject Json_photos = JsonObject.getJSONObject("photos");
                JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

                FlickrImage flickrImage;

                for (int i = 0; i < JsonArray_photo.length(); i++){
                    if (isCancelled()) { break;}
                    JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
                    flickrId = FlickrPhoto.getString("id");
                    flickrSecret = FlickrPhoto.getString("secret");
                    flickrServer = FlickrPhoto.getString("server");
                    flickrFarm = FlickrPhoto.getString("farm");
                    flickrTitle = FlickrPhoto.getString("title");

                    String FlickrPhotoURI = "http://farm" + flickrFarm + ".static.flickr.com/"
                                    + flickrServer + "/" + flickrId + "_" + flickrSecret + "_m.jpg";

                    flickrImage = new FlickrImage(FlickrPhotoURI, flickrTitle);
                    flickrImageList.add(flickrImage);
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return flickrImageList;
        }

        @Override
        protected void onPostExecute(List<FlickrImage> flickrImageList) {
            MainActivity mActivity = mContext.get();
            if (mActivity == null || mActivity.isFinishing() || isCancelled()) {
                return;
            }

            mActivity.mFlickrImageList = flickrImageList;
            if (mActivity.mFlickrImageList == null) {
                LinearLayout errorMsgLayout = mActivity.findViewById(R.id.no_internet_layout);
                mActivity.mProgressBar.setVisibility(View.GONE);
                errorMsgLayout.setVisibility(View.VISIBLE);
                return;
            }
            if (!mActivity.mFlickrImageList.isEmpty()) {
                mActivity.mProgressBar.setVisibility(View.GONE);
                mActivity.mAdapter.setImageList(mActivity.mFlickrImageList);
                mActivity.mAdapter.notifyDataSetChanged();
                mActivity.mRecyclerView.setVisibility(View.VISIBLE);

                if (mActivity.mListState != null) {
                    mActivity.mRecyclerView.getLayoutManager().onRestoreInstanceState(mActivity.mListState);
                }

                mActivity.invalidateOptionsMenu();
            }
        }
    }
}
