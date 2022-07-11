package com.viniciusbozzi.project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.model.Grafico;

import java.util.List;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public class GraficoAdapter extends RecyclerView.Adapter<GraficoAdapter.MyViewHolder> {

    private List<Grafico> graficoList;

    public GraficoAdapter(List<Grafico> lista ) {
        this.graficoList = lista;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_grafico_adapter, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Grafico g = graficoList.get(position);
        holder.grafico.setText(g.getNomeGrafico());

        double segundos;
        double minutos;
//        int horas;
        segundos= g.getTempo()*60;
//        Log.d("tag3",segundos+"");
        double S = segundos%60;
//        Log.d("tag3",S+"");
        minutos = segundos/60;
        double M = minutos%60;
//        horas = minutos/60;

        String cvrt = String.format("%01d:%02d",(int)M,(int)S);

        holder.tempoGrafico.setText(cvrt);
        holder.temperaturaGrafico.setText(g.getTemperatura()+"ºC");

    }

    @Override
    public int getItemCount() {
        return this.graficoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView grafico;
        TextView tempoGrafico;
        TextView temperaturaGrafico;

        public MyViewHolder(View itemView) {
            super(itemView);

            grafico = itemView.findViewById(R.id.textNomeGrafico);
            tempoGrafico = itemView.findViewById(R.id.textTempoGrafico);
            temperaturaGrafico = itemView.findViewById(R.id.textTemperaturaGrafico);
        }
    }

}
