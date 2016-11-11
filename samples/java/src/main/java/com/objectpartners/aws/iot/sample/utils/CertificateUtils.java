package com.objectpartners.aws.iot.sample.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Properties;

/**
 * Per AWS sample codes recommendation, copied from:
 * <p>
 * <a href="https://github.com/aws/aws-iot-device-sdk-java/blob/master/aws-iot-device-sdk-java-samples/src/main/java/com/amazonaws/services/iot/client/sample/sampleUtil/SampleUtil.java">https://github.com/aws/aws-iot-device-sdk-java/blob/master/aws-iot-device-sdk-java-samples/src/main/java/com/amazonaws/services/iot/client/sample/sampleUtil/SampleUtil.java</a>
 * <p>
 * This is a helper class to facilitate reading of the configurations and
 * certificate from the resource files.
 */
public class CertificateUtils {

	private static final String PropertyFile = "aws-iot-sdk-samples.properties";

	public static class KeyStorePasswordPair {
		public KeyStore keyStore;
		public String keyPassword;

		public KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
			this.keyStore = keyStore;
			this.keyPassword = keyPassword;
		}
	}

	public static String getConfig(String name) {
		Properties prop = new Properties();
		URL resource = CertificateUtils.class.getResource(PropertyFile);
		if (resource == null) {
			return null;
		}
		try (InputStream stream = resource.openStream()) {
			prop.load(stream);
		} catch (IOException e) {
			return null;
		}
		String value = prop.getProperty(name);
		if (value == null || value.trim().length() == 0) {
			return null;
		} else {
			return value;
		}
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(String certificateFile, String privateKeyFile) {
		return getKeyStorePasswordPair(certificateFile, privateKeyFile, null);
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(String certificateFile, String privateKeyFile,
			String keyAlgorithm) {
		if (certificateFile == null || privateKeyFile == null) {
			System.out.println("Certificate or private key file missing");
			return null;
		}

		Certificate certificate = loadCertificateFromFile(certificateFile);
		PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, keyAlgorithm);
		if (certificate == null || privateKey == null) {
			return null;
		}

		return getKeyStorePasswordPair(certificate, privateKey);
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(Certificate certificate, PrivateKey privateKey) {
		KeyStore keyStore = null;
		String keyPassword = null;
		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			keyStore.setCertificateEntry("alias", certificate);

			// randomly generated key password for the key in the KeyStore
			keyPassword = new BigInteger(128, new SecureRandom()).toString(32);
			keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), new Certificate[] { certificate });
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			System.out.println("Failed to create key store");
			return null;
		}

		return new KeyStorePasswordPair(keyStore, keyPassword);
	}

	private static Certificate loadCertificateFromFile(String filename) {
		Certificate certificate = null;

		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Certificate file not found: " + filename);
			return null;
		}
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			certificate = certFactory.generateCertificate(stream);
		} catch (IOException | CertificateException e) {
			System.out.println("Failed to load certificate file " + filename);
		}

		return certificate;
	}

	private static PrivateKey loadPrivateKeyFromFile(String filename, String algorithm) {
		PrivateKey privateKey = null;

		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Private key file not found: " + filename);
			return null;
		}
		try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
			privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
		} catch (IOException | GeneralSecurityException e) {
			System.out.println("Failed to load private key from file " + filename);
		}

		return privateKey;
	}

}