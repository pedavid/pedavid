package com.example.app_cliente;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;         //Es una cola donde van los request que voy haciendo (libreria volley)

    private Operations operations = new Operations();

    private TextView textVef;
    private TextView textIef;
    private TextView textPot;

    private int[] arrayTension;      //Aca deberiamos usar number o float
    private int[] arrayCorriente;

    private AppDatabase db;

    private ConstantesDao constantes;

    float cteV;
    float cteI;

    //-----------------Interfaz-----------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);   //inicializo la queue

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                obtenerDatosVolley();
            }
        }, 0, 300);     //put here time 1000 milliseconds = 1 second

        db = Room.databaseBuilder(this,
                AppDatabase.class, "constantes").allowMainThreadQueries().build();

        constantes = db.constantesDao();

        if( constantes.isNotEmpty() == false ){
            Constantes kCalV = new Constantes();
            Constantes kCalI = new Constantes();

            kCalV.constante = "tension";
            kCalI.constante = "corriente";
            kCalV.valor = 1f;
            kCalI.valor = 1f;


            constantes.insertValues(kCalV);
            constantes.insertValues(kCalI);
        }
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
                Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);

                // start the activity connect to the specified class
                startActivity(intent_settings);
                return true;
            case R.id.item2:
                Toast.makeText(this, "Inicio selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void obtenerDatosVolley(){   //Nuevo metodo con logica para obtener json
        String url = "http://192.168.1.239";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(JSONObject response) {
                int i = 0;

                try {
                    JSONArray myJsonArray = response.getJSONArray("data");
                    int jsonLength = myJsonArray.length();
                    arrayCorriente = new int[jsonLength];
                    arrayTension = new int[jsonLength];

                    for( i = 0; i < myJsonArray.length(); i++ ) {
                        JSONObject myJsonObject = myJsonArray.getJSONObject(i);
                        int tension = myJsonObject.getInt("tension");
                        int corriente = myJsonObject.getInt("corriente");   // Aca deberiamos usar number o float

                        arrayTension[i] = tension;
                        arrayCorriente[i] = corriente;
                    }
                    actualizarDisplay();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                i=0;
            }
        },
            new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Mensaje", String.valueOf(error));
            }
        });
        queue.add(request); // Esto es del volley

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void actualizarDisplay() {

        mathematicalOperations();

        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                TextView textPot = (TextView) findViewById(R.id.displayPot);
                TextView textVef = (TextView) findViewById(R.id.displayVef);
                TextView textIef = (TextView) findViewById(R.id.displayIef);
                TextView textPwdFact = (TextView) findViewById(R.id.displayPwdFactor);
                double aux;
                aux = ( operations.activePowerValue(arrayTension, arrayCorriente) );
                String aux2 = String.format("%.2f", aux);
                textPot.setText( aux2 + " W");
                aux = ( operations.getVoltageRmsValue() );
                aux2 = String.format("%.2f", aux);
                textVef.setText( aux2 + " V" );
                aux = ( operations.getCurrentRmsValue() );
                aux2 = String.format("%.2f", aux);
                textIef.setText( aux2 + " A" );
                aux = ( operations.powerFactorValue() );
                aux2 = String.format("%.2f", aux);
                textPwdFact.setText( aux2 );
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mathematicalOperations(){
        double vefTension = operations.rmsValue(arrayTension);
        double vefCorriente = operations.rmsValue(arrayCorriente);

        List listaCte = constantes.getAll();

        cteV = ((Constantes) listaCte.get(0)).valor;
        cteI = ((Constantes) listaCte.get(1)).valor;

        operations.setCurrentMeanValue( operations.meanValue(arrayCorriente) );
        operations.setCurrentRmsValue(  operations.movingAvergeFilterCur(vefCorriente) * cteI );
        operations.setVoltageMeanValue( operations.meanValue(arrayTension) );
        operations.setVoltageRmsValue(  operations.movingAvergeFilterVol(vefTension) * cteV );
        Log.d("Tension rms recibida", "mathematicalOperations: "+ vefTension);

        Log.d("Tension eficaz", "" + operations.getVoltageRmsValue());
        Log.d("Potencia aparente",  operations.apparentPowerValue() +"VA");
        Log.d("Potencia aparente",  operations.activePowerValue(arrayTension, arrayCorriente) +"W");
        Log.d("Factor de Potencia", "" + operations.powerFactorValue());

       // Log.d("Potencia aparente", operations.apparentPower(bufferTension.asInt(), bufferCorriente.asInt()) +"VA");
    }
}