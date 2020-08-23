package com.desarrollo.notificacionespush.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.core.app.NotificationCompat;

import com.desarrollo.notificacionespush.R;
import com.desarrollo.notificacionespush.app.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationUtils {
    private static String TAG = NotificationUtils.class.getSimpleName();
    private Context mContext;
    private static final int REQUEST_NOTIFICATION = 0;
    private Bitmap bitmap;

    private static final String PRIMARY_CHANNEL_ID = "channel_2";
    private static final String PRIMARY_CHANNEL_NAME = "channel_check";
    private static final int NOTIFICATION_ID = 1;


    public NotificationUtils(Context context){
        this.mContext = context;
    }

    public void showNotificationMessage(final String title, final String message, Intent intent){
        showNotificationMessage(title, message, intent, null);
    }

    public void showNotificationMessage(final String title, final String message, Intent intent, String imageUrl){

        final int icon = R.mipmap.ic_launcher_round;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, REQUEST_NOTIFICATION, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, Config.CHANNEL_ID);

        final Uri alarma = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+mContext.getPackageName()+"raw/tono");

        if(TextUtils.isEmpty(imageUrl)){
            if(imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()){
                try{
                    URL url = new URL(imageUrl);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                if(bitmap != null){
                    showBigNotification(bitmap, mBuilder, icon, title, message, resultPendingIntent, alarma);
                    playNotificationAlarm();
                }else{
                    showSmallNotification(mBuilder, icon, title, message, resultPendingIntent, alarma);
                    playNotificationAlarm();
                }
            }
        }
    }

    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent, Uri alarma){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(message);
        Notification notification = mBuilder.setSmallIcon(icon).setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarma)
                .setStyle(inboxStyle)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    private static long getTimeMillisec(String timeStamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date date = (format.parse(timeStamp));
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent, Uri alarma){
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            bigPictureStyle.setSummaryText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY).toString());
        }else{
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        }
        bigPictureStyle.bigPicture(bitmap);
        Notification notification = mBuilder.setSmallIcon(icon).setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarma)
                .setStyle(bigPictureStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE, notification);
    }

    public void playNotificationAlarm(){
        try{
            Uri alarma = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+mContext.getPackageName()+"/raw/notification");
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, alarma);
            ringtone.play();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isAppIsInBackground(Context context){
        boolean isInBackground = true;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos){
            if(processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                for(String activeProcess : processInfo.pkgList){
                    if(activeProcess.equals(context.getPackageName())){
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }


    public void senNotificationForeground(String title, String message, String urlImage) {


        try{
            URL url = new URL(urlImage);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            bigPictureStyle.setSummaryText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY).toString());
        }else{
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        }

        bigPictureStyle.bigPicture(bitmap);

        final int icon = R.drawable.ic_notify;
        //custom notification sound
        Uri soundCustom = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.notification);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL_ID);
        notifyBuilder.setSmallIcon(icon);
        notifyBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon));
        notifyBuilder.setStyle(bigPictureStyle);
        notifyBuilder.setContentTitle(title);
        notifyBuilder.setContentText(message);
        notifyBuilder.setTicker(message);
        notifyBuilder.setSound(soundCustom);
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notifyBuilder.setCategory(NotificationCompat.CATEGORY_PROMO);
        notifyBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notifyBuilder.setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Create Notification Channel
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Checks");
            notificationManager.createNotificationChannel(notificationChannel);
        }
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}
