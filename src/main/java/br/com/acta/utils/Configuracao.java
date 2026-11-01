package br.com.acta.utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

/** Lê segredos do ambiente do servidor e, em desenvolvimento, de um .env opcional. */
public final class Configuracao {
    private static final Dotenv DOTENV = carregarDotenv();

    /** Impede instâncias desta classe de métodos estáticos. */
    private Configuracao() { }

    /** Procura um .env a partir do diretório de execução e das classes. */
    private static Dotenv carregarDotenv() {
        // Mantém a ordem de busca e evita diretórios duplicados.
        Set<Path> candidatos = new LinkedHashSet<>();
        adicionarPais(candidatos, Path.of(System.getProperty("user.dir", ".")));
        try {
            Path localClasses = Path.of(Configuracao.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            adicionarPais(candidatos, localClasses);
        } catch (URISyntaxException | RuntimeException ignored) {
            // Em alguns containers a localização das classes não é exposta como URI de arquivo.
        }

        for (Path diretorio : candidatos) {
            if (Files.isRegularFile(diretorio.resolve(".env"))) {
                return Dotenv.configure().directory(diretorio.toString()).ignoreIfMissing().load();
            }
        }
        return Dotenv.configure().ignoreIfMissing().load();
    }

    /** Inclui até dez diretórios ancestrais, sem repetir candidatos. */
    private static void adicionarPais(Set<Path> candidatos, Path inicio) {
        Path diretorio = inicio.toAbsolutePath().normalize();
        for (int nivel = 0; diretorio != null && nivel < 10; nivel++, diretorio = diretorio.getParent()) {
            candidatos.add(diretorio);
        }
    }

    /** Prioriza o ambiente do servidor; usa o .env como alternativa. */
    public static String obter(String chave) {
        String valorAmbiente = System.getenv(chave);
        if (valorAmbiente != null && !valorAmbiente.isBlank()) return valorAmbiente;
        return DOTENV.get(chave);
    }

    /** Usa o valor padrão apenas quando a chave não foi encontrada. */
    public static String obter(String chave, String padrao) {
        String valor = obter(chave);
        return valor == null ? padrao : valor;
    }
}
