package com.android.jshuin.buscarexito;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String URL = "http://192.168.1.5:8888/Buscameexito/consulta.php";
    ProgressDialog progressDialog;
    RequestQueue requestQueue;
    double latitud, longitud, la1, la2, long1, long2;
    String titulo, medio;
    Spinner destinoSeleccionable, vehiculoSeleccionable;
    JSONArray puntosArray;
    List<String> spinnerDestinoArray;
    List<String> spinnerVehiculoArray;
    Button trazarBoton;

    private GoogleMap mMap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void cargarpuntos() {

        spinnerDestinoArray = new ArrayList<String>();
        spinnerVehiculoArray = new ArrayList<String>();

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

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void manejarRutas() {

        trazarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String destinoString = "";
                String vehiculoString = "";

                if (destinoSeleccionable.getSelectedItem() != null) {
                    destinoString = destinoSeleccionable.getSelectedItem().toString();
                } else {
                    Toast.makeText(getApplicationContext(), "No hay dato seleccionado en el spinner", Toast.LENGTH_SHORT).show();
                }
                if (vehiculoSeleccionable.getSelectedItem() != null) {
                    vehiculoString = vehiculoSeleccionable.getSelectedItem().toString();
                } else {
                    Toast.makeText(getApplicationContext(), "No has seleccionado el medio", Toast.LENGTH_SHORT).show();
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

                    Toast.makeText(getApplicationContext(), "Los datos del server no llegan", Toast.LENGTH_SHORT).show();
                }

                // Enabling MyLocation Layer of Google Map
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                } else {
                    Toast.makeText(getApplicationContext(), "No se puede encontrar la ubicación", Toast.LENGTH_SHORT).show();
                }

                trazarRutaHacia(longitudDestino, latitudDestino, longitudOrigen, latitudOrigen, vehiculoString);
            }
        });

    }

    private void trazarRutaHacia(double longitudDestino, double latitudDestino, double longitudOrigen, double latitudOrigen, String medioDeTransporte) {

        //Toast.makeText(getApplicationContext(), longitudDestino + "\n" + latitudDestino + "\n" + longitudOrigen + "\n" + latitudOrigen + "\n" + medioDeTransporte, Toast.LENGTH_SHORT).show();

        mMap.clear();

        la1= latitudOrigen;
        la2= latitudDestino;
        long1= longitudOrigen;
        long2= longitudDestino;
        medio = medioDeTransporte;

        String url=GenerarURL (latitudOrigen,longitudOrigen, latitudDestino, longitudDestino);
        //String url=GenerarURL (latitudOrigen,longitudOrigen, 6.260242, -75.5947227);
        final ProgressDialog cargar=ProgressDialog.show(this,"Obteniendo ruta", "Por favor tenganos paciencia",false,false);

        StringRequest request=new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                cargar.dismiss();
                pintarruta(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);

    }

    private String GenerarURL(Double la1, Double long1, Double la2, Double long2) {
        StringBuilder urlruta= new StringBuilder();
        urlruta.append("https://maps.googleapis.com/maps/api/directions/json");
        urlruta.append("?origin=");
        urlruta.append(Double.toString(la1));
        urlruta.append(",");
        urlruta.append(Double.toString(long1));
        urlruta.append("&destination=");
        urlruta.append(Double.toString(la2));
        urlruta.append(",");
        urlruta.append(Double.toString(long2));

        if (medio.equals("Carro")){
            urlruta.append("&sensor=false&mode=driving&alternatives=true");
        }else if (medio.equals("Bicicleta")){
            urlruta.append("&sensor=false&mode=walking&alternatives=true");
        }else if (medio.equals("Caminando")){
            urlruta.append("&sensor=false&mode=walking&alternatives=true");
        }else {
            urlruta.append("&sensor=false&mode=driving&alternatives=true");
        }

        urlruta.append("&key=AIzaSyCDY-p2dAegumltiK9AeQo8Wat_FPvq3mk");
        return  urlruta.toString();

    }

    public void pintarruta(String response) {

        LatLng inicio= new LatLng(la1,long1);
        LatLng fin= new LatLng(la2,long2);

        try {
            JSONObject json=new JSONObject(response);
            JSONArray rutaarreglo= json.getJSONArray("routes");
            JSONObject rutas= rutaarreglo.getJSONObject(0);
            JSONObject polilineas= rutas.getJSONObject("overview_polyline");
            String puntosstring=polilineas.getString("points");
            List<LatLng> lista= decodePoly(puntosstring);
            Polyline linea= mMap.addPolyline(new PolylineOptions()
                    .addAll(lista)
                    .width(20)
                    .color(Color.GREEN)
                    .geodesic(true));
            mMap.addMarker(new MarkerOptions().position(inicio).title("marcador inicial"));
            mMap.addMarker(new MarkerOptions().position(fin).title("marcador final"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(inicio));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la1,long1),14));


        } catch (JSONException e) {
            e.printStackTrace();
        }

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

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.android.jshuin.buscarexito/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.android.jshuin.buscarexito/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
