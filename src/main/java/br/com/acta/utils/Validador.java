package br.com.acta.utils;

import br.com.acta.excecoes.ValidacaoException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validações de entrada compartilhadas por todos os servlets.
 * Regra: aceita o texto com máscara, valida e devolve o valor LIMPO (só dígitos, quando for o caso).
 * Qualquer problema lança ValidacaoException (o servlet transforma em HTTP 400).
 */
public class Validador {

    private Validador() { }   // classe só de métodos estáticos: ninguém dá "new Validador()"

    // ---------------------------------------------------------------------------------
    // REGEX. O Pattern é criado UMA vez (static final) e reaproveitado em toda chamada.
    // No Java a barra é dobrada: \d no regex101 vira \\d aqui.
    // ---------------------------------------------------------------------------------

    // EXEMPLO RESOLVIDO: e-mail (leia com calma, depois faça os dois de baixo)
    //  ^            começo do texto
    //  [\w.+-]+     1 ou mais: letra/número/_ , ponto, + ou -
    //  @            a arroba
    //  [\w-]+       nome do domínio
    //  (\.[\w-]+)+  1 ou mais ".algo"  (.com  ou  .com.br)
    //  $            fim do texto
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // CEP: 5 dígitos, hífen opcional, 3 dígitos
    private static final Pattern CEP = Pattern.compile("^[0-9]{5}-?[0-9]{3}$");

    // Celular: DDD com parênteses OU sem, espaço opcional, 5 dígitos, hífen opcional, 4 dígitos
    private static final Pattern TELEFONE = Pattern.compile("^(?:\\([0-9]{2}\\)|[0-9]{2}) ?[0-9]{5}-?[0-9]{4}$");

    // CPF: 000.000.000-00 (pontos e hífen opcionais). O ponto precisa de \\. senão "." casa com QUALQUER caractere.
    private static final Pattern CPF = Pattern.compile("^[0-9]{3}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9]{2}$");

    // CNPJ alfanumérico (padrão novo da Receita): 12 primeiros caracteres letras/números, 2 últimos (DV) só números.
    // Também aceita o CNPJ antigo, só com números. Formato: 00.000.000/0000-00 (separadores opcionais).
    private static final Pattern CNPJ = Pattern.compile("^[A-Za-z0-9]{2}\\.?[A-Za-z0-9]{3}\\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?[0-9]{2}$");

    // ---------------------------------------------------------------------------------
    // MÉTODOS GENÉRICOS (prontos)
    // ---------------------------------------------------------------------------------

    /** Tira espaços das pontas e recusa nulo/vazio. */
    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    /** Respeita o tamanho do VARCHAR(n) da coluna. */
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

