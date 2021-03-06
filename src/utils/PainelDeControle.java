package utils;

import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import middleware.Middleware;
import org.w3c.dom.Document;
import servidor.SistemaArquivo;

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
    public static final String USUARIOS_ARMAZENADOS = "2";
    public static final String FACA_BACKUP = "3";
    public static final String CONFIRMACAO_BACKUP = "4";
    public static final String FALHA_SERVIDOR = "5";
    public static final String EU_ESCOLHO_VOCE = "6";
    public static final int TAMANHO_BUFFER = 500;
    public static final int PORTA_MULTICAST = 5678;
    public static final int PORTA_SERVIDORES = 5679;
    public static final int PORTA_ERROS = 4666;
    public static final String IP_MULTICAST = "228.5.6.7";
    public static final String SEPARADOR = File.separator;
    public static final String HOME = System.getProperty("user.dir");
    public static String TAG_ARQUIVO = "arquivo";
    public static String TAG_PASTA = "pasta";
    public static String TAG_RAIZ = "raiz";
    public static String TAG_DESTRAVADO = "0";
    public static String TAG_TRAVADO = "1";
    public static final String PASTA_RAIZ = HOME + SEPARADOR + "raiz";
    public static String PASTA_XML = PASTA_RAIZ + SEPARADOR + "xml" + SEPARADOR;
    public static String PASTA_ICONES = PASTA_RAIZ + SEPARADOR + "icones" + SEPARADOR;
    public static final int deltaTRespostaServidor = calibrarRede();
    public static final int deltaTRespostaMulticast = 3 * deltaTRespostaServidor;
    public static Middleware middleware;
    public static String username;
    public static Document xml;
    public static final String MENSAGEM_HEARTBEAT = "<3";
    public static final int PORTA_HEARTBEAT = 5555;
    public static final int PORTA_RESOLUCAO_FALHA = 5556;
    public static final String MENSAGEM_CONFIRMACAO = "OK";
    
    
    /**
     * Utilizada para testes locais.
     * @return Retorna uma instância de SistemaDeArquivos
     */
    public static SistemaArquivo getTeste() {
        try {
            return new SistemaArquivo("-----------------------------------RODANDO TESTE-----------------------------------");
        } catch (RemoteException ex) {
            Logger.getLogger(PainelDeControle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Método que define um valor delta de tempo para aceitação de respostas ou
     * heartbeats no sistema de arquivos
     *
     * @return Valor de calibração da rede
     */
    public static int calibrarRede() {
        //TODO
        return 2;
    }

}
