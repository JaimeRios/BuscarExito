package com.android.jshuin.buscarexito;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
    Button trazarBoton;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        destinoSeleccionable = (Spinner) findViewById(R.id.destino);
        vehiculoSeleccionable = (Spinner) findViewById(R.id.vehiculo);
        trazarBoton = (Button) findViewById(R.id.btnruta);
        destinoSeleccionable.setPopupBackgroundResource(R.drawable.spinner);
        vehiculoSeleccionable.setPopupBackgroundResource(R.drawable.spinner);

        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        cargarpuntos();
        manejarRutas();
    }

    private void cargarpuntos() {

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

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    private void manejarRutas() {

        trazarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String destinoString = "";
                String vehiculoString = "Bad Loading..";

                if (destinoSeleccionable.getSelectedItem() != null && vehiculoSeleccionable.getSelectedItem() != null) {
                    destinoString = destinoSeleccionable.getSelectedItem().toString();
                    vehiculoString = vehiculoSeleccionable.getSelectedItem().toString();
                } else {
                    Toast.makeText(getApplicationContext(), "Selecciona todos los campos", Toast.LENGTH_SHORT).show();
                }

                double longitudDestino = 0;
                double latitudDestino = 0;

                if (puntosArray != null) {

                    for (int i = 0; i < puntosArray.length(); i++) {
                        try {
                            JSONObject object = puntosArray.getJSONObject(i);
                            if (destinoString.equals(object.getString("titulo"))) {
                                longitudDestino = object.getDouble("longitud");
                                latitudDestino = object.getDouble("latitud");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "Error al cargar datos del server", Toast.LENGTH_SHORT).show();
                }

                // Enabling MyLocation Layer of Google Map
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
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);

                double longitudOrigen = 0;
                double latitudOrigen = 0;

                if (location != null) {
                    latitudOrigen = location.getLatitude();
                    longitudOrigen = location.getLongitude();
                    //Toast.makeText(getApplicationContext(), "Longitud Actual: "+longitudOrigen+" Latitud Actual: "+latitudOrigen, Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(getApplicationContext(), "No se puede encontrar la ubicación", Toast.LENGTH_SHORT).show();
                }

                trazarRutaHacia(longitudDestino,latitudDestino,longitudOrigen, latitudOrigen, vehiculoString);
            }
        });

    }

    private void trazarRutaHacia(double longitudDestino, double latitudDestino, double longitudOrigen, double latitudOrigen, String medio) {

        Toast.makeText(getApplicationContext(), longitudDestino+"\n"+latitudDestino+"\n"+longitudOrigen+"\n"+latitudOrigen+"\n"+medio, Toast.LENGTH_SHORT).show();

        /*
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(latitudOrigen, longitudOrigen), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.GREEN)); */


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
