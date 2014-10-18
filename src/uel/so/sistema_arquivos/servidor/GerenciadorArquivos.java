package uel.so.sistema_arquivos.servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


/**
 * Classe para manipular arquivos no servidor
 *
 */
public class GerenciadorArquivos {

	/**
	 * Cria um arquivo em disco caso não exista nenhum com o nome
	 * 
	 * @return String - retorna um nome aleatório referente ao arquivo em disco caso seja possível criar o arquivo
	 * 			caso contrário retorna uma string vazia
	 */
	public static String criarArquivo(){
		
		String randomName;
		UUID uuid = UUID.randomUUID();

		randomName = uuid.toString().substring(0, 32);
		
		File file = new File(System.getProperty("user.dir")+"/raiz/"+randomName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}
		
		return randomName;
	}
	
	/**
	 * Apaga um arquivo no disco do servidor
	 * 
	 * @param nome String - nome aleatório referente ao arquivo em disco
	 * @return boolean - retorna true caso seja possível apagar o arquivo,
	 * 					falso caso não seja possível
	 */
	public static boolean apagarArquivo(String nome){
		
		File file = new File(System.getProperty("user.dir")+"/raiz/"+nome);
		
		return file.delete();
		
	}
	
	/**
	 * Persiste o conteúdo de um objeto Arquivo em disco
	 * 
	 * @param arquivo Arquivo - objeto a ser persistido em disco
	 * @param nome String - nome aleátorio gerado quando o arquivo é criado
	 * @return boolean - retorna true caso seja possível salvar o arquivo ou 
	 * 			false caso contrário
	 */
	public static boolean salvarArquivo(Arquivo arquivo, String nome){
		
		
		try {
			File file = new File(System.getProperty("user.dir")+"/raiz/"+nome);
			FileWriter fw;
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(arquivo.toString());//salva as informações no arquivo no disco
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	/**
	 * Retorna um objeto Arquivo dado o nome aleatório de um arquivo persistido
	 * 
	 * @param nome String - nome aleatório do arquivo
	 * @return Arquivo - retorna o objeto com seus atributos
	 */
	public static Arquivo abrirArquivo(String nome){
		Arquivo arquivo = new Arquivo();
		try {
			File file = new File(System.getProperty("user.dir")+"/raiz/"+nome);
			FileReader fr = new FileReader(file.getAbsoluteFile());
			BufferedReader bufferedReader = new BufferedReader(fr);
			
			String atributo;
			atributo = bufferedReader.readLine();
			arquivo.setNome(atributo);

			atributo = bufferedReader.readLine();
			arquivo.setTamanho(Float.parseFloat(atributo));
			
			atributo = bufferedReader.readLine();
			arquivo.setProprietario(atributo);
			
			//passa para o formato de data
			atributo = bufferedReader.readLine();
			Calendar data = Calendar.getInstance();
			data.setTimeInMillis(Long.parseLong(atributo));
			arquivo.setDataCriacao(data.getTime());
	
			atributo = bufferedReader.readLine();
			data.setTimeInMillis(Long.parseLong(atributo));
			arquivo.setDataUltimaModificacao(data.getTime());
			
			String conteudo = bufferedReader.readLine();
			while(conteudo != null){
				atributo += "\n"+conteudo;
				conteudo = bufferedReader.readLine();
			}
			
			arquivo.setConteudo(atributo);
			
			bufferedReader.close();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		return arquivo;
	}
	
}
