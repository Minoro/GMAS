package model;


import java.util.Calendar;
import java.util.Date;

/**
 * Classe que implementa operações básicas sobre um arquivo
 *
 * @author minoro
 *
 */
public class Arquivo {

    private String nome;
    private Integer identificador;
    private int posicao;
    private float tamanho;
    private String proprietario;
    private Date dataCriacao;
    private Date dataUltimaModificacao;
    private String conteudo;

    
    public Arquivo(){}//construtor vazio
    
    /**
     * Instancia um arquivo vazio
     *
     * @param nome - nome do arquivo a ser criado
     * @param proprietario - proprietario do arquivo
     */
    Arquivo(String nome, String proprietario) {
        this.nome = nome;
        this.proprietario = proprietario;

        this.posicao = 0;
        this.tamanho = 0;
        this.dataCriacao = Calendar.getInstance().getTime();
        this.dataUltimaModificacao = Calendar.getInstance().getTime();

    }

    void alterarArquivo() {
        this.tamanho = this.conteudo.length();
        this.dataUltimaModificacao = Calendar.getInstance().getTime();
    }
    
    @Override
	public String toString(){
		String string = nome+"\n"+tamanho+"\n"+proprietario+"\n"+dataCriacao.getTime()+"\n"+dataUltimaModificacao.getTime()+"\n"+conteudo;
		
		return string;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public float getTamanho() {
		return tamanho;
	}

	public void setTamanho(float tamanho) {
		this.tamanho = tamanho;
	}

	public String getProprietario() {
		return proprietario;
	}

	public void setProprietario(String proprietario) {
		this.proprietario = proprietario;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public Date getDataUltimaModificacao() {
		return dataUltimaModificacao;
	}

	public void setDataUltimaModificacao(Date dataUltimaModificacao) {
		this.dataUltimaModificacao = dataUltimaModificacao;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
		this.tamanho = conteudo.length();
	}

}