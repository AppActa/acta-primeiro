package br.com.acta.utils;

import br.com.acta.excecoes.ValidacaoException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validações de entrada compartilhadas por todos os servlets.
 * Valida e normaliza os valores; entradas inválidas geram ValidacaoException.
 */
public class Validador {

    // Limites e referência de tempo da aplicação.
    private static final int TEXTO_LONGO_MAX = 2000;
    private static final int EMAIL_MAX = 80;
    private static final int SENHA_MAX = 100;
    private static final int ANO_MIN = 1900;
    private static final int ANO_MAX = 2100;
    private static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    // Formatos aceitos para os campos.

    // Uma linha, sem caracteres de controle.
    private static final Pattern LINHA = Pattern.compile("^[^\\p{Cntrl}]+$");

    // Texto de várias linhas: permite quebras de linha e tabulação.
    private static final Pattern LONGO = Pattern.compile("^[\\P{Cntrl}\\r\\n\\t]+$");

    // Começa com letra; aceita acentos, espaços, apóstrofos, pontos e hífens.
    private static final Pattern NOME_PESSOA = Pattern.compile("^\\p{L}[\\p{L} '.-]*$");

    // Usuário, @ e domínio com pelo menos um ponto.
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // DDD + fixo ou celular; máscara opcional.
    private static final Pattern TELEFONE = Pattern.compile("^(?:\\([0-9]{2}\\)|[0-9]{2}) ?[0-9]{4,5}-?[0-9]{4}$");

    // DDD sem zero; celular com nove dígitos começa com 9.
    private static final Pattern TELEFONE_DIGITOS = Pattern.compile("[1-9]{2}(?:\\d{8}|9\\d{8})");

    // Oito dígitos, com hífen opcional.
    private static final Pattern CEP = Pattern.compile("^[0-9]{5}-?[0-9]{3}$");

    // Siglas dos 26 estados e do Distrito Federal.
    private static final Pattern UF = Pattern.compile(
            "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$");

    // Até seis dígitos com letra opcional, ou S/N.
    private static final Pattern NUMERO = Pattern.compile("^(\\d{1,6}[A-Za-z]?|[Ss]/[Nn])$");

    // Onze dígitos, com pontos e hífen opcionais.
    private static final Pattern CPF = Pattern.compile("^[0-9]{3}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9]{2}$");

    // Doze letras ou números e dois dígitos verificadores; máscara opcional.
    private static final Pattern CNPJ = Pattern.compile(
            "^[A-Za-z0-9]{2}\\.?[A-Za-z0-9]{3}\\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?[0-9]{2}$");

    // No mínimo oito caracteres, com uma letra e um número.
    private static final Pattern SENHA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    // Formato AAAA-MM-DD usado nos formulários.
    private static final Pattern DATA = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    // O primeiro dígito do CNPJ usa os 12 últimos pesos; o segundo usa todos.
    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Validador() {
        // Classe utilitária: não deve ser instanciada.
    }

    // Validações básicas e identificadores.

    /** Tira espaços das pontas e recusa nulo/vazio. */
    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    /** Limita o tamanho de um texto já preenchido. */
    public static String tamanhoMaximo(String valor, int max, String campo) {
        if (valor.length() > max) {
            throw new ValidacaoException(campo + " deve ter no máximo " + max + " caracteres.");
        }
        return valor;
    }

