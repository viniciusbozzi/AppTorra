package com.viniciusbozzi.project.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.nikartm.button.FitButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.model.Grafico;
import com.viniciusbozzi.project.model.XYValue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class TorraRealTime extends AppCompatActivity {

    private Grafico grafico;
    private LineGraphSeries<DataPoint> xySeries;
    private GraphView graphView;
    private ArrayList<XYValue> xyValueArray;
    private FitButton fitButtonExport;
    private FitButton fimTorra;

    private LineGraphSeries<DataPoint> xySeriesRealTime;
    private GraphView graphViewRealTime;
    private ArrayList<XYValue> xyValueArrayRealTime;
    private StringBuilder dados;
    private ArrayList<XYValue> torraFinal;

    private static double graph2LastXValue = 0.0;

    private static TextView section_label;
    private static TextView tempoReal;
    private static TextView temperaturaReal;
    private static long initialTime;
    private static Handler handlerTempo;
    private static boolean isRunning;
    private static boolean flag;
    private static final long MILLIS_IN_SEC = 1000L;
    private static final int SECS_IN_MIN = 60;
    private static double temporeal = 0;
    private double ultimoX = 1200.0;
    private int flag2 =0;
    private double tempAnterior =0.0;
    private Double valueTempeRealFirebase;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef3;
    DatabaseReference myRef4;
    String macDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torra_real_time);
        Toolbar toolbar = findViewById(R.id.toolbar);
        graphView = findViewById(R.id.graficoSelectRealTime);
        graphViewRealTime = findViewById(R.id.graficoSelectRealTime2);
        tempoReal = findViewById(R.id.tempo_Real);
        temperaturaReal = findViewById(R.id.temp_Real);
        fitButtonExport = findViewById(R.id.buttonExportFinalTorra);
        fimTorra = findViewById(R.id.fimTorra);
        fitButtonExport.setVisibility(View.GONE);
        fimTorra.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        grafico = (Grafico) getIntent().getSerializableExtra("graficoTempoReal");
        macDispositivo = (String) getIntent().getSerializableExtra("macDispositivo");

        myRef3 = database.getReference("dispositivos/" + macDispositivo + "/temperatura");
        myRef4 = database.getReference("dispositivos/" + macDispositivo + "/torrando");

        fitButtonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularValoresCsv();

                try {
                    FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
                    out.write((dados.toString()).getBytes());
                    out.close();

                    Context context = getApplicationContext();
                    File file = new File(getFilesDir(), "data.csv");
                    Uri path = FileProvider.getUriForFile(context, "com.viniciusbozzi.project.fileprovider", file);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/csv");
                    intent.putExtra(Intent.EXTRA_SUBJECT, grafico.getNomeGrafico());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(intent, "Exportar .CSV"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        fimTorra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(TorraRealTime.this, MainActivity.class);
                    startActivity(intent);
                    isRunning = false;
                    xySeriesRealTime = null;
                    graph2LastXValue = 0;
                    temporeal = 0;
                    graphViewRealTime.removeAllSeries();
                    myRef4.setValue(false);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        if (grafico != null) {
            toolbar.setTitle(grafico.getNomeGrafico().toUpperCase());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            xyValueArrayRealTime = new ArrayList<>();
            xyValueArray = new ArrayList<>();
            Log.d("TAG", "onCreate: " + grafico.getValorXY());
            String graficosStr[] = grafico.getValorXY().split(",");
            //String graficosStr[] = Arrays.copyOfRange(op, 1, op.length); //retira elemento 0
            int i;
            int j= 0;
            for(i = 0; i < graficosStr.length; i=i+2){
                xyValueArray.add(new XYValue(Double.parseDouble(graficosStr[i+1])/60,Double.parseDouble(graficosStr[i])));
                j++;
            }

            xySeries = new LineGraphSeries<>();
            if (xyValueArray.size() != 0) {
                iniciarGrafico();
            } else {
            }
        }

        myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Double value = dataSnapshot.getValue(Double.class);
                Log.d("TAG", "Value is: " + value);
                temperaturaReal.setText(value+"ºC");
                valueTempeRealFirebase = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });


    }

    private  void plotaGraficoTempoReal() {

        Log.d("321", "\n" + temporeal + "\n" + valueTempeRealFirebase);
        if(temporeal >= ultimoX){
            Toast.makeText(TorraRealTime.this, "Fim da Torra!", Toast.LENGTH_LONG).show();
            fitButtonExport.setVisibility(View.VISIBLE);
            fimTorra.setVisibility(View.VISIBLE);
            torraFinal = new ArrayList(xyValueArrayRealTime);
            myRef4.setValue(false);
        }else{
            xyValueArrayRealTime.add(new XYValue(temporeal,valueTempeRealFirebase));
            xySeriesRealTime.appendData(new DataPoint(graph2LastXValue, valueTempeRealFirebase), true, 10000);
            graph2LastXValue = 0.01666666667*(temporeal+1);
            graphViewRealTime.getViewport().setMaxX(20);
            graphViewRealTime.getViewport().setMinX(0);
            graphViewRealTime.removeSeries(xySeriesRealTime);
            graphViewRealTime.addSeries(xySeriesRealTime);
        }
    }

    private void calcularValoresCsv() {

        int i,j;
        dados = new StringBuilder();
        dados.append("Tempo(s),Temperatura(C)");

        for (i=0; i < torraFinal.size() - 1; i++) {
            dados.append("\n"+ torraFinal.get(i).getX()+","+torraFinal.get(i).getY());
        }

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Voce deseja abandonar a Torra?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isRunning = false;
                        xySeriesRealTime = null;
                        graph2LastXValue = 0;
                        temporeal = 0;
                        graphViewRealTime.removeAllSeries();
                        myRef4.setValue(false);
                        //TorraRealTime.this.onSuperBackPressed();
                        finish();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

//    public void onSuperBackPressed(){
//        super.onBackPressed();
//    }

    private void iniciarGrafico() {

        xyValueArray = ordenarGrafico(xyValueArray);

        for (int i = 0; i < xyValueArray.size(); i++) {
            try {
                double x = xyValueArray.get(i).getX();
                double y = xyValueArray.get(i).getY();
                xySeries.appendData(new DataPoint(x, y), true, 1000);
            } catch (IllegalArgumentException e) {
                Log.e("TAG", "msgERROR " + e.getMessage());
            }
        }
        ultimoX = xyValueArray.get(xyValueArray.size() - 1).getX() * 60;


        handlerTempo = new Handler();
        isRunning = true;
        initialTime = System.currentTimeMillis();
        handlerTempo.postDelayed(runnable, MILLIS_IN_SEC);

        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.setCursorMode(true);
        xySeries.setColor(getResources().getColor(R.color.corCapuccino));
        xySeries.setDrawBackground(true);
        xySeries.setBackgroundColor(getResources().getColor(R.color.fundoGrafico));
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxY(350);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(20);
        graphView.getViewport().setMinX(0);

        graphView.addSeries(xySeries);

        xySeriesRealTime = new LineGraphSeries<>();
        graphViewRealTime.getViewport().setScalable(false);
        graphViewRealTime.getViewport().setScalableY(false);
        graphViewRealTime.getViewport().setScrollable(true);
        graphViewRealTime.getViewport().setScrollableY(true);
        graphViewRealTime.getViewport().setYAxisBoundsManual(true);
        graphViewRealTime.getViewport().setMaxY(350);
        graphViewRealTime.getViewport().setMinY(0);
        graphViewRealTime.getViewport().setXAxisBoundsManual(true);
        graphViewRealTime.getViewport().setMaxX(20);
        graphViewRealTime.getViewport().setMinX(0);
        xySeriesRealTime.setColor(getResources().getColor(R.color.torraRealTime));
        xySeriesRealTime.setDrawBackground(true);
        xySeriesRealTime.setBackgroundColor(getResources().getColor(R.color.torraRealTimeFundo));

    }

    private ArrayList<XYValue> ordenarGrafico(ArrayList<XYValue> array) {

        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(), 2))));
        int m = array.size() - 1;
        int count = 0;

        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            try {
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                }
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
        return array;
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long seconds = (System.currentTimeMillis() - initialTime) / MILLIS_IN_SEC;
                plotaGraficoTempoReal();
                if(temporeal < ultimoX){
                    tempoReal.setText(String.format("%02d:%02d", seconds / SECS_IN_MIN, seconds % SECS_IN_MIN));
                    temporeal = seconds;
                    handlerTempo.postDelayed(runnable, MILLIS_IN_SEC);
                }
            }
        }
    };
}
