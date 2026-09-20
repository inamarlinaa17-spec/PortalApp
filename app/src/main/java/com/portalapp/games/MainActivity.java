package com.portalapp.games;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Switch switchToggle;
    private TextView tvOffState;
    
    private SharedPreferences prefs;
    private boolean isEnabled = true;
    private boolean speedhackEnabled = true;
    private float speedMultiplier = 2.0f;
    private boolean adSkipEnabled = true;
    private boolean autoBypassEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("PortalAppPrefs", MODE_PRIVATE);
        loadPreferences();

        webView = findViewById(R.id.webView);
        switchToggle = findViewById(R.id.switchToggle);
        tvOffState = findViewById(R.id.tvOffState);

        setupWebView();

        switchToggle.setChecked(isEnabled);
        switchToggle.setText(isEnabled ? "ON " : "OFF ");
        switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEnabled = isChecked;
            switchToggle.setText(isChecked ? "ON " : "OFF ");
            savePreferences();
            
            if (isChecked) {
                tvOffState.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                injectAdTurboScripts();
            } else {
                tvOffState.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }
        });

        // Icon ⚙️ Settings tetap bisa diklik walaupun status OFF
        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettingsDialog());

        // Langsung tampilkan website asli
        webView.loadUrl("http://App.portalapp.games");
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isEnabled) {
                    injectAdTurboScripts();
                }
            }
        });
    }

    // Injection Porting Fitur AdTurbo v4
    private void injectAdTurboScripts() {
        if (!isEnabled) return;

        // 1. Speedhack Engine (setTimeout & setInterval override)
        String speedhackScript = "javascript:(function() {" +
                "if(window.__speedhackInjected) return;" +
                "window.__speedhackInjected = true;" +
                "var mult = " + speedMultiplier + ";" +
                "if(!" + speedhackEnabled + ") mult = 1.0;" +
                "var origSetTimeout = window.setTimeout;" +
                "window.setTimeout = function(fn, delay) { return origSetTimeout(fn, delay / mult); };" +
                "var origSetInterval = window.setInterval;" +
                "window.setInterval = function(fn, delay) { return origSetInterval(fn, delay / mult); };" +
                "})();";

        // 2. Auto Ad-Skip & Speedup
        String adSkipScript = "javascript:(function() {" +
                "if(!" + adSkipEnabled + ") return;" +
                "setInterval(function() {" +
                "  var vids = document.querySelectorAll('video');" +
                "  vids.forEach(v => { v.playbackRate = 16.0; v.muted = true; v.currentTime = v.duration || 999; });" +
                "  var skipBtns = document.querySelectorAll('.ytp-ad-skip-button, .ad-skip-button, [class*=\"skip\"], [id*=\"skip\"]');" +
                "  skipBtns.forEach(btn => btn.click());" +
                "}, 500);" +
                "})();";

        // 3. Bypass Delay / Auto Clicker
        String bypassScript = "javascript:(function() {" +
                "if(!" + autoBypassEnabled + ") return;" +
                "setInterval(function() {" +
                "  var countElements = document.querySelectorAll('[id*=\"timer\"], [class*=\"timer\"], [id*=\"countdown\"]');" +
                "  countElements.forEach(e => { e.innerText = '0'; });" +
                "}, 1000);" +
                "})();";

        webView.evaluateJavascript(speedhackScript, null);
        webView.evaluateJavascript(adSkipScript, null);
        webView.evaluateJavascript(bypassScript, null);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        CheckBox chkSpeedhack = dialogView.findViewById(R.id.chkSpeedhack);
        SeekBar seekBarSpeed = dialogView.findViewById(R.id.seekBarSpeed);
        CheckBox chkAdSkip = dialogView.findViewById(R.id.chkAdSkip);
        CheckBox chkAutoBypass = dialogView.findViewById(R.id.chkAutoBypass);
        Button btnReload = dialogView.findViewById(R.id.btnReload);

        chkSpeedhack.setChecked(speedhackEnabled);
        seekBarSpeed.setProgress((int) (speedMultiplier * 10));
        chkAdSkip.setChecked(adSkipEnabled);
        chkAutoBypass.setChecked(autoBypassEnabled);

        btnReload.setOnClickListener(v -> {
            speedhackEnabled = chkSpeedhack.isChecked();
            speedMultiplier = Math.max(1.0f, seekBarSpeed.getProgress() / 10.0f);
            adSkipEnabled = chkAdSkip.isChecked();
            autoBypassEnabled = chkAutoBypass.isChecked();
            savePreferences();

            webView.reload();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isEnabled", isEnabled);
        editor.putBoolean("speedhackEnabled", speedhackEnabled);
        editor.putFloat("speedMultiplier", speedMultiplier);
        editor.putBoolean("adSkipEnabled", adSkipEnabled);
        editor.putBoolean("autoBypassEnabled", autoBypassEnabled);
        editor.apply();
    }

    private void loadPreferences() {
        isEnabled = prefs.getBoolean("isEnabled", true);
        speedhackEnabled = prefs.getBoolean("speedhackEnabled", true);
        speedMultiplier = prefs.getFloat("speedMultiplier", 2.0f);
        adSkipEnabled = prefs.getBoolean("adSkipEnabled", true);
        autoBypassEnabled = prefs.getBoolean("autoBypassEnabled", true);
    }
}
