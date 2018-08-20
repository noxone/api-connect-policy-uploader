package org.olafneumann.apic.policy;

import static org.olafneumann.apic.policy.IOUtils.createURL;
import static org.olafneumann.apic.policy.IOUtils.createUnsafeOkHttpClientBuilder;
import static org.olafneumann.apic.policy.IOUtils.createXWwwUrlEncoded;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class ApiConnectClient {
	private final OkHttpClient client;
	private final URL server;
	private final String organization;
	private final String catalog;

	ApiConnectClient(URL server, String organization, String catalog, boolean checkSsl) {
		this.server = server;
		this.organization = organization;
		this.catalog = catalog;

		OkHttpClient.Builder builder;
		if (checkSsl) {
			builder = new OkHttpClient.Builder();
		} else {
			builder = createUnsafeOkHttpClientBuilder();
		}
		builder = builder.cookieJar(new VerySimpleCookieJar());
		builder.followRedirects(false); // API Connect responds with 302 upon successful login

		this.client = builder.build();
	}

	public boolean login(String username, String password) throws IOException {
		final RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
				createXWwwUrlEncoded("j_username", username, //
						"j_password", password, //
						"login", "true"));
		final Request request = new Request.Builder()//
				.post(body)//
				.url(getServerLoginUrl())//
				.build();
		final Response response = client.newCall(request).execute();
		// Upon successful login API Connect server returns 302. If the login was not
		// successful the server will return 200.
		return response.code() == 302;
	}

	public Optional<String> uploadPolicy(Path policyZipFile) throws IOException {
		final RequestBody body = MultipartBody.create(MediaType.get(Files.probeContentType(policyZipFile)),
				policyZipFile.toFile());
		final Request request = new Request.Builder()//
				.post(body)//
				.url(getPolicyUploadUrl())//
				.header("Content-type", "multipart/form-data")//
				.build();
		final Response response = client.newCall(request).execute();
		throw new UnsupportedOperationException("uploadPolicy() is not yet implemented");
	}

	private String getServerBasePath() {
		return server.getProtocol() + "://" + server.getHost() + "/";
	}

	private URL getServerLoginUrl() {
		return createURL(getServerBasePath() + "apim/");
	}

	private URL getPolicyUploadUrl() {
		// TODO return createURL(getServerBasePath() + "");
		throw new UnsupportedOperationException("getPolicyUploadUrl() is not yet implemented");
	}
}
