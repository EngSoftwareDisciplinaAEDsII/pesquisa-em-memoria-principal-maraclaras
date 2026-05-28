import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class BuscaAleatoria<T> implements IBuscador<T> {
	private long comparacoes;
	private LocalDateTime inicio;
	private LocalDateTime fim;
	private T[] dados;
	private Random random;

	public BuscaAleatoria(T[] dados) {
		this.dados = dados;
		this.random = new Random();
	}

	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
		if (inicio == null)
			throw new IllegalStateException("Não foi feita nenhuma busca.");
		return Duration.between(inicio, fim).toNanos();
	}

	@Override
	public T buscar(T dado) {
		comparacoes = 0;
		inicio = LocalDateTime.now();
		if (dados == null || dados.length == 0) {
			fim = LocalDateTime.now();
			return null;
		}

		int[] indices = new int[dados.length];
		for (int i = 0; i < dados.length; i++) {
			indices[i] = i;
		}

		// Embaralha os índices para realizar busca aleatória sem repetição
		for (int i = dados.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			int temp = indices[i];
			indices[i] = indices[j];
			indices[j] = temp;
		}

		T encontrado = null;
		for (int pos : indices) {
			comparacoes++;
			if (dados[pos].equals(dado)) {
				encontrado = dados[pos];
				break;
			}
		}
		fim = LocalDateTime.now();
		return encontrado;
	}
}
    
