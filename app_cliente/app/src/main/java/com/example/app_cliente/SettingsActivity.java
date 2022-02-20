package com.example.app_cliente;

import android.content.Intent;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import org.json.JSONException;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private EditText textKV;
    private EditText textKI;

    private AppDatabase db;
    private ConstantesDao constantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textKV = (EditText) findViewById(R.id.text_kv);
        textKV.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Constantes newkCalV = new Constantes();

                newkCalV.uid = 1;
                newkCalV.constante = "tension";
                newkCalV.valor = Float.parseFloat( textKV.getText().toString() );

                constantes.insertValues(newkCalV);

                if (!hasFocus) {
                    {
                        // Validate youredittext
                    }
                }
            }
        });

        textKI = (EditText) findViewById(R.id.text_ki);
        textKI.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Constantes newkCalI = new Constantes();

                newkCalI.uid = 2;
                newkCalI.constante = "corriente";
                newkCalI.valor = Float.parseFloat( textKI.getText().toString() );

                constantes.insertValues(newkCalI);

                if (!hasFocus) {
                    {
                        // Validate youredittext
                    }
                }
            }
        });

        db = Room.databaseBuilder(this,
                AppDatabase.class, "constantes").allowMainThreadQueries().build();

        constantes = db.constantesDao();
        List listaCte = constantes.getAll();

        textKV.setText( ((Constantes) listaCte.get(0)).valor.toString() );
        textKI.setText( ((Constantes) listaCte.get(1)).valor.toString() );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item2:
                Intent intent_main = new Intent(SettingsActivity.this, MainActivity.class);
                // start the activity connect to the specified class
                startActivity(intent_main);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getValues() {
        List<Constantes> listaConstantes = constantes.getAll();
    }
}