    /** "(11) 98765-4321" -> "11987654321" */
    public static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");   // \D = tudo que NÃO é dígito
    }

    /** Converte um id vindo do formulário. Texto como "abc" vira ValidacaoException. */
    public static Long id(String valor, String campo) {
        String valorValidado = obrigatorio(valor, campo);
        try {
            long numero = Long.parseLong(valorValidado);
            // O ID deve ser positivo e caber em um inteiro do banco.
            if (numero <= 0 || numero > Integer.MAX_VALUE) {
                throw new ValidacaoException(campo + " inválido.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ValidacaoException(campo + " inválido.");
        }
    }

    /** Id de chave estrangeira que aceita NULL no banco: vazio vira null. */
    public static Long idOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return id(valor, campo);
    }

    // Textos e nomes.

    /** Texto obrigatório de uma linha, respeitando o VARCHAR(max) da coluna. */
    public static String texto(String valor, String campo, int max) {
        String valorValidado = obrigatorio(valor, campo);
        if (!LINHA.matcher(valorValidado).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return tamanhoMaximo(valorValidado, max, campo);
    }

    /** Igual ao texto(), mas vazio vira null (coluna que aceita NULL). */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, max);
    }

    /** Texto obrigatório com várias linhas (coluna TEXT). */
    public static String textoLongo(String valor, String campo) {
        String valorValidado = obrigatorio(valor, campo);
        if (!LONGO.matcher(valorValidado).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return tamanhoMaximo(valorValidado, TEXTO_LONGO_MAX, campo);
    }

    /** Valida texto de várias linhas; nulo ou em branco vira null. */
    public static String textoLongoOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return textoLongo(valor, campo);
    }

    /** Nome de pessoa ou cidade: só letras, espaço, apóstrofo, ponto e hífen. */
    public static String nomePessoa(String valor, String campo, int max) {
        String valorValidado = obrigatorio(valor, campo);
        if (!NOME_PESSOA.matcher(valorValidado).matches()) {
            throw new ValidacaoException(campo + " deve conter apenas letras.");
        }
        return tamanhoMaximo(valorValidado, max, campo);
    }

    // Contato e endereço.

    /** Valida formato e limite de 80 caracteres; devolve em minúsculas. */
    public static String email(String valor) {
        String valorValidado = obrigatorio(valor, "E-mail");
        if (!EMAIL.matcher(valorValidado).matches()) {
            throw new ValidacaoException("E-mail inválido.");
        }
        return tamanhoMaximo(valorValidado, EMAIL_MAX, "E-mail").toLowerCase();
    }

    /** Devolve o telefone só com dígitos: DDD + fixo (8) ou celular (9). */
    public static String telefone(String valor) {
        String valorValidado = obrigatorio(valor, "Telefone");
        if (!TELEFONE.matcher(valorValidado).matches()) {
            throw new ValidacaoException("Telefone inválido. Use (11) 3456-7890 ou (11) 98765-4321.");
        }
        String digitos = somenteDigitos(valorValidado);
        // Recusa zero no DDD e exige 9 no início de celulares.
        if (!TELEFONE_DIGITOS.matcher(digitos).matches()) {
            throw new ValidacaoException("Telefone inválido: informe DDD + fixo com 8 dígitos ou celular com 9 dígitos.");
        }
        return digitos;
    }

    /** Devolve o CEP só com dígitos (CHAR(8) no banco). */
    public static String cep(String valor) {
        String valorValidado = obrigatorio(valor, "CEP");
        if (!CEP.matcher(valorValidado).matches()) {
            throw new ValidacaoException("CEP inválido.");
        }
        return somenteDigitos(valorValidado);
    }

    /** Sigla do estado (CHAR(2)): devolve em maiúsculas. */
    public static String uf(String valor) {
        String valorValidado = obrigatorio(valor, "Estado").toUpperCase();
        if (!UF.matcher(valorValidado).matches()) {
            throw new ValidacaoException("Estado inválido. Use a sigla, por exemplo SP.");
        }
        return valorValidado;
    }

    /** Número do endereço (VARCHAR(10)): 123, 12A ou S/N. */
    public static String numeroEndereco(String valor) {
        String valorValidado = obrigatorio(valor, "Número");
        if (!NUMERO.matcher(valorValidado).matches()) {
            throw new ValidacaoException("Número inválido. Use 123, 12A ou S/N.");
        }
        return valorValidado.toUpperCase();
    }

    // Documentos e senha.

    /** Devolve o CPF só com dígitos (CHAR(11) no banco). */
    public static String cpf(String valor) {
        String valorValidado = obrigatorio(valor, "CPF");
        if (!CPF.matcher(valorValidado).matches()) {
            throw new ValidacaoException("CPF inválido. Use 000.000.000-00.");
        }
        String digitos = somenteDigitos(valorValidado);
        if (todosIguais(digitos) || !cpfConfere(digitos)) {
            throw new ValidacaoException("CPF inválido.");
        }
        return digitos;
    }

    /** Devolve o CNPJ sem pontuação e em maiúsculas (CHAR(14) no banco). */
    public static String cnpj(String valor) {
        String valorValidado = obrigatorio(valor, "CNPJ");
        if (!CNPJ.matcher(valorValidado).matches()) {
            throw new ValidacaoException("CNPJ inválido. Use 00.000.000/0000-00.");
        }
        // Preserva as letras do CNPJ ao retirar a pontuação.
        String cnpjLimpo = valorValidado.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (todosIguais(cnpjLimpo) || !cnpjConfere(cnpjLimpo)) {
            throw new ValidacaoException("CNPJ inválido.");
        }
        return cnpjLimpo;
    }

    /** Senha nova. Não faz trim (espaço pode fazer parte da senha). */
    public static String senha(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new ValidacaoException("Senha é obrigatória.");
        }
        if (valor.length() > SENHA_MAX) {
            throw new ValidacaoException("Senha deve ter no máximo 100 caracteres.");
        }
        if (!SENHA.matcher(valor).matches()) {
            throw new ValidacaoException("Senha deve ter no mínimo 8 caracteres, com letras e números.");
        }
        return valor;
    }

    // Datas e períodos.

    /** Data obrigatória no formato AAAA-MM-DD. Recusa datas que não existem (2026-02-30). */
    public static Date data(String valor, String campo) {
        String valorValidado = obrigatorio(valor, campo);
        if (!DATA.matcher(valorValidado).matches()) {
            throw new ValidacaoException(campo + " inválida. Use o formato AAAA-MM-DD.");
        }
        LocalDate data;
        try {
            // O formato sozinho não garante que o dia exista no calendário.
            data = LocalDate.parse(valorValidado);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException(campo + " inválida.");
        }
        // Restringe os anos ao intervalo aceito pela aplicação.
        if (data.getYear() < ANO_MIN || data.getYear() > ANO_MAX) {
            throw new ValidacaoException(campo + " fora do intervalo permitido.");
        }
        return Date.valueOf(data);
    }

    /** Valida a data quando preenchida; nulo ou em branco vira null. */
    public static Date dataOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return data(valor, campo);
    }

    /** Data que não pode estar no futuro (contratação, problema encontrado...). */
    public static Date dataNaoFutura(String valor, String campo) {
        Date dataValidada = data(valor, campo);
        // Usa o dia atual no fuso de São Paulo.
        if (dataValidada.toLocalDate().isAfter(LocalDate.now(FUSO_HORARIO))) {
            throw new ValidacaoException(campo + " não pode estar no futuro.");
        }
        return dataValidada;
    }

    /** Compara datas quando ambas existem; fim igual ao início é permitido. */
    public static void periodo(Date inicio, Date fim, String campoFim) {
        if (inicio != null && fim != null && fim.before(inicio)) {
            throw new ValidacaoException(campoFim + " não pode ser anterior à data de início.");
        }
    }

    // Opções de formulário.

    /** Exige uma opção do enum com a mesma escrita, incluindo maiúsculas. */
    public static <E extends Enum<E>> E opcao(Class<E> tipo, String valor, String campo) {
        String valorValidado = obrigatorio(valor, campo);
        try {
            return Enum.valueOf(tipo, valorValidado);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(campo + " inválido.");
        }
    }

    /** Aceita true/on/1 e false/off/0; nulo ou em branco resulta em false. */
    public static boolean booleano(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        String valorValidado = valor.trim().toLowerCase();
        if (valorValidado.equals("true") || valorValidado.equals("on") || valorValidado.equals("1")) {
            return true;
        }
        if (valorValidado.equals("false") || valorValidado.equals("off") || valorValidado.equals("0")) {
            return false;
        }
        throw new ValidacaoException("Valor inválido para campo marcável.");
    }

    // Cálculos auxiliares de CPF e CNPJ.

    /** Recusa sequências repetidas, mesmo quando passam no cálculo. */
    private static boolean todosIguais(String texto) {
        return texto.chars().distinct().count() == 1;
    }

    /** Compara os dois dígitos finais com os valores calculados. */
    private static boolean cpfConfere(String digitos) {
        return digitos.charAt(9) - '0' == digitoCpf(digitos.substring(0, 9))
                && digitos.charAt(10) - '0' == digitoCpf(digitos.substring(0, 10));
    }

    /** Calcula um dígito do CPF usando pesos decrescentes. */
    private static int digitoCpf(String base) {
        int peso = base.length() + 1;
        int soma = 0;
        for (char caractere : base.toCharArray()) {
            // Subtrair '0' converte o caractere em número.
            soma += (caractere - '0') * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Confere os dois dígitos, incluindo o primeiro na segunda conta. */
    private static boolean cnpjConfere(String cnpjLimpo) {
        return cnpjLimpo.charAt(12) - '0' == digitoCnpj(cnpjLimpo.substring(0, 12))
                && cnpjLimpo.charAt(13) - '0' == digitoCnpj(cnpjLimpo.substring(0, 13));
    }

    /** Calcula um dígito do CNPJ, aceitando números e letras. */
    private static int digitoCnpj(String base) {
        // Alinha os pesos ao tamanho da base: 12 ou 13 caracteres.
        int inicio = PESOS_CNPJ.length - base.length();
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            // ASCII - 48: números valem 0 a 9; letras começam em A = 17.
            soma += (base.charAt(i) - '0') * PESOS_CNPJ[inicio + i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
