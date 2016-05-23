package cosw.mercayappcelador;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Felipe Gomez on 30/4/2016.
 */
public class GetProductsActivity extends ActionBarActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;
    private TableLayout tl;
    private JSONArray ja = null;
    private JSONObject jo = null;
    private Button scanBtn;
    private double totalVenta, totalPeso = 0;
    private String idFactura = "";
    private boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_products);
        inicializarCampos();
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void inicializarCampos(){
        //Se Instancia el botón de Scan
        scanBtn = (Button)findViewById(R.id.scanButton);
    }

    /**
     * Crear estructura de la tabla
     * @throws JSONException
     */
    private void crearTabla() throws JSONException {
        totalVenta=0;
        totalPeso = 0;
        TableLayout t1 = null;
        tl = (TableLayout) findViewById(R.id.main_table);
        tl.removeViews(1, tl.getChildCount()-1); //Eliminar anterior consulta de factura
        poblarDatosFactura();
        TableRow tr_head = new TableRow(this);
        tr_head.setId(new Integer(0));
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_nombre = new TextView(this);
        label_nombre.setId(new Integer(1));// define id that must be unique
        label_nombre.setText("NOMBRE"); // set the text for the header
        label_nombre.setTextColor(Color.WHITE); // set the color
        label_nombre.setTypeface(null, Typeface.BOLD);
        label_nombre.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_nombre); // add the column to the table row here

        TextView label_precio = new TextView(this);
        label_precio.setId(new Integer(2));// define id that must be unique
        label_precio.setText("PRECIO"); // set the text for the header
        label_precio.setTextColor(Color.WHITE); // set the color
        label_precio.setTypeface(null, Typeface.BOLD);
        label_precio.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_precio); // add the column to the table row here

        TextView label_peso = new TextView(this);
        label_peso.setId(new Integer(3));
        label_peso.setText("PESO");
        label_peso.setTextColor(Color.WHITE);
        label_peso.setTypeface(null, Typeface.BOLD);
        label_peso.setPadding(5, 5, 5, 5);
        tr_head.addView(label_peso);// add the column to the table row here

        tl.addView(tr_head, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Listar los productos asociados a la factura
     */
    private void poblarDatosTotal(){
        TableRow tr_head = new TableRow(this);
        tr_head.setId(new Integer(4));
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_void = new TextView(this);
        label_void.setId(new Integer(5));
        label_void.setText("TOTAL");
        label_void.setTextColor(Color.WHITE);
        label_void.setTypeface(null, Typeface.BOLD);
        label_void.setPadding(5, 5, 5, 5);
        tr_head.addView(label_void);// add the column to the table row here

        TextView label_venta = new TextView(this);
        label_venta.setId(new Integer(6));// define id that must be unique
        label_venta.setText(totalVenta+""); // set the text for the header
        label_venta.setTextColor(Color.WHITE); // set the color
        label_venta.setTypeface(null, Typeface.BOLD);
        label_venta.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_venta); // add the column to the table row here

        TextView label_peso = new TextView(this);
        label_peso.setId(new Integer(7));// define id that must be unique
        label_peso.setText(totalPeso+""); // set the text for the header
        label_peso.setTextColor(Color.WHITE); // set the color
        label_peso.setTypeface(null, Typeface.BOLD);
        label_peso.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_peso); // add the column to the table row here

        tl.addView(tr_head, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    /**
     * Agrega los datos de la factura al informe
     * @throws JSONException
     */
    private void poblarDatosFactura() throws JSONException {
        TableRow tr1 = new TableRow(this);
        tr1.setId(new Integer(8));

        tr1.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        //Agregar numero ID de factura
        TextView labelDatosFactura = new TextView(this);
        labelDatosFactura.setId(new Integer(9));
        labelDatosFactura.setText("DATOS DE FACTURA: ");
        labelDatosFactura.setTextColor(Color.parseColor("#40BBCB"));
        labelDatosFactura.setTypeface(null, Typeface.BOLD);
        labelDatosFactura.setPadding(2, 0, 5, 0);
        tr1.addView(labelDatosFactura);
        //Agregar numero ID de factura
        TextView labelIdFactura = new TextView(this);
        labelIdFactura.setId(new Integer(10));
        labelIdFactura.setText(jo.getString("idInvoices"));
        labelIdFactura.setTextColor(Color.parseColor("#40BBCB"));
        labelIdFactura.setTypeface(null, Typeface.BOLD);
        labelIdFactura.setPadding(2, 0, 5, 0);
        tr1.addView(labelIdFactura);
        //Agregar fecha de factura
        TextView labelFechaFactura = new TextView(this);
        labelFechaFactura.setId(new Integer(11));
        labelFechaFactura.setText(jo.getString("dateInvoice"));
        labelFechaFactura.setTextColor(Color.parseColor("#40BBCB"));
        labelFechaFactura.setTypeface(null, Typeface.BOLD);
        labelFechaFactura.setPadding(2, 0, 5, 0);
        tr1.addView(labelFechaFactura);

        tl.addView(tr1, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }

    private void poblarTabla() throws JSONException {

        for (int i= 0; i<ja.length(); i++) {
            //Crear el objeto json extraido
            JSONObject jo = (JSONObject)ja.getJSONObject(i);
            String id = jo.getString("idProductos");
            String nombre = jo.getString("nameProduct");
            int precio = jo.getInt("buyPrice");
            int peso = jo.getInt("weight");
            totalPeso+=peso;
            double porcentaje = jo.getDouble("percentage")/100;
            double precioVenta = precio*(1+porcentaje);
            totalVenta+=precioVenta;
            // Create the table row
            TableRow tr = new TableRow(this);
            if(i%2!=0) tr.setBackgroundColor(Color.parseColor("#40BBCB"));
            tr.setId(new Integer(200+i));

            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView labelNombre = new TextView(this);
            labelNombre.setId(new Integer(300+i));
            labelNombre.setText(nombre);
            tr.addView(labelNombre);

            TextView labelPrecio = new TextView(this);
            labelPrecio.setId(new Integer(400+i));
            labelPrecio.setText(precioVenta+"");
            tr.addView(labelPrecio);

            TextView labelId = new TextView(this);
            labelId.setId(new Integer(500+i));
            labelId.setText(peso+"");
            labelId.setPadding(2, 0, 5, 0);
            tr.addView(labelId);

            // finally add this to the table row
            tl.addView(tr, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetProducts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cosw.mercayappcelador/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetProducts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cosw.mercayappcelador/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }

    public void onClick(View v){
        //Se responde al evento click
        if(v.getId()==R.id.scanButton){
            //Se instancia un objeto de la clase IntentIntegrator
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            //Se procede con el proceso de scaneo
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Se obtiene el resultado del proceso de scaneo y se parsea
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //Quiere decir que se obtuvo resultado pro lo tanto:
            //Desplegamos en pantalla el contenido del código de barra scaneado
            String scanContent = scanningResult.getContents();
            idFactura = scanContent;
            try {
                buscarFactura(idFactura);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            //Quiere decir que NO se obtuvo resultado
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Buscar en el API la factura con el id escaneado del código de barras
     * @param id
     * @throws JSONException
     */
    public void buscarFactura(String id) throws JSONException {
        tl = null;
        GetFacturaAsync factura = new GetFacturaAsync();
        String url = "http://mercayapp1.herokuapp.com/invoices/"+id;
        factura.execute(url);
        if(error==true){
            mensaje("Error en la autenticación");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }



    /**
     * Hace get del producto en el API
     */
    private class GetFacturaAsync extends AsyncTask<String, Integer, JSONArray> {
        protected JSONArray doInBackground(String... url) {
            StringBuilder builder = new StringBuilder();

            try {
                Intent intent = getIntent();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url[0]);
                httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(intent.getStringExtra("user"), intent.getStringExtra("password")), "UTF-8", false));
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
                ja = jo.getJSONArray("productses");
            } catch (Exception e){
                e.printStackTrace();
                error=true;
                Log.e(GetProductsActivity.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
            return ja;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONArray result) {
            try {
                if(jo!=null) {
                    crearTabla();
                    poblarTabla();
                    poblarDatosTotal();
                    mensaje("Ya se cargaron todos los productos de la factura!");
                }else {
                    tl.removeViews(1, tl.getChildCount()-1); //Eliminar anterior consulta de factura
                    mensaje("NO SE HA ENCONTRADO FACTURA");
                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    v.vibrate(3000);
                }
                ja=null;
            } catch (Exception e) {
                mensaje("NO SE HA ENCONTRADO FACTURA");
                e.printStackTrace();
            }
        }
    }


    private void mensaje(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

}
