package cosw.mercayappcelador;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
    private View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarCampos();
    }

    private void inicializarCampos(){
        campoUser = (EditText)findViewById(R.id.campoUser);
        campoPassword = (EditText)findViewById(R.id.campoPassword);
        progress = findViewById(R.id.login_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void ingresar(View v){
        mensaje("Autenticando");
        user = campoUser.getText().toString();
        password = campoPassword.getText().toString();
        try {
            buscarCliente(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        showProgress(true);
        GetClienteAsync cli = new GetClienteAsync();
        String url = "http://mercayapp1.herokuapp.com/clientsApp/"+id;
        cli.execute(url);
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

                showProgress(false);
                mensaje("Bienvenido!");
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*
            viewReposterias.setVisibility(show ? View.GONE : View.VISIBLE);
            viewReposterias.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewReposterias.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
