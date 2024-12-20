package ru.shift;

import java.io.IOException;
import java.util.Properties;

public final class PropertiesReader {
    private static final String PORT = "port";

    private PropertiesReader() {}

    public static int readProperties() throws IOException, NumberFormatException {
        try (var resourcesInputStream = ClassLoader.getSystemResourceAsStream("settings.properties")) {
            if (resourcesInputStream == null) {
                throw new IOException("settings.properties file not found in the classpath.");
            }

            Properties properties = new Properties();
            properties.load(resourcesInputStream);

            return Integer.parseInt(properties.getProperty(PORT));

        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Number Format Exception. Please, input valid natural numbers in the properties file.");
        } catch (IOException ex) {
            throw new IOException("Error loading properties file.", ex);
        }
    }
}
