package utils;

import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementa operações simples para manipulação do XML
 *
 * @author minoro
 */
public class ManipuladorXML {

    public boolean existeArquivo(String caminho, Document xml) throws XPathExpressionException {
        return existeArquivoPasta(caminho, false, xml);
    }

    public boolean existePasta(String caminho, Document xml) throws XPathExpressionException {
        return existeArquivoPasta(caminho, true, xml);
    }

    /**
     * Verifica a existência de um ARQUIVO ou PASTA no XML
     *
     * @param caminho String - caminho do arquivo a ser checado, com a extensão
     * no fim. Exemplo: "Pasta 1/Pasta 2/Pasta 3/ultima/senha_facebook.txt"
     * @return boolean - true caso exista o arquivo, falso caso não exista
     * @throws XPathExpressionException
     */
    private boolean existeArquivoPasta(String caminho, boolean pasta, Document xml) {
        String expressao = montaExpressao(caminho, pasta);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr;
        Object exprResult = null;
        try {
            expr = xpath.compile(expressao);
            exprResult = expr.evaluate(xml, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            return false;
        }
        NodeList node = (NodeList) exprResult;
        if (node.getLength() != 0) {
            return true;
        }
        return false;
    }

    /**
     * Monta uma expressão para arquivo o XPath compilar
     *
     * @param caminho String - caminho a ser gerado a expressao
     * @return String - retorna uma String representando a expressão do caminho
     * do parâmetro
     */
    public String montarExpressaoArquivo(String caminho) {
        return montaExpressao(caminho, false);
    }

    /**
     * Monta uma expressão para pasta o XPath compilar
     *
     * @param caminho String - caminho a ser gerado a expressao
     * @return String - retorna uma String representando a expressão do caminho
     * do parâmetro
     */
    public String montarExpressaoPasta(String caminho) {
        return montaExpressao(caminho, true);
    }

    /**
     * Monta uma expressão para o XPath compilar
     *
     * @param caminho String - caminho a ser gerado a expressao
     * @param pasta boolean - true caso a expressão deva ser montada com pasta
     * como último elemento, e false caso o último elemento seja um arquivo
     * @return String - retorna uma String representando a expressão do caminho
     * do parâmetro
     */
    private String montaExpressao(String caminho, boolean pasta) {
        String[] list = caminho.split("/");
        String expressao = "/raiz/";

        if (!caminho.contains("/")) {
            System.out.println("RAIZ selecionada!");
            return expressao.substring(0, expressao.length() - 1);
        }
        for (int i = 0; i < list.length; i++) {
            String string = list[i];
            if (pasta || i != list.length - 1) {
                expressao += "pasta[text()='" + string + "']/";
            } else {
                expressao += "arquivo[text()='" + string + "']";
            }
        }

        if (pasta) {
            expressao = expressao.substring(0, expressao.lastIndexOf("/"));
        }
        System.out.println("Expressão gerada: " + expressao + ".");
        return expressao;
    }

    /**
     * Dada uma expressão, retorna a última pasta dela. Utilizado para dar
     * appendChild.
     *
     * @param expressao String - expressao XML
     * @param xml - Document - xml para ser pesquisado
     * @return Node - o nó da última pasta da expressão
     * @throws XPathExpressionException
     */
    public Node pegaUltimaPasta(String expressao, Document xml)
            throws XPathExpressionException {
        expressao = expressao.substring(0, expressao.lastIndexOf("/"));
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() == 0) {
            throw new XPathExpressionException("Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            throw new XPathExpressionException("Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
        }
        return node.item(0);
    }

    /**
     *
     * @param expressao String - expressao XML
     * @param xml - Document - xml a ser pesquisado
     * @return Node - o ultimo nó da expressão
     * @throws XPathExpressionException
     */
    public Node pegaUltimoNode(String expressao, Document xml)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() == 0) {
            throw new XPathExpressionException("Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            throw new XPathExpressionException("Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
        }
        return node.item(0);
    }

    /**
     * Salva o xml do parâmetro no arquivo físico
     *
     * @param nomeUsuario String - nome do usuário para ser salvo o arquivo XML
     * @param xml Document - Objeto representando o arquivo xml
     * @return boolean - true caso salve o arquivo, false caso haja algum erro
     * @throws TransformerConfigurationException
     */
    public boolean salvarXML(Document xml, String nomeUsuario)
            throws TransformerConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xml);
        String caminho = PainelDeControle.PASTA_XML + nomeUsuario + ".xml";
        StreamResult result = new StreamResult(new File(caminho));
        transformer.transform(source, result);
        return true;
    }

    public static List<String> getNomesArquivosFisicos(Document xml) throws XPathExpressionException {
        List<String> retorno = new ArrayList();

        NodeList nodelist = xml.getElementsByTagName(PainelDeControle.TAG_ARQUIVO);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node no = nodelist.item(i);
            String nome = no.getAttributes().getNamedItem("nome").getNodeValue();
            System.out.println(nome);
            retorno.add(nome);
        }
        return retorno;
    }

    public String getNomeArquivoFisico(String caminho, Document xml) throws XPathExpressionException {
        Element ultimoNode = (Element) pegaUltimoNode(montarExpressaoArquivo(caminho), xml);
        return ultimoNode.getAttribute("nome");
    }
}
