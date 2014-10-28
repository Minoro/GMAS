package servidor;

import model.Arquivo;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

/**
 * Interface para operações basicas sobre arquivos
 *
 */
public interface SistemaArquivoInterface extends Remote {

    /**
     * Cria um arquivo passando o caminho do arquivo, separado por barra, onde o
     * ultimo nome, após a ultima barra, corresponde ao nome do arquivo
     *
     * @param caminho String - caminho onde será criado o arquivo, concatenado
     * com "/" e o nome do arquivo
     * @return boolean - true caso seja possivel criar o arquivo ou false caso
     * já exista um arquivo com o nome
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     *
     */
    boolean criarArquivo(String caminho) throws RemoteException, XPathExpressionException;

    /**
     * Deleta um arquivo passando o caminho do arquivo, separado por barra, onde
     * o ultimo nome, após a ultima barra, corresponde ao nome do arquivo
     *
     * @param caminho String - caminho do arquivo a ser deletado, concatenado
     * com "/" e o nome do arquivo
     * @return boolean - true caso seja possivel deletar o arquivo ou false caso
     * não seja possível
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean deletarArquivo(String caminho) throws RemoteException, XPathExpressionException;

    /**
     * Renomeia um arquivo passando o caminho de origem do arquivo, separado por
     * barra, onde o ultimo nome, após a ultima barra, corresponde ao nome do
     * arquivo. O caminho de destino corresponde ao caminho do arquivo, separado
     * por barra, onde o ultimo nome é o novo nome do arquivo
     *
     * @param caminhoOrigem String - caminho do arquivo a ser renomeado,
     * concatenado com "/" e o nome do arquivo
     * @param caminhoDestino String - caminho de destino o arquivo renomeado,
     * concatenado com "/" e e novo nome do arquivo
     * @return boolean - true caso seja possivel renomear o arquivo ou false
     * caso não seja possível
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean renomearArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException, XPathExpressionException;

    /**
     * Move um arquivo, dado um caminho de origem, onde o ultimo nome após a "/"
     * é o nome do arquivo, para o caminho de destino
     *
     * @param caminhoOrigem String - caminho de origem do arquivo a ser movido,
     * concatenado com "/" e o nome do arquivo
     * @param caminhoDestino - caminho de destino do arquivo.
     * @return boolean - true caso seja possivel mover o arquivo ou false caso
     * não seja possível
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean moverArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException, XPathExpressionException;

    /**
     * Copia um arquivo, dado um caminho de origem, onde o ultimo nome após a
     * "/" é o nome do arquivo, para o caminho de destino
     *
     * @param caminhoOrigem String - caminho de origem do arquivo a ser copiado,
     * concatenado com "/" e o nome do arquivo
     * @param caminhoDestino String - caminho de destino do arquivo
     * @return boolean - true caso seja possivel mover o arquivo ou false caso
     * não seja possível
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean copiarArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException, XPathExpressionException;

    /**
     * Lê o conteudo do arquivo ao final do caminho
     *
     * @param caminho String - caminho para o arquivo
     * @return String - retorna o conteudo do arquivo em forma de texto
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    String lerArquivo(String caminho) throws RemoteException, XPathExpressionException;

    /**
     * Escreve no final do arquivo indicado ao final do caminho
     *
     * @param caminho String - caminho para o arquivo
     * @param texto String - texto a ser escrito no final do arquivo
     * @return boolean - retorna true caso o arquivo seja alterado com sucesso,
     * caso contrário retorna falso
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean escreverArquivo(String caminho, String texto) throws RemoteException, XPathExpressionException;

    /**
     * Escreve na posição indicada no arquivo ao final do caminho
     *
     * @param caminho String - caminho para o arquivo
     * @param texto String - texto a ser escrito na posição indicada
     * @param posicao int - posicao a se inserir o texto
     * @return boolean - retorna true caso o arquivo seja alterado com sucesso,
     * caso contrário retorna falso
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    boolean escreverArquivo(String caminho, String texto, int posicao) throws RemoteException, XPathExpressionException;

    /**
     * Retorna um objeto com as informações referentes ao arquivo no final do
     * caminho
     *
     * @param caminho String - caminho para o arquivo que se deseja as
     * informações
     * @return Arquivo - retorna um objeto com as informações referentes ao
     * arquivo
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    Arquivo getAtributes(String caminho) throws RemoteException, XPathExpressionException;

    /**
     * Altera os atributos referentes ao arquivo
     *
     * @param caminho String - caminho para o arquivo que se deseja alterar as
     * informações
     * @param arquivo Arquivo - Objeto com as informações a serem atualizadas
     * @throws RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    void setAtributes(String caminho, Arquivo arquivo) throws RemoteException, XPathExpressionException;

    /**
     * Salva o xml do parâmetro no arquivo físico
     *
     * @param nomeUsuario String - nome do usuário para ser salvo o arquivo XML
     * @param xml Document - Objeto representando o arquivo xml
     * @return boolean - true caso salve o arquivo, false caso haja algum erro
     * @throws RemoteException
     * @throws TransformerConfigurationException
     */
    public boolean salvaXML(Document xml, String nomeUsuario) throws RemoteException, TransformerConfigurationException, TransformerException;

    Document pedirXML(String nomeUsuario) throws RemoteException, XPathExpressionException;
}
