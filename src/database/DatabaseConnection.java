package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static volatile HikariDataSource dataSource;
    private static volatile Properties configProps;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (dataSource == null) {
            synchronized (DatabaseConnection.class) {
                if (dataSource == null) {
                    init();
                }
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexao do pool: " + e.getMessage());
            return null;
        }
    }

    private static void init() {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input == null) {
                throw new RuntimeException("Arquivo " + CONFIG_FILE + " nao encontrado.");
            }
            props.load(input);
            input.close();
            configProps = props;

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "5432");
            String name = props.getProperty("db.name", "barbearia");
            String user = props.getProperty("db.user", "postgres");
            String password = props.getProperty("db.password", "postgres");

            int minPool = Integer.parseInt(props.getProperty("db.pool.minimum", "2"));
            int maxPool = Integer.parseInt(props.getProperty("db.pool.maximum", "10"));
            long timeout = Long.parseLong(props.getProperty("db.pool.timeout", "30000"));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + name);
            config.setUsername(user);
            config.setPassword(password);
            config.setMinimumIdle(minPool);
            config.setMaximumPoolSize(maxPool);
            config.setConnectionTimeout(timeout);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Erro fatal ao inicializar pool de conexoes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static String getConfigProperty(String key, String defaultValue) {
        if (configProps == null) {
            return defaultValue;
        }
        return configProps.getProperty(key, defaultValue);
    }
}