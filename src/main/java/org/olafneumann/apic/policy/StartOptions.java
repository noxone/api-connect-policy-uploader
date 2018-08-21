package org.olafneumann.apic.policy;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.PathOptionHandler;
import org.kohsuke.args4j.spi.URLOptionHandler;

public class StartOptions {

	public static StartOptions parse(String[] args) {
		StartOptions options = new StartOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
			return options;
		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("Valid options for this application are:");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			return null;
		}
	}

	private StartOptions() {
	}

	@Option(name = "-s", aliases = { "--server" }, required = true, handler = URLOptionHandler.class,
			usage = "The server where to install the policy")
	private URL server;

	@Option(name = "-u", aliases = { "--username" }, required = false, forbids = { "-ch", "-cf" }, depends = { "-p" },
			usage = "The username to use for policy installation")
	private String username = null;

	@Option(name = "-p", aliases = { "--password" }, required = false, forbids = { "-ch", "-cf" }, depends = { "-u" },
			usage = "The password to use for policy installation")
	private String password = null;

	@Option(name = "-ch", aliases = { "--credentialsHttp" }, required = false, forbids = { "-u", "-p", "-cf" },
			usage = "The cretentials to be used for policy installation in HTTP authentication format. Format: base64(username:password)")
	private String credentialsHttp = null;

	@Option(name = "-cf", aliases = { "--credentialsFile" }, required = false, forbids = { "-u", "-p", "-ch" },
			usage = "Path to file containing credentials information in HTTP authentication format",
			handler = PathOptionHandler.class)
	private Path credentialsFile = null;

	@Option(name = "-o", aliases = { "--organization" }, required = true,
			usage = "The API Connect organization where to install the policy")
	private String organization;

	@Option(name = "-c", aliases = { "--catalog" }, required = true,
			usage = "The API Connect catalog where to install the policy")
	private String catalog;

	@Option(name = "-pf", aliases = { "--policyFile" }, required = true, usage = "Path to file containing policy definition",
			handler = PathOptionHandler.class)
	private Path policyFile;

	@Option(name = "--ignoreSSL", required = false, usage = "Ignore SSL check while policy installation",
			handler = BooleanOptionHandler.class)
	private boolean ignoreSsl = false;

	public URL getServer() {
		return server;
	}

	public String getCatalog() {
		return catalog;
	}

	public String getOrganization() {
		return organization;
	}

	public Path getPolicyFile() {
		return policyFile;
	}

	public boolean isIgnoreSsl() {
		return ignoreSsl;
	}

	public Credentials getCredentials() {
		if (username != null && password != null) {
			return new Credentials(username, password);
		} else if (credentialsHttp != null) {
			return new Credentials(credentialsHttp);
		} else if (credentialsFile != null) {
			try {
				return new Credentials(Files.readAllLines(credentialsFile).iterator().next());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new RuntimeException("No credentials provided.");
		}
	}

	Map<String, String> getProperties() {
		try {
			return Stream//
				.of(Introspector.getBeanInfo(getClass(), Object.class).getPropertyDescriptors())//
				.collect(Collectors.toMap(PropertyDescriptor::getName, pd -> get(pd.getReadMethod())));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String get(Method method) {
		try {
			Object object = method.invoke(this);
			if (object != null) {
				return object.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Credentials {
		private final String username;
		private final String password;

		private Credentials(String base64HttpAuth) {
			String httpAuth = new String(Base64.getDecoder().decode(base64HttpAuth), StandardCharsets.US_ASCII);
			this.username = httpAuth.substring(0, httpAuth.indexOf(":"));
			this.password = httpAuth.substring(httpAuth.indexOf(":") + 1);
		}

		private Credentials(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}

		public String getPassword() {
			return password;
		}

		public String getUsername() {
			return username;
		}
	}
}
