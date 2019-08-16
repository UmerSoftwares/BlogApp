package com.website.app;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class web extends AppCompatActivity {
    int topScrollPercent=30;
    AppBarLayout appBar;
    NestedScrollView nestedScrollView;
    WebView webView;
    SwipeRefreshLayout mySwipeRefreshLayout;
    public AdView mAdView;
    public boolean isAdVisible=true;
    private ProgressBar progress_bar_web;
    boolean shouldScroll=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nestedScrollView=findViewById(R.id.nested_scroll);
        FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=webView.getTitle()+"\n"+webView.getUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
        appBar=findViewById(R.id.appbar);


        mAdView = findViewById(R.id.adView);
        if (Modifications.HAS_ADS){

            MobileAds.initialize(this, getString(R.string.admob_app_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    if (!web.this.isAdVisible){
                        web.this.isAdVisible=true;
                        web.this.mAdView.setVisibility(AdView.VISIBLE);
                    }
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    web.this.isAdVisible=false;
                    web.this.mAdView.setVisibility(AdView.GONE);
                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AdRequest adRequest = new AdRequest.Builder().build();
                            web.this.mAdView.loadAd(adRequest);
                        }
                    },5000);

                }

            });
        }else{
            mAdView.setVisibility(View.GONE);
        }
        String URL = getIntent().getStringExtra("URL");
        webView = findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        progress_bar_web = findViewById(R.id.progress_bar_web);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override

            public void onProgressChanged(WebView view, int newProgress) {

                progress_bar_web.setVisibility(View.VISIBLE);

                progress_bar_web.setProgress(newProgress);
                if (newProgress>=topScrollPercent && shouldScroll){
                    returnToTop();
                    shouldScroll=false;
                }
                if (newProgress == 100) {
                    progress_bar_web.setVisibility(View.GONE);

                }

            }



            @Override

            public void onReceivedTitle(WebView view, String title) {
                getSupportActionBar().setTitle(title);
                super.onReceivedTitle(view, title);
            }

        });
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
        webView.loadUrl(URL);

        mySwipeRefreshLayout=findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mySwipeRefreshLayout.setRefreshing(true);
                        webView.reload();
                    }
                }
        );


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mySwipeRefreshLayout.setRefreshing(true);
                webView.reload();
                return true;
            case R.id.action_open_browser:
                launch(webView.getUrl());
                return true;
            case R.id.action_share:
                String text=webView.getTitle()+"\n"+webView.getUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.action_exit:
                web.this.finish();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    public void returnToTop(){
        nestedScrollView.scrollTo(0,0);
        appBar.setExpanded(true,true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    Toast toast;
    public boolean shouldExit=false;
    @Override
    public void onBackPressed(){
        if(webView.canGoBack()) {
            webView.goBack();
        }
        else if (shouldExit)
        {
            toast.cancel();
            super.onBackPressed();
        } else{
            toast=Toast.makeText(this,"Press again to exit",Toast.LENGTH_SHORT);
            toast.show();
            shouldExit=true;
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldExit=false;
                }
            },1500);
        }

    }

    public void launch(String s)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        startActivity(browserIntent);
    }

    private class CustomWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("whatsapp://send") || url.contains("intent://") || url.contains("wa.me/") || url.contains("m.me/") ||url.contains("mailto:") || url.contains("play.google.com"))
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
            else {
                String[] filterUrls=Modifications.FILTER;
                if (filterUrls.length==0){
                    return false;
                }else{
                    for (int i=0;i<filterUrls.length;i++){
                        if (url.toLowerCase().contains(filterUrls[i].toLowerCase())){
                            return false;
                        }
                    }
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        return true;

                }
            }
        }


        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            super.onPageStarted(webview,url,favicon);
            shouldScroll=true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mySwipeRefreshLayout.setRefreshing(false);

        }
    }
}