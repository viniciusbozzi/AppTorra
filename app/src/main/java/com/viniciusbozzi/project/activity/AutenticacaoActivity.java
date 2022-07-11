package com.viniciusbozzi.project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;
import com.viniciusbozzi.project.helper.UsuarioFirebase;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;//tipoUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);


        inicializaComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Verificar usuario logado
        verificarUsuarioLogado();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {

                        //Verifica estado do switch
                        if (tipoAcesso.isChecked()) {//Cadastro

                            autenticacao.createUserWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Cadastro realizado com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        String erroExcecao = "";
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            erroExcecao = "Digite uma senha mais forte!";
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            erroExcecao = "Por favor, digite um e-mail válido";
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            erroExcecao = "Este conta já foi cadastrada";
                                        } catch (Exception e) {
                                            erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Erro: " + erroExcecao,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        } else {//Login

                            autenticacao.signInWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Logado com sucesso",
                                                Toast.LENGTH_SHORT).show();
                                        String tipoUsuario = task.getResult().getUser().getDisplayName();
                                        abrirTelaPrincipal(tipoUsuario);

                                    } else {
                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Erro ao fazer login : " + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                    } else {
                        Toast.makeText(AutenticacaoActivity.this,
                                "Preencha a senha!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AutenticacaoActivity.this,
                            "Preencha o E-mail!",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void verificarUsuarioLogado() {
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        Log.d("usuario", "verificarUsuarioLogado: "+ usuarioAtual);
        if (usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }


    private void abrirTelaPrincipal(String tipoUsuario) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private void inicializaComponentes() {
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
    }
}