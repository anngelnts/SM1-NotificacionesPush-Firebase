package com.desarrollo.notificacionespush;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.desarrollo.notificacionespush.adapters.NewsAdapter;
import com.desarrollo.notificacionespush.app.Config;
import com.desarrollo.notificacionespush.models.News;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    BroadcastReceiver broadcastReceiver;

    RecyclerView recyclerView;
    private ArrayList<News> news;

    private String messageFB = "";
    private String regID = "";
    private String TOKEN_TO = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String nuevoToken = instanceIdResult.getToken();
                Log.d("NEWTOKEN ", nuevoToken);
                //Tenemos el token
                almacenarPreferencias(nuevoToken);
            }
        });

        setNews();
        initRecyclerView();

        broadcastReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    //mostrar ID
                    mostrarFirebaseId();
                }else if(intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    String mensaje = intent.getStringExtra("mensaje");
                    Toast.makeText(getApplicationContext(), "Notificación Push: " + mensaje, Toast.LENGTH_SHORT).show();
                    messageFB = mensaje;
                }
            }
        };
        mostrarFirebaseId();
    }

    private void almacenarPreferencias(String token){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("REGID", token);
        editor.commit();
        TOKEN_TO = token;
    }

    private void mostrarFirebaseId(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = sharedPreferences.getString("REGID", null);
        Log.d(TAG, " Firebase Id: " + regId);
        if(!TextUtils.isEmpty(regId)){
            regID = "Firebase ID: " + regId;
        }else{
            regID = "No existe una respuesta de Firebase aún";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));

        //clearNotifycation
        clearNotification();

    }

    public void clearNotification(){
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }


    private void initRecyclerView() {
        recyclerView = findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        NewsAdapter newsAdapter = new NewsAdapter(news);
        newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
            @Override
            public void onSendNotificationClick(int position) {
                News obj = news.get(position);
                sendNotification(obj);
            }
        });
        recyclerView.setAdapter(newsAdapter);
    }

    private void setNews(){
        news = new ArrayList<>();
        news.add(new News(1, "Ronald Koeman toma la batuta del Barcelona: ¿Qué fue de la vida de los hombres del ‘Dream Team’?", "Guardiola, Zubizarreta, Bakero, Stoichkov y Laudrup, esto pasó con los gestores del equipo soñado del Barcelona 1992.", "https://elcomercio.pe/resizer/Gt4DntDSgWVRYjYqv7S-W8QWq-E=/580x330/smart/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/6UTQTV3VZZGKXPJKAKRQV5Y6PA.jpg" ));
        news.add(new News(2, "Crear confianza en tiempos de fake news: el reto de los medios frente a la pandemia", "Pese a la coyuntura, el consumo de medios impresos en el Perú crece más que en cinco países de Latinoamérica. Es una oportunidad para promover la transparencia en la búsqueda de información, según la consultora Sherlock Communications.", "https://elcomercio.pe/resizer/AgPRq1xghk4lV5UUX3a5zzcybwk=/580x330/smart/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/YNPI75YCZZBANPUT24YCHT5DVA.jpeg" ));
        news.add(new News(3, "Congreso debate este viernes retiro de fondos de la ONP", "El pleno del Parlamento tiene en agenda además insistencia de la ley de acaparamiento y especulación", "https://elcomercio.pe/resizer/BzvpUWCt1x6kZfxDSL1Ny9fTi3o=/580x330/smart/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/VX4SN6ENSZH6FPCYYSG3VUHDQA.jpg" ));
        news.add(new News(4, "Bayern Munich: Serge Gnabry pasó a la historia de la Champions tras sus goles ante Lyon", "Serge Gnabry de Bayern Munich destaca por sus impresionantes números en la Champions League.", "https://e.rpp-noticias.io/normal/2020/08/19/585458_985443.jpg" ));
        news.add(new News(5, "Bayern Munich envió un mensaje al PSG y calienta la final de la Champions League", "Bayern Munich clasificó a la final de la Champions al golear 3-0 al Lyon en Lisboa.", "https://e.rpp-noticias.io/normal/2020/08/19/292629_985496.jpg" ));
        news.add(new News(6, "Obama advierte que Donald Trump está dispuesto a \"derribar la democracia para ganar\" las elecciones de EE.UU.", "El expresidente Barack Obama pidió a los estadounidenses no dejar que un líder que \"nunca se ha tomado en serio\" su cargo les \"quite\" el derecho a votar.", "https://e.rpp-noticias.io/normal/2020/08/19/023002_985636.jpg" ));
        news.add(new News(7, "Minagri: Retribución por jornal a núcleos ejecutores ayuda a la economía de los agricultores familiares", "En declaraciones a RPP, el ministro Jorge Montenegro sostuvo que la entrega del jornal de 50 soles a los agricultores familiares ya inició en diversas zonas como Piura, Tacna y Arequipa. Agregó que están garantizados 100 000 puestos de trabajo durante el plazo de 90 días que tiene el mecanismo.", "https://e.rpp-noticias.io/normal/2020/08/19/505150_985642.jpg" ));
        news.add(new News(8, "Sin salida: Estados Unidos inicia la parte más intensa del bloqueo comercial a Huawei", "Tras anunciar la expiración de licencias solicitadas por empresas estadounidenses para mantener acuerdos comerciales con Huawei, el gobierno de Estados Unidos ha iniciado la fase más agresiva del veto a la empresa china en medio de varias batallas que enfrentan a Donald Trump con marcas asiáticas.", "https://e.rpp-noticias.io/normal/2020/08/17/203120_984394.png" ));
        news.add(new News(9, "Un asteroide hizo el vuelo más cercano a la Tierra sin impactarla y recién nos enteramos", "2020 QG, del tamaño de un auto, estableció un récord al volar a tan solo 2,950 kilómetros sobre la superficie del planeta.", "https://e.rpp-noticias.io/normal/2020/08/19/173917_985205.png" ));
        news.add(new News(10, "Apple se compromete a cero emisiones de carbono para el 2030", "La compañía quiere neutralizar esta emisiones gracias a una hoja de ruta que tomará 10 años en ser implementada.", "https://e.rpp-noticias.io/normal/2020/07/21/403640_973121.jpg" ));
    }


    private void sendNotification(final News news) {
        if(TOKEN_TO == null) return;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setMessage("Enviando Información a FCM Cloud Messaging");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        Log.d("TOKEN", TOKEN_TO);
        String URL_FCM_SEND = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY = "key=AAAAW2cZIpM:APA91bFpNpwYjDqbDuzCLzEHEhXj86000DiPSxpCeyyDiQzq45rpxD4VWesOgYaJoRFR2V8PD7xIrKMU4-CbLCcgWPL0K1ZFlxnjjab0dbyAY6pbLnCn0_COmI5e54CPXKeLWQuseh8T";
        final String DEVICE_TOKEN = TOKEN_TO;
        Toast.makeText(this, DEVICE_TOKEN, Toast.LENGTH_SHORT).show();
        JSONObject jsonObject = getBodyFCM(DEVICE_TOKEN, news);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_FCM_SEND, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Toast.makeText(MainActivity.this, "Response is: "+jsonObject.toString(), Toast.LENGTH_SHORT).show();
                Log.d("RESPONSE: ", jsonObject.toString());
                progressDialog.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "That didn´t work!", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", SERVER_KEY);
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject getBodyFCM(String to, News news){
        JSONObject json = new JSONObject();
        JSONObject notificationJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        try {
            notificationJson.put("title", news.getTitle());
            notificationJson.put("body", news.getSummary());
            notificationJson.put("image", news.getUrlImage());
            dataJson.put("title", news.getTitle());
            dataJson.put("description", news.getSummary());
            //user
            json.put("to", to);
            json.put("priority", "high");
            //data
            json.put("notification", notificationJson);
            //data extra
            json.put("data", dataJson);
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d("JSONOBJECT", json.toString());
        return json;
    }
}
