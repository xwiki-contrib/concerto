package org.xwoot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Id$
 */
public class XWootContentProviderConfiguration
{
    private static final String CONFIGURATION_FILE = "xwoot-content-provider.properties";

    private static final String CUMULATIVE_CLASSES_PROPERTY = "cumulative_classes";

    private static final String WOOTABLE_PROPERTIES_SUFFIX = ".wootable_properties";

    private static XWootContentProviderConfiguration sharedInstance;

    private Set<String> cumulativeClasses;

    private Map<String, Set<String>> wootablePropertiesMap;

    private XWootContentProviderConfiguration()
    {
        Properties properties = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);

        if (is != null) {
            try {
                properties.load(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Read cumulative classes */
        cumulativeClasses = new HashSet<String>();

        String cumulativeClassesValue = properties.getProperty(CUMULATIVE_CLASSES_PROPERTY);
        if (cumulativeClassesValue != null) {
            String[] values = cumulativeClassesValue.split(",");
            for (String value : values) {
                cumulativeClasses.add(value.trim());
            }
        }

        /* Read wootable fields */
        wootablePropertiesMap = new HashMap<String, Set<String>>();

        for (Object object : properties.keySet()) {
            String key = (String) object;
            if (key.endsWith(WOOTABLE_PROPERTIES_SUFFIX)) {
                String className = key.substring(0, key.indexOf(WOOTABLE_PROPERTIES_SUFFIX));

                Set<String> wootableProperties = wootablePropertiesMap.get(className);
                if (wootableProperties == null) {
                    wootableProperties = new HashSet<String>();
                    wootablePropertiesMap.put(className, wootableProperties);
                }

                String[] values = properties.getProperty(key).split(",");
                for (String value : values) {
                    wootableProperties.add(value.trim());
                }
            }
        }
    }

    public static XWootContentProviderConfiguration getDefault()
    {
        if (sharedInstance == null) {
            sharedInstance = new XWootContentProviderConfiguration();
        }

        return sharedInstance;
    }

    public boolean isCumulative(String className)
    {
        return cumulativeClasses.contains(className);
    }

    public boolean isWootable(String className, String property)
    {
        Set<String> properties = wootablePropertiesMap.get(className);
        if (properties != null) {
            return properties.contains(property);
        }

        return false;
    }
}
