package org.olafneumann.apic.policy;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public final class IOUtils {
	private IOUtils() {
		throw new RuntimeException("Do not create instances of this class");
	}

	public static URL createURL(String urlString) {
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static String createXWwwUrlEncoded(String... strings) {
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < strings.length; i += 2) {
				if (i > 0) {
					sb.append("&");
				}
				sb.append(strings[i]).append("=").append(URLEncoder.encode(strings[i + 1], "UTF-8"));
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static OkHttpClient.Builder createUnsafeOkHttpClientBuilder() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			return builder;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("Unable to create unsafe HTTP client", e);
		}
	}
}
