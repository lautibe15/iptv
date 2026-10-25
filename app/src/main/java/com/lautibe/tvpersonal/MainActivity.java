package com.lautibe.tvpersonal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "tv_personal";
    private static final String KEY_CHANNELS = "channels";

    private final List<Channel> channels = new ArrayList<>();
    private LinearLayout channelList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadChannels();
        buildUi();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, float size, int color) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        return tv;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(28), dp(24), dp(28), dp(24));
        root.setBackgroundColor(Color.rgb(12, 13, 15));

        TextView title = text("TV Personal", 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView subtitle = text("Tus canales HLS · tocá un canal para reproducir", 15, Color.rgb(170, 174, 182));
        subtitle.setPadding(0, dp(4), 0, dp(18));
        root.addView(subtitle);

        Button add = new Button(this);
        add.setText("+  AGREGAR CANAL");
        add.setTextSize(16);
        add.setAllCaps(false);
        add.setMinHeight(dp(56));
        add.setOnClickListener(v -> showChannelDialog(-1));
        root.addView(add, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView hint = text("Consejo: usá enlaces http:// o https:// que terminen en .m3u8.", 13, Color.rgb(125, 130, 140));
        hint.setPadding(0, dp(12), 0, dp(10));
        root.addView(hint);

        ScrollView scroll = new ScrollView(this);
        channelList = new LinearLayout(this);
        channelList.setOrientation(LinearLayout.VERTICAL);
        channelList.setPadding(0, dp(4), 0, dp(32));
        scroll.addView(channelList);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        renderChannels();
    }

    private void renderChannels() {
        channelList.removeAllViews();
        for (int i = 0; i < channels.size(); i++) {
            final int index = i;
            Channel channel = channels.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(5), 0, dp(5));

            Button play = new Button(this);
            play.setText("▶  " + channel.name);
            play.setTextSize(18);
            play.setAllCaps(false);
            play.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            play.setMinHeight(dp(68));
            play.setOnClickListener(v -> playChannel(channel));
            row.addView(play, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            Button edit = new Button(this);
            edit.setText("⋮");
            edit.setTextSize(24);
            edit.setMinWidth(dp(64));
            edit.setMinHeight(dp(68));
            edit.setContentDescription("Editar " + channel.name);
            edit.setOnClickListener(v -> showChannelDialog(index));
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(72), ViewGroup.LayoutParams.WRAP_CONTENT);
            editParams.setMargins(dp(8), 0, 0, 0);
            row.addView(edit, editParams);

            channelList.addView(row);
        }

        if (channels.isEmpty()) {
            TextView empty = text("No hay canales. Tocá “Agregar canal” para cargar uno.", 16, Color.LTGRAY);
            empty.setPadding(0, dp(24), 0, 0);
            channelList.addView(empty);
        }
    }

    private void playChannel(Channel channel) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("name", channel.name);
        intent.putExtra("url", channel.url);
        startActivity(intent);
    }

    private void showChannelDialog(int index) {
        boolean editing = index >= 0;
        Channel current = editing ? channels.get(index) : null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(8), dp(24), 0);

        EditText name = new EditText(this);
        name.setHint("Nombre del canal");
        name.setSingleLine(true);
        if (current != null) name.setText(current.name);
        form.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText url = new EditText(this);
        url.setHint("https://…/playlist.m3u8");
        url.setSingleLine(false);
        url.setMinLines(2);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        if (current != null) url.setText(current.url);
        form.addView(url, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Editar canal" : "Agregar canal")
                .setView(form)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        if (editing) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Eliminar", (d, which) -> {});
        }

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String n = name.getText().toString().trim();
                String u = url.getText().toString().trim();
                if (n.isEmpty()) {
                    name.setError("Ingresá un nombre");
                    return;
                }
                if (!(u.startsWith("http://") || u.startsWith("https://"))) {
                    url.setError("El enlace debe empezar con http:// o https://");
                    return;
                }
                if (editing) channels.set(index, new Channel(n, u));
                else channels.add(new Channel(n, u));
                saveChannels();
                renderChannels();
                dialog.dismiss();
            });

            if (editing) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setTitle("Eliminar canal")
                        .setMessage("¿Eliminar “" + current.name + "”?")
                        .setPositiveButton("Eliminar", (confirm, which) -> {
                            channels.remove(index);
                            saveChannels();
                            renderChannels();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show());
            }
        });

        dialog.show();
    }

    private void loadChannels() {
        channels.clear();
        String raw = prefs.getString(KEY_CHANNELS, null);
        if (raw == null) {
            channels.add(new Channel("Telefe", "http://45.173.231.22:8000/play/a053/index.m3u8"));
            channels.add(new Channel("El Trece", "http://190.108.83.69:8000/play/a01d/index.m3u8"));
            channels.add(new Channel("TyC Sports", "https://amg26268-amg26268c14-freelivesports-emea-10267.playouts.now.amagi.tv/ts-us-e2-n2/playlist/amg26268-sportsstudio-tycsports-freelivesportsemea/playlist.m3u8"));
            saveChannels();
            return;
        }

        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                channels.add(new Channel(obj.getString("name"), obj.getString("url")));
            }
        } catch (Exception e) {
            Toast.makeText(this, "No se pudieron leer los canales guardados", Toast.LENGTH_LONG).show();
        }
    }

    private void saveChannels() {
        JSONArray arr = new JSONArray();
        try {
            for (Channel c : channels) {
                JSONObject obj = new JSONObject();
                obj.put("name", c.name);
                obj.put("url", c.url);
                arr.put(obj);
            }
            prefs.edit().putString(KEY_CHANNELS, arr.toString()).apply();
        } catch (Exception ignored) { }
    }
}
