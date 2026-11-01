package br.com.acta.dao;

import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.model.Empresa;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testa os resultados e conversões de falha sem conectar ao PostgreSQL real. */
class EmpresaDAOTest {

    private static Driver driverPostgresOriginal;
    private static Driver driverFalso;
    private static SQLException falhaConfigurada;
    private static int resultadoConfigurado;

    @BeforeAll
    static void instalarDriverFalso() throws Exception {
        Class.forName("org.postgresql.Driver");
        var drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals("org.postgresql.Driver")) {
                driverPostgresOriginal = driver;
                DriverManager.deregisterDriver(driver);
                break;
            }
        }

        driverFalso = new Driver() {
            @Override
            public Connection connect(String url, Properties info) throws SQLException {
                if (!acceptsURL(url)) return null;
                PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("executeUpdate")) {
                                if (falhaConfigurada != null) throw falhaConfigurada;
                                return resultadoConfigurado;
                            }
                            if (method.getName().equals("isClosed")) return false;
                            return null;
                        });
                return (Connection) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[]{Connection.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("prepareStatement")) return statement;
                            if (method.getName().equals("isClosed")) return false;
                            return null;
                        });
            }

            @Override public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:postgresql:"); }
            @Override public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new java.sql.DriverPropertyInfo[0]; }
            @Override public int getMajorVersion() { return 1; }
            @Override public int getMinorVersion() { return 0; }
            @Override public boolean jdbcCompliant() { return false; }
            @Override public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(driverFalso);
    }

    @AfterAll
    static void restaurarDriverPostgres() throws Exception {
        if (driverFalso != null) DriverManager.deregisterDriver(driverFalso);
        if (driverPostgresOriginal != null) DriverManager.registerDriver(driverPostgresOriginal);
    }

    @Test
    void retornaUmQuandoInsercaoAfetaRegistro() {
        falhaConfigurada = null;
        resultadoConfigurado = 1;
        assertEquals(1, new EmpresaDAO().inserir(empresa()));
    }

    @Test
    void retornaZeroQuandoNenhumaLinhaFoiAfetada() {
        falhaConfigurada = null;
        resultadoConfigurado = 0;
        assertEquals(0, new EmpresaDAO().inserir(empresa()));
    }

    @Test
    void converteSqlStateDeDuplicidade() {
        falhaConfigurada = new SQLException("duplicado simulado", "23505");
        assertThrows(DuplicidadeException.class, () -> new EmpresaDAO().inserir(empresa()));
    }

    @Test
    void converteSqlStateDeReferenciaInvalida() {
        falhaConfigurada = new SQLException("referência simulada", "23503");
        assertThrows(IntegridadeReferencialException.class, () -> new EmpresaDAO().inserir(empresa()));
    }

    @Test
    void converteFalhaSqlGenerica() {
        falhaConfigurada = new SQLException("falha simulada", "08006");
        assertThrows(DaoException.class, () -> new EmpresaDAO().inserir(empresa()));
    }

    private static Empresa empresa() {
        return new Empresa("Empresa teste", "Tecnologia", "00000000000000", Status.ATIVA, TamanhoEmpresa.PEQUENA);
    }
}
