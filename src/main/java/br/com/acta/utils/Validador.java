package br.com.acta.utils;

import br.com.acta.excecoes.ValidacaoException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Valida os dados dos formulários. */
public class Validador {

    private Validador() { }   // Impede criar instâncias.

    // Regex

    // E-mail
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // CEP
    private static final Pattern CEP = Pattern.compile("^[0-9]{5}-?[0-9]{3}$");

    // Telefone
    private static final Pattern TELEFONE = Pattern.compile("^(?:\\([0-9]{2}\\)|[0-9]{2}) ?[0-9]{5}-?[0-9]{4}$");

    // CPF
    private static final Pattern CPF = Pattern.compile("^[0-9]{3}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9]{2}$");

    // CNPJ
    private static final Pattern CNPJ = Pattern.compile("^[A-Za-z0-9]{2}\\.?[A-Za-z0-9]{3}\\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?[0-9]{2}$");

    // Métodos gerais

    /** Verifica se o campo foi preenchido. */
    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    /** Verifica o tamanho máximo. */
    public static String tamanhoMaximo(String valor, int max, String campo) {
        if (valor.length() > max) {
            throw new ValidacaoException(campo + " deve ter no máximo " + max + " caracteres.");
        }
        return valor;
    }

    /** Remove tudo que não for número. */
    public static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");   // Remove caracteres não numéricos.
    }

    /** Valida um id positivo. */
    public static Long id(String valor, String campo) {
        String v = obrigatorio(valor, campo);
        try {
            long numero = Long.parseLong(v);
            if (numero <= 0) {
                throw new ValidacaoException(campo + " inválido.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ValidacaoException(campo + " inválido.");
        }
    }

    /** Valida email. */
    public static String email(String valor) {
        String v = obrigatorio(valor, "E-mail");
        tamanhoMaximo(v, 80, "E-mail");   // antes da regex
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidacaoException("E-mail inválido.");
        }
        return v.toLowerCase();
    }

    /** Valida o CEP. */
    public static String cep(String valor) {
        String v = obrigatorio(valor, "CEP");
        if (!CEP.matcher(v).matches()) {
            throw new ValidacaoException("CEP inválido.");
        }
        return somenteDigitos(v);
    }

    /** Valida o telefone. */
    public static String telefone(String valor) {
        String v = obrigatorio(valor, "Telefone");
        if (!TELEFONE.matcher(v).matches()) {
            throw new ValidacaoException("Telefone inválido. Use (11) 98765-4321.");
        }
        String digitos = somenteDigitos(v);
        if (!digitos.matches("[1-9]{2}9\\d{8}")) {        // Valida DDD e celular.
            throw new ValidacaoException("Telefone inválido: informe DDD + celular com 9 dígitos.");
        }
        return digitos;
    }


    /** Valida o CPF. */
    public static String cpf(String valor) {
        String v = obrigatorio(valor, "CPF");
        if (!CPF.matcher(v).matches()) {
            throw new ValidacaoException("CPF inválido. Use 000.000.000-00.");
        }
        String d = somenteDigitos(v);
        if (todosIguais(d) || !cpfConfere(d)) {
            throw new ValidacaoException("CPF inválido.");
        }
        return d;
    }

    /** Valida o CNPJ. */
    public static String cnpj(String valor) {
        String v = obrigatorio(valor, "CNPJ");
        if (!CNPJ.matcher(v).matches()) {
            throw new ValidacaoException("CNPJ inválido. Use 00.000.000/0000-00.");
        }
        String c = v.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (todosIguais(c) || !cnpjConfere(c)) {
            throw new ValidacaoException("CNPJ inválido.");
        }
        return c;
    }

    /** Verifica se todos os caracteres são iguais. */
    private static boolean todosIguais(String texto) {
        return texto.chars().distinct().count() == 1;
    }

    // Calcula o dígito verificador do CPF.
    private static int digitoCpf(String base) {
        int peso = base.length() + 1;
        int soma = 0;
        for (char c : base.toCharArray()) {
            soma += (c - '0') * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cpfConfere(String d) {
        return d.charAt(9) - '0' == digitoCpf(d.substring(0, 9))
                && d.charAt(10) - '0' == digitoCpf(d.substring(0, 10));
    }

    // Pesos para validar o CNPJ.
    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private static int digitoCnpj(String base) {
        int inicio = PESOS_CNPJ.length - base.length();
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += (base.charAt(i) - '0') * PESOS_CNPJ[inicio + i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cnpjConfere(String c) {
        return c.charAt(12) - '0' == digitoCnpj(c.substring(0, 12))
                && c.charAt(13) - '0' == digitoCnpj(c.substring(0, 13));
    }

    // Outros campos

    private static final int TEXTO_LONGO_MAX = 2000;   // Limite do texto longo.

    // Texto de uma linha
    private static final Pattern LINHA = Pattern.compile("^[^\\p{Cntrl}]+$");
    // Texto de várias linhas
    private static final Pattern LONGO = Pattern.compile("^[\\P{Cntrl}\\r\\n\\t]+$");
    // Nome de pessoa ou cidade
    private static final Pattern NOME_PESSOA = Pattern.compile("^\\p{L}[\\p{L} '.-]*$");
    // Siglas dos estados
    private static final Pattern UF = Pattern.compile("^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$");
    // Número do endereço
    private static final Pattern NUMERO = Pattern.compile("^(\\d{1,6}[A-Za-z]?|[Ss]/[Nn])$");
    // Senha
    private static final Pattern SENHA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    // Data no formato AAAA-MM-DD
    private static final Pattern DATA = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /** Valida texto de uma linha. */
    public static String texto(String valor, String campo, int max) {
        String v = obrigatorio(valor, campo);
        tamanhoMaximo(v, max, campo);      // antes da regex
        if (!LINHA.matcher(v).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return v;
    }

    /** Valida texto opcional. */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, max);
    }

    /** Valida texto com várias linhas. */
    public static String textoLongo(String valor, String campo) {
        String v = obrigatorio(valor, campo);
        tamanhoMaximo(v, TEXTO_LONGO_MAX, campo);   // antes da regex
        if (!LONGO.matcher(v).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return v;
    }

    public static String textoLongoOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return textoLongo(valor, campo);
    }

    /** Valida nome de pessoa ou cidade. */
    public static String nomePessoa(String valor, String campo, int max) {
        String v = obrigatorio(valor, campo);
        tamanhoMaximo(v, max, campo);      // antes da regex
        if (!NOME_PESSOA.matcher(v).matches()) {
            throw new ValidacaoException(campo + " deve conter apenas letras.");
        }
        return v;
    }


    /** Valida a sigla do estado. */
    public static String uf(String valor) {
        String v = obrigatorio(valor, "Estado").toUpperCase();
        if (!UF.matcher(v).matches()) {
            throw new ValidacaoException("Estado inválido. Use a sigla, por exemplo SP.");
        }
        return v;
    }

    /** Valida o número do endereço. */
    public static String numeroEndereco(String valor) {
        String v = obrigatorio(valor, "Número");
        if (!NUMERO.matcher(v).matches()) {
            throw new ValidacaoException("Número inválido. Use 123, 12A ou S/N.");
        }
        return v.toUpperCase();
    }

    /** Valida a senha. */
    public static String senha(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new ValidacaoException("Senha é obrigatória.");
        }
        if (valor.length() > 100) {        // era 101
            throw new ValidacaoException("Senha deve ter no máximo 100 caracteres.");
        }
        if (!SENHA.matcher(valor).matches()) {
            throw new ValidacaoException("Senha deve ter no mínimo 8 caracteres, com letras e números.");
        }
        return valor;
    }

    /** Valida a data. */
    public static Date data(String valor, String campo) {
        String v = obrigatorio(valor, campo);
        if (!DATA.matcher(v).matches()) {
            throw new ValidacaoException(campo + " inválida. Use o formato AAAA-MM-DD.");
        }
        LocalDate data;
        try {
            data = LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException(campo + " inválida.");
        }
        if (data.getYear() < 1900 || data.getYear() > 2100) {
            throw new ValidacaoException(campo + " fora do intervalo permitido.");
        }
        return Date.valueOf(data);
    }

    public static Date dataOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return data(valor, campo);
    }

    /** Valida uma data que não pode ser futura. */
    public static Date dataNaoFutura(String valor, String campo) {
        Date d = data(valor, campo);
        if (d.toLocalDate().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new ValidacaoException(campo + " não pode estar no futuro.");
        }
        return d;
    }

    /** Verifica se a data final é anterior à inicial. */
    public static void periodo(Date inicio, Date fim, String campoFim) {
        if (inicio != null && fim != null && fim.before(inicio)) {
            throw new ValidacaoException(campoFim + " não pode ser anterior à data de início.");
        }
    }

    /** Converte texto para enum. */
    public static <E extends Enum<E>> E opcao(Class<E> tipo, String valor, String campo) {
        String v = obrigatorio(valor, campo);
        try {
            return Enum.valueOf(tipo, v);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(campo + " inválido.");
        }
    }

    /** Converte valor de checkbox para booleano. */
    public static boolean booleano(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        String v = valor.trim().toLowerCase();
        if (v.equals("true") || v.equals("on") || v.equals("1")) {
            return true;
        }
        if (v.equals("false") || v.equals("off") || v.equals("0")) {
            return false;
        }
        throw new ValidacaoException("Valor inválido para campo marcável.");
    }

    /** Valida id opcional. */
    public static Long idOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return id(valor, campo);
    }
}
