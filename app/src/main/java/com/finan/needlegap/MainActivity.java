package com.finan.needlegap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {
    private WebView webView;
    private RewardedAd rewardedAd;
    private boolean rewardedAdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(0xff050508);
        getWindow().setNavigationBarColor(0xff050508);

        webView = new WebView(this);
        setContentView(webView);
        configureWebView();
        MobileAds.initialize(this);
        loadRewardedAd();
        hideSystemBars();
        webView.loadUrl("file:///android_asset/index.html");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.addJavascriptInterface(new AdsBridge(), "AndroidAds");
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xff050508);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
    }

    private void loadRewardedAd() {
        if (rewardedAd != null || rewardedAdLoading) {
            return;
        }
        rewardedAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(
            this,
            getString(com.finan.needlegap.R.string.rewarded_ad_unit_id),
            adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                    rewardedAdLoading = false;
                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            rewardedAd = null;
                            hideSystemBars();
                            loadRewardedAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            rewardedAd = null;
                            notifyAdUnavailable();
                            hideSystemBars();
                            loadRewardedAd();
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    rewardedAd = null;
                    rewardedAdLoading = false;
                    notifyAdUnavailable();
                }
            }
        );
    }

    private void showRewardedAd() {
        runOnUiThread(() -> {
            if (rewardedAd == null) {
                notifyAdUnavailable();
                loadRewardedAd();
                return;
            }
            rewardedAd.show(this, rewardItem -> {
                if (webView != null) {
                    webView.evaluateJavascript("window.grantExtraLifeFromAd && window.grantExtraLifeFromAd();", null);
                }
            });
        });
    }

    private void notifyAdUnavailable() {
        if (webView == null) {
            return;
        }
        runOnUiThread(() ->
            webView.evaluateJavascript("window.onRewardedAdUnavailable && window.onRewardedAdUnavailable();", null)
        );
    }

    private class AdsBridge {
        @JavascriptInterface
        public void showRewardedAd() {
            MainActivity.this.showRewardedAd();
        }
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
