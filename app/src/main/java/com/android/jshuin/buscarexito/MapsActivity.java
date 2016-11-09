package com.android.jshuin.buscarexito;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String URL = "http://192.168.1.5:8888/Buscameexito/consulta.php";
    ProgressDialog progressDialog;
    RequestQueue requestQueue;
    double latitud, longitud;
    String titulo;
    Spinner destinoSeleccionable, vehiculoSeleccionable;
    JSONArray puntosArray;
    List<String> spinnerDestinoArray;
    List<String> spinnerVehiculoArray;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        destinoSeleccionable = (Spinner)findViewById(R.id.destino);
        vehiculoSeleccionable = (Spinner)findViewById(R.id.destino);
        destinoSeleccionable.setPopupBackgroundResource(R.drawable.spinner);
        vehiculoSeleccionable.setPopupBackgroundResource(R.drawable.spinner);

        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        cargarpuntos();
    }

    private void cargarpuntos() {
        //progressDialog = new ProgressDialog(getApplicationContext());
        //progressDialog.setMessage("Cargando puntos en el mapa.");
        //progressDialog.show();

        spinnerDestinoArray =  new ArrayList<String>();
        spinnerVehiculoArray =  new ArrayList<String>();

        final JsonArrayRequest request = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                puntosArray = response;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject object = response.getJSONObject(i);
                        latitud = object.getDouble("latitud");
                        longitud = object.getDouble("longitud");
                        titulo = object.getString("titulo");
                        spinnerDestinoArray.add(titulo);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud)).title(titulo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                CameraUpdate Zoom = CameraUpdateFactory.zoomTo(8);
                mMap.animateCamera(Zoom);
                //mMap.moveCamera(new LatLng());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // Load Spinners
        spinnerVehiculoArray.add("Carro");
        spinnerVehiculoArray.add("Bicicleta");
        spinnerVehiculoArray.add("Caminando");

        ArrayAdapter<String> destinoAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item, spinnerDestinoArray);
        ArrayAdapter<String> vehiculoAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item, spinnerVehiculoArray);
        destinoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehiculoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinoSeleccionable.setAdapter(destinoAdapter);
        vehiculoSeleccionable.setAdapter(vehiculoAdapter);


        requestQueue.add(request);

    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
}
