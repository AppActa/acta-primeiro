package br.com.acta.utils;

import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.enums.Status;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Exercita entradas válidas e exceções de validação sem abrir conexão com o banco. */
class ValidadorTest {

    @Test
    void aceitaAs27UfsBrasileiras() {
        List<String> ufs = List.of(
                "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
                "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        for (String uf : ufs) {
            assertEquals(uf, Validador.uf(uf), "UF válida: " + uf);
        }
        assertEquals("SP", Validador.uf(" sp "));
    }

    @Test
    void rejeitaUfForaDoBrasil() {
        for (String uf : List.of("ZZ", "CA", "XX", "S", "SPO")) {
            assertThrows(ValidacaoException.class, () -> Validador.uf(uf), uf);
        }
        assertThrows(ValidacaoException.class, () -> Validador.uf(null));
    }

    @Test
    void rejeitaIdsInvalidos() {
        for (String id : List.of("0", "-1", "abc", "2147483648")) {
            assertThrows(ValidacaoException.class, () -> Validador.id(id, "Id"), id);
        }
    }

    @Test
    void rejeitaDatasInvalidasOuForaDoIntervalo() {
        for (String data : List.of("2026-02-30", "2026/01/01", "1899-12-31", "2101-01-01")) {
            assertThrows(ValidacaoException.class, () -> Validador.data(data, "Data"), data);
        }
    }

    @Test
    void rejeitaSenhasFracas() {
        for (String senha : List.of("", "abcdefgh", "12345678", "abc123")) {
            assertThrows(ValidacaoException.class, () -> Validador.senha(senha), senha);
        }
    }

    @Test
    void validaCamposObrigatoriosTextosEIdsOpcionais() {
        assertEquals("Ana", Validador.obrigatorio(" Ana ", "Nome"));
        assertThrows(ValidacaoException.class, () -> Validador.obrigatorio("  ", "Nome"));
        assertEquals("Oi", Validador.texto(" Oi ", "Campo", 10));
        assertThrows(ValidacaoException.class, () -> Validador.texto("duas\nlinhas", "Campo", 50));
        assertEquals("linha\nlinha", Validador.textoLongo("linha\nlinha", "Campo"));
        assertEquals("", Validador.textoOpcional(" ", "Campo", 10) == null ? "" : "não nulo");
        assertEquals(null, Validador.textoLongoOpcional(null, "Campo"));
        assertEquals(null, Validador.idOpcional(" ", "ID"));
        assertEquals(3L, Validador.idOpcional("3", "ID"));
        assertThrows(ValidacaoException.class, () -> Validador.tamanhoMaximo("123", 2, "Campo"));
    }

    @Test
    void validaEmailTelefoneCepNumeroEEnum() {
        assertEquals("ana@example.com", Validador.email(" Ana@Example.com "));
        assertEquals("11987654321", Validador.telefone("(11) 98765-4321"));
        assertEquals("1134567890", Validador.telefone("11 3456-7890"));
        assertEquals("12345678", Validador.cep("12345-678"));
        assertEquals("S/N", Validador.numeroEndereco("s/n"));
        assertEquals(Status.ATIVA, Validador.opcao(Status.class, "ATIVA", "Status"));
        for (String item : List.of("sem-email", "ana@localhost", "x".repeat(81) + "@a.com"))
            assertThrows(ValidacaoException.class, () -> Validador.email(item), item);
        for (String item : List.of("0000", "(00) 98765-4321", "11 12345-6789"))
            assertThrows(ValidacaoException.class, () -> Validador.telefone(item), item);
        for (String item : List.of("1234", "12-345678", "abcdefgh"))
            assertThrows(ValidacaoException.class, () -> Validador.cep(item), item);
        for (String item : List.of("", "casa 1", "1234567A"))
            assertThrows(ValidacaoException.class, () -> Validador.numeroEndereco(item), item);
        assertThrows(ValidacaoException.class, () -> Validador.opcao(Status.class, "ativa", "Status"));
    }

    @Test
    void cpfECnpjDevemPassarFormatoEDigitosVerificadores() {
        assertEquals("52998224725", Validador.cpf("529.982.247-25"));
        assertEquals("11222333000181", Validador.cnpj("11.222.333/0001-81"));
        for (String item : List.of("111.111.111-11", "529.982.247-24", "123"))
            assertThrows(ValidacaoException.class, () -> Validador.cpf(item), item);
        for (String item : List.of("11.111.111/1111-11", "11.222.333/0001-80", "123"))
            assertThrows(ValidacaoException.class, () -> Validador.cnpj(item), item);
    }

    @Test
    void validaDatasPeriodosEValoresMarcaveis() {
        assertEquals(Date.valueOf("2024-02-29"), Validador.data("2024-02-29", "Data"));
        assertEquals(null, Validador.dataOpcional(" ", "Data"));
        assertEquals(true, Validador.booleano(" ON "));
        assertEquals(false, Validador.booleano("off"));
        assertEquals(false, Validador.booleano(null));
        Validador.periodo(Date.valueOf("2024-01-01"), Date.valueOf("2024-01-01"), "Fim");
        assertThrows(ValidacaoException.class,
                () -> Validador.periodo(Date.valueOf("2024-02-01"), Date.valueOf("2024-01-01"), "Fim"));
        assertThrows(ValidacaoException.class, () -> Validador.dataNaoFutura("2099-01-01", "Data"));
        assertThrows(ValidacaoException.class, () -> Validador.booleano("talvez"));
        assertThrows(ValidacaoException.class, () -> Validador.senha("a".repeat(101) + "1"));
    }
}
