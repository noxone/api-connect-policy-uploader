package org.olafneumann.apic.policy;

import static org.olafneumann.apic.policy.IOUtils.createUnsafeOkHttpClientBuilder;

import java.nio.file.Path;

import okhttp3.OkHttpClient;

class ApiConnectClient {
	private final OkHttpClient client;
	private final String server;
	private final String organization;
	private final String catalog;

	ApiConnectClient(String server, String organization, String catalog, boolean checkSsl) {
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

	public boolean login(String username, String password) {
		throw new UnsupportedOperationException("login() is not yet implemented");
	}

	public boolean uploadPolicy(Path policyZipFile) {
		throw new UnsupportedOperationException("uploadPolicy() is not yet implemented");
	}

}
