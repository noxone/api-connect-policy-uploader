package org.olafneumann.apic.policy;

import static java.util.stream.Collectors.joining;
import static org.olafneumann.apic.policy.IOUtils.createURL;
import static org.olafneumann.apic.policy.IOUtils.createUnsafeOkHttpClientBuilder;
import static org.olafneumann.apic.policy.IOUtils.createXWwwUrlEncoded;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class ApiConnectClient {
	private final OkHttpClient client;
	private final URL server;
	private final String organization;
	private final String catalog;

	ApiConnectClient(URL server, String organization, String catalog, boolean ignoreSsl) {
		this.server = server;
		this.organization = organization;
		this.catalog = catalog;

		OkHttpClient.Builder builder;
		if (ignoreSsl) {
			builder = createUnsafeOkHttpClientBuilder();
		} else {
			builder = new OkHttpClient.Builder();
		}
		builder = builder.cookieJar(new VerySimpleCookieJar());
		//builder.followRedirects(false); // API Connect responds with 302 upon successful login

		this.client = builder.build();
	}

	public boolean login(String username, String password) throws IOException {
		final RequestBody body = RequestBody.create(
			MediaType.parse("application/x-www-form-urlencoded"),
			createXWwwUrlEncoded(
				"j_username",
				username, //
				"j_password",
				password, //
				"login",
				"true"));
		final Request request = new Request.Builder()//
			.post(body)//
			.url(getServerLoginUrl())//
			.build();
		final Response response = client.newCall(request).execute();

		// If unsuccessful the server will respond with a login form
		String responseBody = response.body().string();
		return !responseBody.contains("<title>Login:");
	}

	public Optional<String> uploadPolicy(Path policyZipFile) throws IOException {
		final RequestBody body = new MultipartBody.Builder()
			.setType(MediaType.get("multipart/form-data"))
			.addFormDataPart(
				"file",
				policyZipFile.getFileName().getFileName().toString(),
				RequestBody.create(MediaType.get("application/zip"), policyZipFile.toFile()))
			.build();
		final Request request = new Request.Builder()//
			.post(body)//
			.url(getPolicyUploadUrl())//
			.build();
		final Response response = client.newCall(request).execute();
		int code = response.code();
		if (code == 200) {
			return Optional.empty();
		} else {
			return Optional.of(getErrorMessage(response.body()));
		}
	}

	private String getErrorMessage(ResponseBody body) throws JSONException, IOException {
		JSONObject object = new JSONObject(body.string());
		String id = object.getString("id");
		JSONArray jsonErrors = object.getJSONArray("errors");
		List<String> errors = new ArrayList<>();
		for (int i = 0; i < jsonErrors.length(); ++i) {
			JSONObject map = jsonErrors.getJSONObject(i);
			errors.add(map.getString(map.keys().next()));
		}
		return String.format("ID:\r\n\t%s\r\nMessages:\r\n\t%s", id, errors.stream().collect(joining("\r\n\t")));
	}

	private String getServerBasePath() {
		return server.getProtocol() + "://" + server.getHost() + "/apim/";
	}

	private URL getServerLoginUrl() {
		return createURL(getServerBasePath() + "j_security_check");
	}

	private URL getPolicyUploadUrl() {
		return createURL(
			getServerBasePath() + "proxy/orgs/" + organization + "/environments/" + catalog + "/policies?force=true");
	}
}
