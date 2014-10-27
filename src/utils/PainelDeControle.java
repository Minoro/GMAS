package utils;

import java.io.File;
import middleware.Middleware;
import org.w3c.dom.Document;

/**
 * Classe para a definição de valores utilizados no sistema todo
 *
 * @author mastelini
 *
 */
public class PainelDeControle {

    public static final String NOVO_USUARIO = "0";
    public static final String USUARIO_EXISTENTE = "1";
    public static final String RESPOSTA_NOVO_USUARIO = "0";
    public static final String RESPOSTA_USUARIO_EXISTENTE = "1";
    public static final int TAMANHO_BUFFER = 500;
    public static final int PORTA_MULTICAST = 5678;
    public static final String IP_MULTICAST = "228.5.6.7";
    public static final String SEPARADOR = File.separator;
    public static final String RAIZ = "raiz";
    public static final String HOME = System.getProperty("user.dir");
    public static Middleware middleware;
    public static String username;
    public static String TAG_ARQUIVO = "arquivo";
    public static String TAG_PASTA = "pasta";
    public static Document xml;
}
