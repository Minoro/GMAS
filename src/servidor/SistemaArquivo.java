<<<<<<< HEAD:src/servidor/SistemaArquivo.java
package servidor;

import model.Arquivo;
=======
//<<<<<<< HEAD:src/uel/so/sistema_arquivos/servidor/SistemaArquivo.java
package uel.so.sistema_arquivos.servidor;
//=======
import model.Arquivo;
//>>>>>>> 043899c9dc3ea723d98c5e5884b755b8c94587b4:src/SistemaArquivo.java
>>>>>>> f75662e9c66e6990676e5b942a84196e6a0f443f:src/uel/so/sistema_arquivos/servidor/SistemaArquivo.java
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;	


public class SistemaArquivo extends UnicastRemoteObject implements SistemaArquivoInterface {

	protected SistemaArquivo() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean criarArquivo(String caminho) throws RemoteException {
		// TODO Auto-generated method stub
		return false;	
	}

	@Override
	public boolean deletarArquivo(String caminho) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean renomearArquivo(String caminhoOrigem, String caminhoDestino)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moverArquivo(String caminhoOrigem, String caminhoDestino)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String lerArquivo(String caminho) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean escreverArquivo(String caminho, String texto)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean escreverArquivo(String caminho, String texto, int posicao)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Arquivo getAtributes(String caminho) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAtributes(String caminho, Arquivo arquivo)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean autenticarUsuario(String nome) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
