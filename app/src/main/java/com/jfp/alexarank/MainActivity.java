package com.jfp.alexarank;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.jfp.alexarank.databinding.ActivityMainBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Locale;

import me.samlss.bloom.Bloom;
import me.samlss.bloom.effector.BloomEffector;
import me.samlss.bloom.listener.BloomListener;

public class MainActivity extends AppCompatActivity {
    String site_prefix = "";
    ActivityMainBinding binding;
    Snackbar snackbar;
    boolean explode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        init();

    }

    public void init() {

        binding.loading.animate().alpha(0).setDuration(0);
        Glide.with(MainActivity.this).asGif().load(R.raw.loading).into(binding.loading);

        binding.card.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
            binding.card.startAnimation(animation);
        });

        binding.searchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doSearch();
                return true;
            }
            return false;
        });

        binding.searchButton.setOnLongClickListener(v -> true);
        binding.searchButton.setOnClickListener(v -> {
            doSearch();
        });
    }

    public void doSearch() {
        hideKeyboard(MainActivity.this);
        if (snackbar != null) {
            snackbar.dismiss();
        }
        if (binding.searchView.getText().toString().length() != 0) {
            if (!android.util.Patterns.WEB_URL.matcher(binding.searchView.getText().toString().trim()).matches()) {
                makeSnackBar("Incorrect  url :(", "OK");
            } else {
                try {
                    if (!binding.searchView.getText().toString().contains("https://") && !binding.searchView.getText().toString().contains("http://")) {
                        site_prefix = "https://" + binding.searchView.getText().toString();
                    } else {
                        site_prefix = binding.searchView.getText().toString();
                    }
                    site_prefix = getDomainName(site_prefix);

                    new fetcher().execute();

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else {
            makeSnackBar("Url is empty !", "OK");
        }
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url.trim());
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public void makeSnackBar(String title, String button) {
        snackbar = Snackbar.make(binding.root, title, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(button, view -> snackbar.dismiss());

        snackbar.setActionTextColor(Color.RED);

        View snackbarView = snackbar.getView();
        TextView snackbarText = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarText.setTextColor(Color.BLACK);
        snackbarView.setBackgroundColor(Color.parseColor("#FFAC00"));
        snackbar.show();
    }

    public class fetcher extends AsyncTask<Void, Void, String> {
        String url = "https://www.alexa.com/minisiteinfo/" + site_prefix;

        @Override
        protected String doInBackground(Void... arg0) {

            Document document = null;
            try {
                document = Jsoup.connect(url).get();

                int table_counter = 0;
                for (Element table : document.select("table")) {
                    for (Element row : table.select("tr")) {
                        Elements tds = row.select("td");
                        if (tds.size() >= 3) {
                            Log.d("table", tds.get(0).text() + "----" + tds.get(1).text() + "----" + tds.get(2).text());
                            Log.d("table_things", tds.get(0).html() + "----" + tds.get(1).html() + "----" + tds.get(2).html());

                            Element country_flag = tds.get(1).select("img").first();
                            Element a_tag = tds.get(1).select("a").first();
                            String img_url = country_flag.attr("src");
                            String title = a_tag.attr("title");


                            runOnUiThread(() -> {
                                binding.global.setText("Global Rank:  ");
                                binding.country.setText("Rank in:  ");
                                binding.linking.setText("Sites Linking:  ");
                                binding.websiteLink.setText(site_prefix);
                                binding.searchView.setText("");
                                Glide.with(MainActivity.this).load(img_url).into(binding.countryFlag);
                                if (!tds.get(0).text().contains("No Data")) {
                                    binding.global.append(NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(tds.get(0).text().replaceAll("\\D+", ""))));
                                    explode = true;
                                } else
                                    binding.global.append("No Data");
                                if (!tds.get(1).text().contains("No Data")) {
                                    binding.country.setText("Rank in " + title + ":  " + NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(tds.get(1).text().replaceAll("\\D+", ""))));
                                    explode = true;
                                } else
                                    binding.country.append("No Data");
                                if (!tds.get(2).text().contains("No Data"))
                                    binding.linking.append(NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(tds.get(2).text().replaceAll("\\D+", ""))));
                                else
                                    binding.linking.append("No Data");
                            });
                            table_counter++;
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                                binding.searchView.setText("");
                            });

                        }
                    }
                }


            } catch (IOException e) {
                Log.e("eps", e.toString());
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            binding.loading.animate().alpha(1).setDuration(500);
        }

        @Override
        protected void onPostExecute(String result) {
            binding.loading.animate().alpha(0).setDuration(500);

            if (explode) {
                Bloom.with(MainActivity.this)
                        .setParticleRadius(10)
                        .setBloomListener(new BloomListener() {
                            @Override
                            public void onBegin() {
                                binding.help.setVisibility(View.GONE);
                            }

                            @Override
                            public void onEnd() {
                                binding.mainThings.setVisibility(View.VISIBLE);
                            }
                        })
                        .setEffector(new BloomEffector.Builder()
                                .setDuration(1000)
                                .setAnchor(binding.help.getWidth() / 2, binding.help.getHeight() / 2)
                                .build())
                        .boom(binding.help);
            }

        }

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
