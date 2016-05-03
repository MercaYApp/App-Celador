package cosw.mercayappcelador;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Felipe Brasil on 1/5/2016.
 */
public class MainActivity extends ActionBarActivity {
    private String user, password;
    private EditText campoUser, campoPassword;
    private Button btnIngresar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void inicializarCampos(){
        campoUser = (EditText)findViewById(R.id.campoUser);
        campoPassword = (EditText)findViewById(R.id.campoPassword);
        btnIngresar = (Button)findViewById(R.id.btnIngresar);

        user = campoUser.getText().toString();
        password = campoPassword.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void ingresar(View v){
        inicializarCampos();
        mensaje("Usuario: "+user+" \nPassword: "+password);
        Intent intent = new Intent(this, GetProductsActivity.class);
        startActivity(intent);
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
}
