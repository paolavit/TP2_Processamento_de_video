import java.util.ArrayList;
import java.util.List;

public class FiltroThreadsBorroes extends Thread{
    private byte[][][] cuboPixels;
    private int inicio;
    private int fim;
    private int vizinhosQtd;
    private double taxaVariacao;


    public FiltroThreadsBorroes(byte[][][] cuboPixels, int inicio, int fim ,int vizinhosQtd, double taxaVariacao){
        this.cuboPixels = cuboPixels;
        this.inicio = inicio;
        this.fim = fim;
        this.vizinhosQtd = vizinhosQtd;
        this.taxaVariacao = taxaVariacao;
    }

    @Override
    public void run() {
        for (int frame = inicio; frame < fim; frame++) {
            //navega nas linhas do frame
            for (int y = 0; y < cuboPixels[frame].length; y++) {
                //navega nas colunas do frame
                for (int x = 0; x < cuboPixels[frame][y].length; x++) {

                    int valorDoPixel = cuboPixels[frame][y][x] & 0xFF;
                    boolean precisaCorrigir = false;
                    List<Integer> vizinhos = new ArrayList<>();

                    // Coleta os vizinhos válidos com base no parâmetro vizinhosQtd
                    for (int frameVizinhoAtual = -vizinhosQtd; frameVizinhoAtual <= vizinhosQtd; frameVizinhoAtual++) {
                        //pula o proprio frame
                        if (frameVizinhoAtual == 0) continue;


                        int frameVizinhoIndex = frame + frameVizinhoAtual;
                        //verifica se o index esta dentro do intervalo
                        if (frameVizinhoIndex >= 0 && frameVizinhoIndex < cuboPixels.length) {
                            int val = cuboPixels[frameVizinhoIndex][y][x] & 0xFF;
                            vizinhos.add(val);

                            double dif = Math.abs(valorDoPixel - val) / 255.0;
                            if (dif > taxaVariacao) precisaCorrigir = true;
                        }
                    }

                    // corrige usando média
                    if (precisaCorrigir) {
                        //vizinhos.add(valorDoPixel);

                        int soma = 0;
                        for (int valor : vizinhos) {
                            soma += valor;
                        }

                        int media = soma / vizinhos.size();

                        // Atualiza o pixel atual
                        cuboPixels[frame][y][x] = (byte) media;
                    }
                }
            }
        }

    }
}
