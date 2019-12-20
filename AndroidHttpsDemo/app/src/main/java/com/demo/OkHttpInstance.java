package com.demo;

import android.os.Handler;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okio.Buffer;

public class OkHttpInstance {

    private static final String ROOT_CA_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDSzCCAjMCFCWCftYFB2wpkryu35KGmWXLwKNmMA0GCSqGSIb3DQEBCwUAMGIx\n" +
                    "CzAJBgNVBAYTAkNOMRIwEAYDVQQIDAlHdWFuZ2RvbmcxEjAQBgNVBAcMCUd1YW5n\n" +
                    "emhvdTERMA8GA1UECgwIQ3JlZGl0ZnQxCzAJBgNVBAsMAklUMQswCQYDVQQDDAJX\n" +
                    "VDAeFw0xOTEyMTkwNzM4MDBaFw0yMDAxMTgwNzM4MDBaMGIxCzAJBgNVBAYTAkNO\n" +
                    "MRIwEAYDVQQIDAlHdWFuZ2RvbmcxEjAQBgNVBAcMCUd1YW5nemhvdTERMA8GA1UE\n" +
                    "CgwIQ3JlZGl0ZnQxCzAJBgNVBAsMAklUMQswCQYDVQQDDAJXVDCCASIwDQYJKoZI\n" +
                    "hvcNAQEBBQADggEPADCCAQoCggEBANrOVKqsJiVG+alUbauhiJj7wLq/zqAHrxhS\n" +
                    "8Ca6wJoBXMy32Hij33LmNPXS1VDjxcjkmxAhS/sueNt9oO8io6qtHGD/6ctbGlw3\n" +
                    "kfmHRZLPt4ZyNY6El06cItzvaX9CxsnwsLa5OPWmtDZ/iQSQvNF36js8DoV7U0+x\n" +
                    "rF+reS7RQLg3pGeBcHHNtQuGSZr4ISb+F9FsP09wkIn4/T8WWEXZT+a02U8QG605\n" +
                    "SCoZJuOVlLAtK4vIuMox82ddoDN7mL6MZ1zr9DamVTglzXoUQPwslaDHSpx7l83B\n" +
                    "EgR3bhjX11at7J44C7ZG5UEmfmDfvZnxZ5PHMAOsSKeWdBmBnbMCAwEAATANBgkq\n" +
                    "hkiG9w0BAQsFAAOCAQEA0Ezkq/WiMn92re/O5wvl7FxNmI0bleR5uZlvX0wA/Avq\n" +
                    "VEeHTvXzeGkE4UW6dtoNkhMzMexjIYO172mwDlaqNbN8Y1ywroqT/2awAv8GR1aI\n" +
                    "ync/I7/OsnbYLIwUt+CMvaQkjtEo/UV1Q4iQMZTL7LGCXPs2iveq/n6u/4IMwvyx\n" +
                    "EmUaw2FjJlMl5X+jeuWY65Z7d98tB+yg+Q6Kq+u8o2c+qwvxeEhvbZqrp/njvOPl\n" +
                    "f/NTFznroYCNm9MaNkiXTc3QZ9liUH0YWWKBbnRw5Y+6O1QpJbk9YnwjwMd8aPEH\n" +
                    "QzFYCZYIOVt/e3I4jrnMJRSlbLqrv46Nq10d7IbmlQ==\n" +
                    "-----END CERTIFICATE-----";


    private static volatile OkHttpInstance mInstance;

    private OkHttpClient mOkHttpClient;
    /**
     * 全局处理子线程和主线程通信
     */
    private Handler okHttpHandler;

    private OkHttpInstance() {
        try {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(false)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);

            //TrustManager
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(getKeyStore());

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            //双向认证服务端证书
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(App.getApp().getAssets().open("final.bks"), "123456".toCharArray());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, "123456".toCharArray());

            //SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);

            // If unset, a default hostname verifier will be used.
            // 默认检测只会检测SubjectAltNames，也就是证书中可替换的域名
            // 这里实现验证请求host与证书内host是否相同
            builder.hostnameVerifier((String hostname, SSLSession session) -> {
                try {
                    final Certificate[] certs = session.getPeerCertificates();
                    final X509Certificate x509 = (X509Certificate) certs[0];
                    return verifyCn(hostname, x509);
                } catch (final SSLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            });
            mOkHttpClient = builder.build();

            //可以验证证书内的host，也可以验证固定的host
            //private static String[] VERIFY_HOST_NAME_ARRAY = new String[]{};
            //
            //public static final HostnameVerifier createInsecureHostnameVerifier() {
            //     return new HostnameVerifier() {
            //         @Override
            //         public boolean verify(String hostname, SSLSession session) {
            //             if (TextUtils.isEmpty(hostname)) {
            //                 return false;
            //             }
            //             return !Arrays.asList(VERIFY_HOST_NAME_ARRAY).contains(hostname);
            //         }
            //     };
            // }

            okHttpHandler = new Handler(App.getApp().getMainLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里手动验证CN与host是否相同
     *
     * @param hostname 请求的host
     * @param x509     服务器返回的证书
     * @return host是否合法
     */
    private boolean verifyCn(String hostname, X509Certificate x509) {
        if (hostname == null) {
            return false;
        }
        String name = x509.getSubjectDN().getName();
        if (name != null) {
            String temp = name.substring(name.indexOf("CN="));
            //"CN=XX"不是最后一个，所以后面一定有","
            String cn = temp.substring(3, temp.indexOf(","));
            if (cn.length() > 0) {
                return cn.equals(hostname);
            }
        }
        return false;
    }

    /**
     * 导入证书
     */
    private KeyStore getKeyStore() {
        // 添加https证书
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            // 从文件中获取
            // InputStream is = App.getApp().getAssets().open("rootCA.crt");
            // 从代码中获取
            InputStream is = new Buffer().writeUtf8(ROOT_CA_CERT).inputStream();
            // 签名文件设置证书
            keyStore.setCertificateEntry("0", certificateFactory.generateCertificate(is));
            is.close();
            return keyStore;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取单例引用
     */
    public static OkHttpInstance getInstance() {
        OkHttpInstance inst = mInstance;
        if (inst == null) {
            synchronized (OkHttpInstance.class) {
                inst = mInstance;
                if (inst == null) {
                    inst = new OkHttpInstance();
                    mInstance = inst;
                }
            }
        }
        return inst;
    }

    /**
     * 获取 OkHttp对象
     */
    public static OkHttpClient getmOkHttpClient() {
        return OkHttpInstance.getInstance().mOkHttpClient;
    }

    /**
     * 获取主线程句柄
     */
    public static Handler getMainThreadHandler() {
        return OkHttpInstance.getInstance().okHttpHandler;
    }
}