    public static String email(String valor) {
        String v = obrigatorio(valor, "E-mail");
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidacaoException("E-mail inválido.");
        }
        return v.toLowerCase();
    }

    // ---------------------------------------------------------------------------------
    // OS DOIS DO EXERCÍCIO (a lógica já está pronta; falta só o regex acima)
    // ---------------------------------------------------------------------------------

    /** Devolve o CEP só com dígitos (CHAR(8) no banco). */
    public static String cep(String valor) {
        String v = obrigatorio(valor, "CEP");
        if (!CEP.matcher(v).matches()) {
            throw new ValidacaoException("CEP inválido.");
        }
        return somenteDigitos(v);
    }

    /** Devolve o celular só com dígitos (CHAR(11) no banco): DDD (2) + 9 + 8 dígitos. */
    public static String telefone(String valor) {
        String v = obrigatorio(valor, "Telefone");
        if (!TELEFONE.matcher(v).matches()) {
            throw new ValidacaoException("Telefone inválido. Use (11) 98765-4321.");
        }
        String digitos = somenteDigitos(v);
        if (!digitos.matches("[1-9]{2}9\\d{8}")) {        // DDD sem zero + 9 + 8 dígitos
            throw new ValidacaoException("Telefone inválido: informe DDD + celular com 9 dígitos.");
        }
        return digitos;
    }

    // ---------------------------------------------------------------------------------
    // CPF e CNPJ: o regex confere o FORMATO; a conta abaixo confere os DÍGITOS VERIFICADORES.
    // ---------------------------------------------------------------------------------

    /** Devolve o CPF só com dígitos (CHAR(11) no banco). */
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

    /** Devolve o CNPJ sem pontuação e em maiúsculas (CHAR(14) no banco). */
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

    /** 111.111.111-11 passa na conta mas não existe: recusa tudo igual. */
    private static boolean todosIguais(String texto) {
        return texto.chars().distinct().count() == 1;
    }

    // CPF: cada dígito é multiplicado por um peso que diminui (10,9,8... para o 1º DV; 11,10,9... para o 2º).
    // Soma tudo, pega o resto da divisão por 11: resto menor que 2 vira DV 0, senão DV = 11 - resto.
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

    // CNPJ: mesma ideia, com pesos 6,5,4,3,2,9,8,7,6,5,4,3,2 (o 1º DV usa só os 12 últimos pesos).
    // Letra vale (código ASCII - 48): '0'..'9' = 0..9, 'A' = 17, 'B' = 18... (regra do CNPJ alfanumérico).
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

    // ---------------------------------------------------------------------------------
    // TEXTOS, NOMES, UF, NÚMERO, SENHA, DATAS, OPÇÕES (enum) E IDS
    // ---------------------------------------------------------------------------------

    private static final int TEXTO_LONGO_MAX = 2000;   // limite do app para colunas TEXT

    // Texto de UMA linha: qualquer coisa menos caracteres de controle (tab, quebra de linha, etc.)
    private static final Pattern LINHA = Pattern.compile("^[^\\p{Cntrl}]+$");
    // Texto de várias linhas (descrições): permite quebra de linha e tab
    private static final Pattern LONGO = Pattern.compile("^[\\P{Cntrl}\\r\\n\\t]+$");
    // Nome de pessoa/cidade: começa com letra (aceita acento), depois letras, espaço, apóstrofo, ponto ou hífen
    private static final Pattern NOME_PESSOA = Pattern.compile("^\\p{L}[\\p{L} '.-]*$");
    // As 27 siglas de estado
    private static final Pattern UF = Pattern.compile("^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$");
    // Número do endereço: 1 a 6 dígitos com letra opcional (12, 12A) ou S/N
    private static final Pattern NUMERO = Pattern.compile("^(\\d{1,6}[A-Za-z]?|[Ss]/[Nn])$");
    // Senha: mínimo 8 caracteres, com pelo menos uma letra e um número
    private static final Pattern SENHA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    // Data do <input type="date">: AAAA-MM-DD
    private static final Pattern DATA = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /** Texto obrigatório de uma linha, respeitando o VARCHAR(max) da coluna. */
    public static String texto(String valor, String campo, int max) {
        String v = obrigatorio(valor, campo);
        if (!LINHA.matcher(v).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return tamanhoMaximo(v, max, campo);
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
        String v = obrigatorio(valor, campo);
        if (!LONGO.matcher(v).matches()) {
            throw new ValidacaoException(campo + " contém caracteres inválidos.");
        }
        return tamanhoMaximo(v, TEXTO_LONGO_MAX, campo);
    }

    public static String textoLongoOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return textoLongo(valor, campo);
    }

    /** Nome de pessoa ou cidade: só letras, espaço, apóstrofo, ponto e hífen. */
    public static String nomePessoa(String valor, String campo, int max) {
        String v = obrigatorio(valor, campo);
        if (!NOME_PESSOA.matcher(v).matches()) {
            throw new ValidacaoException(campo + " deve conter apenas letras.");
        }
        return tamanhoMaximo(v, max, campo);
    }

    /** Sigla do estado (CHAR(2)): devolve em maiúsculas. */
    public static String uf(String valor) {
        String v = obrigatorio(valor, "Estado").toUpperCase();
        if (!UF.matcher(v).matches()) {
            throw new ValidacaoException("Estado inválido. Use a sigla, por exemplo SP.");
        }
        return v;
    }

    /** Número do endereço (VARCHAR(10)): 123, 12A ou S/N. */
    public static String numeroEndereco(String valor) {
        String v = obrigatorio(valor, "Número");
        if (!NUMERO.matcher(v).matches()) {
            throw new ValidacaoException("Número inválido. Use 123, 12A ou S/N.");
        }
        return v.toUpperCase();
    }

    /** Senha nova. Não faz trim (espaço pode fazer parte da senha). */
    public static String senha(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new ValidacaoException("Senha é obrigatória.");
        }
        if (valor.length() > 100) {
            throw new ValidacaoException("Senha deve ter no máximo 100 caracteres.");
        }
        if (!SENHA.matcher(valor).matches()) {
            throw new ValidacaoException("Senha deve ter no mínimo 8 caracteres, com letras e números.");
        }
        return valor;
    }

    /** Data obrigatória no formato AAAA-MM-DD. Recusa datas que não existem (2026-02-30). */
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

    /** Data que não pode estar no futuro (contratação, problema encontrado...). */
    public static Date dataNaoFutura(String valor, String campo) {
        Date d = data(valor, campo);
        if (d.toLocalDate().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new ValidacaoException(campo + " não pode estar no futuro.");
        }
        return d;
    }

    /** Regra do banco: fim não pode ser antes do início. Fim vazio (null) é permitido. */
    public static void periodo(Date inicio, Date fim, String campoFim) {
        if (inicio != null && fim != null && fim.before(inicio)) {
            throw new ValidacaoException(campoFim + " não pode ser anterior à data de início.");
        }
    }

    /** Converte o texto para um valor do enum (Status, Situacao...). Valor fora da lista é recusado. */
    public static <E extends Enum<E>> E opcao(Class<E> tipo, String valor, String campo) {
        String v = obrigatorio(valor, campo);
        try {
            return Enum.valueOf(tipo, v);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(campo + " inválido.");
        }
    }

    /** Checkbox: marcado chega "on" ou "true"; desmarcado não chega (null). */
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

    /** Id de chave estrangeira que aceita NULL no banco: vazio vira null. */
    public static Long idOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return id(valor, campo);
    }
}
