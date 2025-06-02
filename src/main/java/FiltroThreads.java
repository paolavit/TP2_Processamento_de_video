import java.util.ArrayList;
import java.util.Collections;

public class FiltroThreads extends Thread{
    private byte[][][] pixels;
    private int inicio;
    private int fim;

    public FiltroThreads(byte[][][] pixels, int inicio, int fim ){
        this.pixels = pixels;
        this.inicio = inicio;
        this.fim = fim;
    }

    @Override
    public void run() {
        //percorre os frames
        for(int i = inicio; i<fim; i++) {
            //percorre as linhas de cada frame
            for (int y = 0; y < pixels[0].length; y++) {
                //percorre as colunas de cada frame
                for (int x = 0; x < pixels[0][0].length; x++) {
                    ArrayList<Integer> vizinhos = new ArrayList<>();

                    //percorrendo "vizinhos" do pixel
                    for (int visinhosY = -1; visinhosY <= 1; visinhosY++) {
                        for (int visinhoX = -1; visinhoX <= 1; visinhoX++) {
                            int coordenadaY = y + visinhosY;
                            int coodenadaX = x + visinhoX;

                            //acresentando ao array apenas os vizinhos válidos (que existam)
                            if (coordenadaY >= 0 && coordenadaY < pixels[0].length && coodenadaX >= 0 && coodenadaX < pixels[0][0].length) {
                                int valor = pixels[i][coordenadaY][coodenadaX] & 0xFF;
                                vizinhos.add(valor);
                            }
                        }
                    }

                    //calculando a mediana
                    //ordenando array
                    Collections.sort(vizinhos);
                    //pega valor que esta no indiceMediana
                    int mediana = vizinhos.get(vizinhos.size() / 2);
                    //atualiza o pixel com valor da mediana
                    pixels[i][y][x] = (byte) mediana;
                }
            }
        }

    }
}
