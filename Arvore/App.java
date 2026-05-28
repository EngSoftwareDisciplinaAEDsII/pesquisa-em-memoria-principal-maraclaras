import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * Perceba que poderia haver uma melhor modularização com a criação de uma classe Menu.
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Carregar produtos por nome/descrição");
        System.out.println("2 - Carregar produtos por id");
        System.out.println("3 - Procurar produto, por nome");
        System.out.println("4 - Procurar produto, por id");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extrairChave) {
    ABB<K, Produto> arvore = new ABB<>();
    int numProdutos = 0;
    String linha;
    Produto produto;
	
    try (Scanner arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"))) {
        numProdutos = Integer.parseInt(arquivo.nextLine().trim());
		
        for (int i = 0; i < numProdutos && arquivo.hasNextLine(); i++) {
            linha = arquivo.nextLine();
            produto = Produto.criarDoTexto(linha.trim());
            arvore.inserir(extrairChave.apply(produto), produto);
        }
        quantosProdutos = numProdutos;
    } catch (IOException | IllegalArgumentException excecaoArquivo) {
        quantosProdutos = 0;
        arvore = new ABB<>();
    }
	
    return arvore;
    }
    
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
    *  Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        if (produtosCadastrados == null) {
            System.out.println("A árvore de produtos por id não foi carregada.");
            return null;
        }
        Integer idProduto = lerOpcao("Digite o id do produto:", Integer.class);
        if (idProduto == null) {
            return null;
        }
		return localizarProduto(produtosCadastrados, idProduto);
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        if (produtosCadastrados == null) {
            System.out.println("A árvore de produtos por nome não foi carregada.");
            return null;
        }
        System.out.println("Digite o nome do produto:");
        String nome = teclado.nextLine();
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        String chave = nome.trim().toLowerCase();
		return localizarProduto(produtosCadastrados, chave);
    }

	static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
		Produto produto = null;
		try {
			produto = produtosCadastrados.pesquisar(procurado);
		} catch (NoSuchElementException ex) {
			// produto permanece null
		}
		System.out.printf("Comparações: %d\n", produtosCadastrados.getComparacoes());
		System.out.printf("Tempo gasto: %,.0f nanossegundos\n", produtosCadastrados.getTempo());
		return produto;
	}
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, produto -> produto.descricao.trim().toLowerCase());
                case 2 -> produtosCadastradosPorId = lerProdutos(nomeArquivoDados, produto -> produto.idProduto);
                case 3 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 4 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            }
            pausa();
        }while(opcao != 0);       

        teclado.close();    
    }
}
