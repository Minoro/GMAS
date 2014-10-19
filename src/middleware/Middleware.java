package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.List;

import utils.Mensagem;

/**
 * Classe que realiza a comunicação entre a aplicação do cliente e os servidores de arquivo.
 * 
 * A comunicação é realizada através de chamadas RMI.
 */

public class Middleware {
	/**
	 * Lista dos Servidores de arquivos que a aplicação esta utilizando 
	 */
	private List<InetAddress> servidoresArquivo;
	
	/**
	 * Construtor da Classe de Middleware. A partir do endereço de um grupo Multicast
	 * passado por parametro envia uma solicitação para todos os servidores de arquivos
	 * remotos e, armazena na lista de servidores os dois primeiros que responderem a solicitação.
	 *  
	 * @param multicastGroup Endereço do grupo de multicast
	 * @throws IOException
	 */
	
	//Adicionar arquivo de persistencia de servidores.
	public Middleware(String multicastGroup) throws IOException {
		servidoresArquivo = new LinkedList<>();
		
		InetAddress group = InetAddress.getByName(multicastGroup);
		MulticastSocket mSckt = new MulticastSocket(5678);
		String mensagem = ""+Mensagem.GET_FILE_SERVER_GROUP;
		byte [] m = mensagem.getBytes();
		DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
		mSckt.send(messageOut);
		//recebe a confirmação dos 2 primeiros servidores de arquivo
		for(int i = 0; i < 2; i++) {
			while(true) {
				byte[] buffer = new byte[5];
				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
				mSckt.receive(messageIn);
				if(messageIn.getLength() > 0) {
					//recupera endereco do servidor
					servidoresArquivo.add(messageIn.getAddress());
					break;
				}
			}
		}
		mSckt.close();
	}
}
