package cosw.mercayappcelador;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Felipe Brasil on 1/5/2016.
 */
public class MainActivity extends ActionBarActivity {
    private String user, password;
    private EditText campoUser, campoPassword;
    private Button btnIngresar;
    private boolean error = false;
    private JSONObject jo = null;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarCampos();
    }

    private void inicializarCampos(){
        campoUser = (EditText)findViewById(R.id.campoUser);
        campoPassword = (EditText)findViewById(R.id.campoPassword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void ingresar(View v){
        user = campoUser.getText().toString();
        password = campoPassword.getText().toString();
        mensaje("Usuario: "+user+" \nPassword: "+password);
        try {
            buscarCliente(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*Intent intent = new Intent(this, GetProductsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);*/
    }

    private void mensaje(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.menuEscanear) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }*/
        if (id == R.id.escanearFactura) {
            Intent intent = new Intent(this, GetProductsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.inicio) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * carga los datos del cliente que esté intentando logearse
     * @param id
     * @throws JSONException
     */
    public void buscarCliente(String id) throws JSONException {
        //showProgress(true);
        GetClienteAsync cli = new GetClienteAsync();
        String url = "http://mercayapp1.herokuapp.com/clientsApp/"+id;
        cli.execute(url);
        /*if(error==true){
            mensaje("Error en la autenticación");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }*/
    }


    /**
     * asigna los datos al cliente (singletone )que está logeado
     */
    public void agregarDatosAlCliente(){
        try {
            String s= jo.getString("nameClientApp");
        } catch (JSONException e) {
            error = true;
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            Log.e(MainActivity.class.toString(),
                    "Login request failed " + e.getLocalizedMessage());
        }
    }

    /**
     * Hace get del producto en el API
     */
    private class GetClienteAsync extends AsyncTask<String, Integer, JSONObject> {
        protected JSONObject doInBackground(String... url) {
            StringBuilder builder = new StringBuilder();

            try {
                Intent intent = getIntent();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url[0]);
                httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), "UTF-8", false));
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(content));
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                jo = new JSONObject(builder.toString());
//                ja = jo.getJSONArray("productses");
            } catch (Exception e){
                error = true;
                e.printStackTrace();
                Log.e(MainActivity.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
//            return ja;
            return jo;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONObject result) {
            agregarDatosAlCliente();
            if (error == true) {
                mensaje("Error en la autenticación");
            } else {
                Intent intent = new Intent(context, GetProductsActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("password", password);
                startActivity(intent);

                //showProgress(false);
                mensaje("Bienvenido!");
            }
        }
    }



}
