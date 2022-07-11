package com.viniciusbozzi.project.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.nikartm.button.FitButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.helper.GraficoDAO;
import com.viniciusbozzi.project.model.Grafico;
import com.viniciusbozzi.project.model.XYValue;

import java.util.ArrayList;
import java.util.Arrays;

/**
    DESENVOLVIDO POR VINICIUS BOZZI
 */

public class EditarTorraActivity extends AppCompatActivity {

    private static final String TAG = "EditarTorraActivity";
    private LineGraphSeries<DataPoint> xySeries;
    private FitButton btnAddPt;
    private EditText mX, mY;
    private Grafico grafico;
    private GraphView graphView;
    private ArrayList<XYValue> xyValueArray;
    private Switch tipoAcesso;
    String macDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_editar_torra);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Editar Torra");
        setSupportActionBar(toolbar);

        btnAddPt = findViewById(R.id.btnAddPtEdit);
        mX = findViewById(R.id.numXEdit);
        mY = findViewById(R.id.numYEdit);
        graphView = findViewById(R.id.scatterPlotEdit);

        grafico = (Grafico) getIntent().getSerializableExtra("graficoEditado");
        macDispositivo = (String) getIntent().getSerializableExtra("macDispositivo");

        Log.d("TAG20", "onCreate: " + grafico.getIdFirebase());

        if (grafico != null) {
            toolbar.setTitle("EDITAR " + grafico.getNomeGrafico().toUpperCase());

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
            iniciar();
        } else {
            finish();
        }

        tipoAcesso = findViewById(R.id.switchAcessoGlobal2);
        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    tipoAcesso.setText("Público");
                }else{
                    tipoAcesso.setText("Privado");
                }
            }
        });

    }

    private void iniciar() {

        xySeries = new LineGraphSeries<>();
        btnAddPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mX.getText().toString().equals("") && !mY.getText().toString().equals("")) {
                    double x = Double.parseDouble(mX.getText().toString());
                    double y = Double.parseDouble(mY.getText().toString());
                    boolean b = true;
                    for(XYValue value: xyValueArray){
                        if(value.getX() == x){
                            b = false;
                            break;
                        }
                    }
                    if(y>290){
                        Toast.makeText(EditarTorraActivity.this, "Favor digitar uma Temperatura abaixo de 290ºC!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(x>15){
                        Toast.makeText(EditarTorraActivity.this, "Favor digitar uma Tempo abaixo de 15 minutos!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(b){
                        xyValueArray.add(new XYValue(x,y));
                        iniciar();
                        mX.setText("");
                        mY.setText("");
                    }else{
                        Toast.makeText(EditarTorraActivity.this, "Impossivel duas temperaturas no mesmo instante de Tempo!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditarTorraActivity.this, "Preencha Os campos!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (xyValueArray.size() != 0) {
            criarGrafico();
        } else {
        }
    }

    private void criarGrafico() {

        xyValueArray = ordenarGrafico(xyValueArray);

        for (int i = 0; i < xyValueArray.size(); i++) {
            try {
                xySeries.resetData(generateData());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "msgERROR " + e.getMessage());
            }
        }
        xySeries.setColor(getResources().getColor(R.color.corCapuccino));

        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.setCursorMode(true);
        xySeries.setDrawBackground(true);
        xySeries.setDrawDataPoints(true);
        xySeries.setDataPointsRadius(10);
        xySeries.setBackgroundColor(getResources().getColor(R.color.fundoGrafico));
        xySeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, final DataPointInterface dataPoint) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditarTorraActivity.this);
                builder.setTitle("Deseja Remover esse Ponto ? \n Tempo=" + dataPoint.getX() + " , Temperatura=" + dataPoint.getY());
                builder.setCancelable(false);
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (XYValue value : xyValueArray) {
                            if (value.getX() == dataPoint.getX()) {
                                xyValueArray.remove(value);
                                break;
                            }
                        }
                        iniciar();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxY(350);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(18);
        graphView.getViewport().setMinX(0);

        graphView.addSeries(xySeries);
    }

    private DataPoint[] generateData() {
        xyValueArray = ordenarGrafico(xyValueArray);
        graphView.removeAllSeries();

        int count = xyValueArray.size();
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = xyValueArray.get(i).getX();
            double y = xyValueArray.get(i).getY();
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
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
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                break;
            }
        }
        return array;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editar_grafico, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.itemEditar:

                GraficoDAO graficoDAO = new GraficoDAO(getApplicationContext());

                Grafico g = new Grafico();
                g.setId(grafico.getId());
                g.setNomeGrafico(grafico.getNomeGrafico());
                g.setTempo(xyValueArray.get(xyValueArray.size() - 1).getX());
                g.setIdFirebase(grafico.getIdFirebase());

                if(xyValueArray.get(0).getX() != 0){
                    Toast.makeText(getApplicationContext(),"Favor inserir a Temperatura no Tempo 0!",Toast.LENGTH_SHORT).show();
                    break;
                }

                double max = 0.0;
                for (int i = 0; i < xyValueArray.size(); i++) {
                    if (xyValueArray.get(i).getY() > max){
                        max = xyValueArray.get(i).getY();
                    }
                }
                g.setTemperatura(max);
                g.setValoresXY(xyValueArray);

                int i = 0;
                StringBuilder dados;
                dados = new StringBuilder();
                dados.delete(0, dados.length());
                for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
                    dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
                    Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
                }
                dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60);
                grafico.setValorXY(dados.toString());

                if (graficoDAO.atualizar(g, tipoAcesso.isChecked())) { //passar o status do switch
                    Intent intent = new Intent(EditarTorraActivity.this, TorraSelecionadaActivity.class);
                    intent.putExtra("graficoSelecionado",grafico);
                    intent.putExtra("macDispositivo",macDispositivo);
                    startActivity( intent );
                    finish();
                    Toast.makeText(getApplicationContext(), "Sucesso ao salvar Torra!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao salvar Torra!", Toast.LENGTH_SHORT).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
