import model.Arquivo;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**	
 * Interface para operações basicas sobre arquivos
 * 
 */


public interface SistemaArquivoInterface extends Remote{
	
	/**
	 * Cria um arquivo passando o caminho do arquivo, separado por barra,
	 * 	onde o ultimo nome, após a ultima barra, corresponde ao nome do arquivo
	 * 
	 * @param caminho String - caminho onde será criado o arquivo, concatenado com "/" e o nome do arquivo
	 * @return boolean - true caso seja possivel criar o arquivo ou false caso já exista um arquivo
	 * 			com o nome 
	 * @throws RemoteException
	 * 
	 */
	boolean criarArquivo(String caminho) throws RemoteException;
	
	/**
	 * Deleta um arquivo passando o caminho do arquivo, separado por barra,
	 * 	onde o ultimo nome, após a ultima barra, corresponde ao nome do arquivo
	 * 
	 * @param caminho String - caminho do arquivo a ser deletado, concatenado com "/" e o nome do arquivo
	 * @return boolean - true caso seja possivel deletar o arquivo ou false caso não seja possível
	 * @throws RemoteException
	 */
	boolean deletarArquivo(String caminho) throws RemoteException;
	
	/**
	 *  Renomeia um arquivo passando o caminho de origem do arquivo, separado por barra,
	 * 	onde o ultimo nome, após a ultima barra, corresponde ao nome do arquivo. O caminho
	 * de destino corresponde ao caminho do arquivo, separado por barra, onde o ultimo nome
	 * é o novo nome do arquivo
	 * 
	 * @param caminhoOrigem String - caminho do arquivo a ser renomeado, concatenado com "/" e o nome do arquivo
	 * @param caminhoDestino String - caminho de destino o arquivo renomeado, concatenado com "/" e e novo nome
	 * 								 do arquivo
	 * @return boolean - true caso seja possivel renomear o arquivo ou false caso não seja possível
	 * @throws RemoteException
	 */
	boolean renomearArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException;
	
	
	/**
	 * Move um arquivo, dado um caminho de origem, onde o ultimo nome após a "/" é o
	 * nome do arquivo, para o caminho de destino
	 * 
	 * @param caminhoOrigem String - caminho de origem do arquivo a ser movido, concatenado com "/" e o nome do arquivo
	 * @param caminhoDestino - caminho de destino do arquivo.
	 * @return boolean - true caso seja possivel mover o arquivo ou false caso não seja possível
	 * @throws RemoteException
	 */
	boolean moverArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException;
	
	/**
	 * Copia um arquivo, dado um caminho de origem, onde o ultimo nome após a "/" é o 
	 * nome do arquivo, para o caminho de destino
	 * 
	 * @param caminhoOrigem String - caminho de origem do arquivo a ser copiado, concatenado com "/" e o nome do arquivo
	 * @param caminhoDestino String - caminho de destino do arquivo
	 * @return boolean - true caso seja possivel mover o arquivo ou false caso não seja possível
	 * @throws RemoteException
	 */
	boolean copiarArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException;
	
	/**
	 * Lê o conteudo do arquivo ao final do caminho
	 * 
	 * @param caminho String - caminho para o arquivo
	 * @return String - retorna o conteudo do arquivo em forma de texto
	 * @throws RemoteException
	 */
	String lerArquivo(String caminho) throws RemoteException;
	
	/**
	 * Escreve no final do arquivo indicado ao final do caminho
	 * 
	 * @param caminho String - caminho para o arquivo
	 * @param texto String - texto a ser escrito no final do arquivo 
	 * @return boolean - retorna true caso o arquivo seja alterado com sucesso, caso contrário retorna falso
	 * @throws RemoteException
	 */
	boolean escreverArquivo(String caminho, String texto) throws RemoteException;
	
	/**
	 * Escreve na posição indicada no arquivo ao final do caminho
	 * 
	 * @param caminho String - caminho para o arquivo
	 * @param texto String - texto a ser escrito na posição indicada
	 * @param posicao int - posicao a se inserir o texto
	 * @return boolean - retorna true caso o arquivo seja alterado com sucesso, caso contrário retorna falso
	 * @throws RemoteException
	 */
	boolean escreverArquivo(String caminho, String texto, int posicao) throws RemoteException;
	
	/**
	 * Retorna um objeto com as informações referentes ao arquivo no final do caminho
	 * 
	 * @param caminho String - caminho para o arquivo que se deseja as informações
	 * @return Arquivo - retorna um objeto com as informações referentes ao arquivo
	 * @throws RemoteException
	 */
	Arquivo getAtributes(String caminho) throws RemoteException;
	
	/**
	 * Altera os atributos referentes ao arquivo
	 * 
	 * @param caminho String - caminho para o arquivo que se deseja alterar as informações
	 * @param arquivo Arquivo - Objeto com as informações a serem atualizadas
	 * @throws RemoteException
	 */
	void setAtributes(String caminho, Arquivo arquivo) throws RemoteException;
	
	/**
	 * Autentica o usuário no servidor, retornando true caso o usuário seja autenticado corretamente
	 * ou false caso não seja possivel autenticar o usuário
	 * 
	 * @param nome String - nome do usuário a ser autenticado
	 * @throws RemoteException
	 */
	boolean autenticarUsuario(String nome) throws RemoteException;
	
	
}
