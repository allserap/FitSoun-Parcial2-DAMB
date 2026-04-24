package com.fit.sound;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;


//Implementación de OnMapReadyCallBack para manejar GoogleMaps
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int cardioMin = 0;
    private int fuerzaMin = 0;
    private int flexibilidadMin = 0;
    private List<Integer> histCardio = new ArrayList<>();
    private List<Integer> histFuerza = new ArrayList<>();
    private List<Integer> histFlex = new ArrayList<>();

    private EditText etMinutos;
    private Spinner spinnerCharts;
    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChart;
    private final int COLOR_CARDIO = Color.parseColor("#A2D2FF");
    private final int COLOR_FUERZA = Color.parseColor("#D8BFD8");
    private final int COLOR_FLEX   = Color.parseColor("#FF6B6B");


    //Declaración de objeto de API de Google Maps (HU4)
    private GoogleMap mapa;

    //Declaración de objeto MediaPlayer(HU3/6)
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Busqueda del fragmento definido en el XML (HU4)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Carga del mapa en segundo plano
         mapFragment.getMapAsync(this);


        etMinutos = findViewById(R.id.etMinutos);
        spinnerCharts = findViewById(R.id.spinnerCharts);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);

        String[] opciones = {"Gráfico de Barras", "Gráfico de Pastel", "Gráfico de Líneas"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCharts.setAdapter(adapter);

        //HU5
        if (savedInstanceState != null) {
            cardioMin = savedInstanceState.getInt("cardio");
            fuerzaMin = savedInstanceState.getInt("fuerza");
            flexibilidadMin = savedInstanceState.getInt("flex");
            int savedPos = savedInstanceState.getInt("spinnerPos");
            spinnerCharts.setSelection(savedPos);
        }

        configurarBotones();
        configurarSpinner();
        actualizarGraficos();


        //HU3
        // Inicializar MediaPlayer con el audio de res/raw/success.mp3
         mediaPlayer = MediaPlayer.create(this, R.raw.success);
         if (mediaPlayer == null) {
            Toast.makeText(this, "Error: no se pudo cargar el audio", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnCardio).setEnabled(false);
            findViewById(R.id.btnFlexibilidad).setEnabled(false);
            findViewById(R.id.btnFuerza).setEnabled(false);
            return;
         }
        // Configurar listener para cuando termina la reproducción
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.seekTo(0);
         });

    }

    private void actualizarGraficos() {
        setupBarChart();
        setupPieChart();
        setupLineChart();
    }

    private void setupLineChart() {
        List<Entry> entriesCardio = new ArrayList<>();
        List<Entry> entriesFuerza = new ArrayList<>();
        List<Entry> entriesFlex = new ArrayList<>();

        for (int i = 0; i < histCardio.size(); i++) {
            entriesCardio.add(new Entry(i, histCardio.get(i)));
            entriesFuerza.add(new Entry(i, histFuerza.get(i)));
            entriesFlex.add(new Entry(i, histFlex.get(i)));
        }

        LineDataSet setCardio = new LineDataSet(entriesCardio, "Cardio");
        configurarEstiloLinea(setCardio, COLOR_CARDIO);
        LineDataSet setFuerza = new LineDataSet(entriesFuerza, "Fuerza");
        configurarEstiloLinea(setFuerza, COLOR_FUERZA);
        LineDataSet setFlex = new LineDataSet(entriesFlex, "Flex");
        configurarEstiloLinea(setFlex, COLOR_FLEX);

        LineData lineData = new LineData(setCardio, setFuerza, setFlex);
        lineChart.setData(lineData);

        // Eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void configurarEstiloLinea(LineDataSet set, int color) {
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(3f);
        set.setCircleRadius(4f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(cardioMin, "Cardio"));
        entries.add(new PieEntry(fuerzaMin, "Fuerza"));
        entries.add(new PieEntry(flexibilidadMin, "Flex"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(COLOR_CARDIO, COLOR_FUERZA, COLOR_FLEX);
        dataSet.setValueTextSize(12f);

        pieChart.setData(new PieData(dataSet));
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, cardioMin));
        entries.add(new BarEntry(1, fuerzaMin));
        entries.add(new BarEntry(2, flexibilidadMin));

        BarDataSet dataSet = new BarDataSet(entries, "Minutos");
        dataSet.setColors(COLOR_CARDIO, COLOR_FUERZA, COLOR_FLEX);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        // Eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Cardio", "Fuerza", "Flex"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(2f);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    //HU2
    private void configurarSpinner() {
        spinnerCharts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                barChart.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                pieChart.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                lineChart.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //HU1
    private void configurarBotones() {
        View.OnClickListener listener = v -> {
            String input = etMinutos.getText().toString();
            if (input.isEmpty() || Integer.parseInt(input) <= 0) {
                Toast.makeText(this, "Ingrese minutos válidos", Toast.LENGTH_SHORT).show();
                return;
            }

            int valor = Integer.parseInt(input);
            int id = v.getId();

            if (id == R.id.btnCardio) {
                cardioMin += valor;
            } else if (id == R.id.btnFuerza) {
                fuerzaMin += valor;
            } else if (id == R.id.btnFlexibilidad) {
                flexibilidadMin += valor;
            }

            histCardio.add(cardioMin);
            histFuerza.add(fuerzaMin);
            histFlex.add(flexibilidadMin);

            // HU2
            // Limitar historial a los últimos 10
            if (histCardio.size() > 10) {
                histCardio.remove(0);
                histFuerza.remove(0);
                histFlex.remove(0);
            }

            etMinutos.setText("");
            actualizarGraficos();

            reproducirSonido(); //HU3
        };

        findViewById(R.id.btnCardio).setOnClickListener(listener);
        findViewById(R.id.btnFuerza).setOnClickListener(listener);
        findViewById(R.id.btnFlexibilidad).setOnClickListener(listener);
    }

    //HU5
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("cardio", cardioMin);
        outState.putInt("fuerza", fuerzaMin);
        outState.putInt("flex", flexibilidadMin);
        outState.putInt("spinnerPos", spinnerCharts.getSelectedItemPosition());
    }

    //HU4
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

         //Controles de zoom
         mapa.getUiSettings().setZoomControlsEnabled(true);

         //Iniciar en Plaza Salvador del Mundo con coordenadas
         LatLng salvadorDelMundo = new LatLng(13.70131880212184, -89.2243357518757);

         //Centrar cámara con zoom 12 en
         mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(salvadorDelMundo, 12f));

         //Marcador en Parque Bicentenario
         mapa.addMarker(new MarkerOptions().position(salvadorDelMundo).title("Salvador del Mundo"));
    }

    //HU3/6
    //Ejecuta el sonido de éxito
    private void reproducirSonido() {
         if (mediaPlayer != null) {
             if (mediaPlayer.isPlaying()) {
             mediaPlayer.seekTo(0);
             }
            mediaPlayer.start();
         }
    }

    //Ejecución cuando se cierra la activity
    @Override
    protected void onDestroy() {
         super.onDestroy();
         if (mediaPlayer != null) {
             if (mediaPlayer.isPlaying()) {
             mediaPlayer.stop();
             }
             mediaPlayer.release();
             mediaPlayer = null;
         }
    }

}
