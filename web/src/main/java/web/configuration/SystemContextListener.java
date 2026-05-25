/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.Security;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.TimeZone;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SystemContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            System.setProperty("file.encoding", "UTF-8");
            Field defaultCharsetField = Charset.class.getDeclaredField("defaultCharset");
            defaultCharsetField.setAccessible(true);
            defaultCharsetField.set(null, null);
            System.setProperty("user.timezone", "UTC");
            TimeZone.setDefault(null);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(servletContextEvent.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF")))) {
                servletContextEvent.getServletContext().setAttribute("version", reader.lines().filter(s -> s.startsWith("version:")).findFirst()
                                                                                      .map(s -> s.replaceFirst("^version:\\s+", "")).get());
            }
            Security.setProperty("crypto.policy", "unlimited");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            try {
                DriverManager.deregisterDriver(drivers.nextElement());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
