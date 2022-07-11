package com.viniciusbozzi.project.helper;
import com.viniciusbozzi.project.model.Grafico;
import org.json.JSONException;
import java.util.List;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public interface IGraficoDAO {

    public boolean salvar(Grafico grafico, boolean check_global) throws JSONException;
    public boolean atualizar(Grafico grafico, boolean check_global);
    public boolean deletar(Grafico grafico);
    public List<Grafico> listar();

}
