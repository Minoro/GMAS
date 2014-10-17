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

}
