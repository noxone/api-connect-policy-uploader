package org.olafneumann.apic.policy;

import java.io.IOException;
import java.util.Optional;

import org.olafneumann.apic.policy.StartOptions.Credentials;

public class PolicyUploader {
	public static void main(String[] args) throws IOException {
		// parse command line options
		StartOptions options = StartOptions.parse(args);
		// if command line options fail: shut down
		if (options == null) {
			return;
		}

		final Credentials credentials = options.getCredentials();
		final ApiConnectClient client = new ApiConnectClient(
			options.getServer(),
			options.getOrganization(),
			options.getCatalog(),
			options.isIgnoreSsl());

		// Log in to server
		boolean loggedIn = client.login(credentials.getUsername(), credentials.getPassword());
		if (!loggedIn) {
			System.err.println(
				String.format(
					"Unable to log in to server[%s] using username[%s].",
					options.getServer(),
					credentials.getUsername()));
			System.exit(1);
			return;
		}

		// Do the upload
		Optional<String> message = client.uploadPolicy(options.getPolicyFile());
		if (message.isPresent()) {
			System.err.println(String.format("Unable to upload policy.\r\n%s", message.get()));
			System.exit(2);
			return;
		}
	}
}
