package com.nbaencolores.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Descarga la APK nueva y la instala encima de la actual (misma firma = no hay que borrar). */
public class Updater {
    public interface Progress { void on(int pct); }

    public static void run(Activity act, String apkUrl, Progress cb) {
        PackageInstaller.Session session = null;
        try {
            HttpURLConnection c = open(apkUrl);
            long total = c.getContentLengthLong();
            PackageInstaller pi = act.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(act.getPackageName());
            if (total > 0) params.setSize(total);
            int sid = pi.createSession(params);
            session = pi.openSession(sid);

            try (InputStream in = c.getInputStream();
                 OutputStream out = session.openWrite("update.apk", 0, total > 0 ? total : -1)) {
                byte[] buf = new byte[65536];
                long done = 0; int n, last = -1;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    done += n;
                    if (total > 0) {
                        int p = (int) (done * 100 / total);
                        if (p != last) { last = p; cb.on(p); }
                    }
                }
                session.fsync(out);
            }
            cb.on(101);
            Intent i = new Intent(act, InstallReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= 31) flags |= PendingIntent.FLAG_MUTABLE;
            PendingIntent pend = PendingIntent.getBroadcast(act, sid, i, flags);
            session.commit(pend.getIntentSender());
            session.close();
        } catch (Exception e) {
            if (session != null) session.abandon();
            cb.on(-1);
        }
    }

    /** Sigue redirecciones manualmente (GitHub redirige a otro servidor). */
    private static HttpURLConnection open(String u) throws Exception {
        for (int i = 0; i < 6; i++) {
            HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
            c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(20000);
            c.setReadTimeout(30000);
            c.setRequestProperty("User-Agent", "NBAenColores");
            int code = c.getResponseCode();
            if (code >= 300 && code < 400) {
                u = new URL(new URL(u), c.getHeaderField("Location")).toString();
                c.disconnect();
                continue;
            }
            if (code != 200) throw new Exception("HTTP " + code);
            return c;
        }
        throw new Exception("Demasiadas redirecciones");
    }
}
