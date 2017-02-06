package morc.helpme.kr.morc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import io.realm.Realm;
import io.realm.RealmResults;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import morc.helpme.kr.morc.model.Envelope;
import morc.helpme.kr.morc.model.LogInfo;
import morc.helpme.kr.morc.model.Payload;
import morc.helpme.kr.morc.model.Route;
import morc.helpme.kr.morc.model.SMSInfo;
import morc.helpme.kr.morc.model.Trigger;
import morc.helpme.kr.morc.retrofit.HelpmeService;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SMSListener extends BroadcastReceiver {

  private Retrofit retrofit;
  private HelpmeService helpmeService;

  @Override
  public void onReceive(Context context, Intent intent) {
    initializeRetrofit();

    if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        try{
          Object[] pdus = (Object[]) bundle.get("pdus");
          SmsMessage[] messages = new SmsMessage[pdus.length];
          for(int i = 0; i < messages.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
            } else {
              messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            String msgFrom = messages[i].getOriginatingAddress();
            String msgBody = messages[i].getMessageBody();

            final Realm realm = Realm.getDefaultInstance();
            RealmResults<Route>
                routeRealmResults = realm.where(Route.class).equalTo("enabled", true).findAll();

            for(int j = 0; j < routeRealmResults.size(); j++) {
              final Route route = routeRealmResults.get(j);
              if(route.satisfyCondition(msgFrom, msgBody)) {
                for(int k = 0; k < route.urlList.size(); k++) {
                  Trigger trigger = new Trigger(route);
                  Envelope envelope = new Envelope(msgFrom);
                  //TODO title
                  Payload payload = new Payload("a", msgBody);
                  Timestamp timestamp = new Timestamp(messages[i].getTimestampMillis());

                  SMSInfo smsInfo = new SMSInfo(trigger, envelope, payload, timestamp);

                  helpmeService.dynamic(route.urlList.get(k).str, route.authorization, smsInfo).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                      Realm realm1 = Realm.getDefaultInstance();
                      realm1.beginTransaction();

                      LogInfo logInfo = realm1.createObject(LogInfo.class);
                      logInfo.initialize(route.title, formattedDate(), String.valueOf(response.code()), null);

                      realm1.commitTransaction();
                    }

                    @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                      Realm realm1 = Realm.getDefaultInstance();
                      realm1.beginTransaction();

                      LogInfo logInfo = realm1.createObject(LogInfo.class);
                      logInfo.initialize(route.title, formattedDate(), LogInfo.ERROR, t.toString());

                      realm1.commitTransaction();
                    }
                  });
                }
              }
            }
          }
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void initializeRetrofit() {
    if(retrofit == null) {
      HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
      interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

      retrofit = new Retrofit.Builder()
          .baseUrl("http://localhost")
          .client(client)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
    }

    helpmeService = retrofit.create(HelpmeService.class);
  }

  private String formattedDate() {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.KOREA);
    return format.format(new Date());
  }
}
