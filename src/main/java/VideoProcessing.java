
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

public class VideoProcessing {

    /* Carrega a biblioteca nativa (via nu.pattern.OpenCV) assim que a classe é carregada na VM. */
    static {
        nu.pattern.OpenCV.loadLocally();
    }


    public static byte[][][] carregarVideo(String caminho) {

        VideoCapture captura = new VideoCapture(caminho);
        if (!captura.isOpened()) {
            System.out.println("Vídeo está sendo processado por outra aplicação");
        }

        //tamanho do frame
        int largura = (int) captura.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int altura = (int) captura.get(Videoio.CAP_PROP_FRAME_HEIGHT);

        //não conheço a quantidade dos frames (melhorar com outra lib) :(
        List<byte[][]> frames = new ArrayList<>();

        //matriz RGB mesmo preto e branco?? - uso na leitura do frame
        Mat matrizRGB = new Mat();

        //criando uma matriz temporária em escala de cinza
        Mat escalaCinza = new Mat(altura, largura, CvType.CV_8UC1); //1 única escala
        byte linha[] = new byte[largura];

        while (captura.read(matrizRGB)) {//leitura até o último frames

            //convertemos o frame atual para escala de cinza
            Imgproc.cvtColor(matrizRGB, escalaCinza, Imgproc.COLOR_BGR2GRAY);

            //criamos uma matriz para armazenar o valor de cada pixel (int estouro de memória)
            byte pixels[][] = new byte[altura][largura];
            for (int y = 0; y < altura; y++) {
                escalaCinza.get(y, 0, linha);
                for (int x = 0; x < largura; x++) {
                    pixels[y][x] = (byte) (linha[x] & 0xFF); //shift de correção - unsig
                }
            }
            frames.add(pixels);
        }
        captura.release();

        /* converte o array de frames em matriz 3D */
        byte cuboPixels[][][] = new byte[frames.size()][][];
        for (int i = 0; i < frames.size(); i++) {
            cuboPixels[i] = frames.get(i);
        }

        return cuboPixels;
    }

    public static void gravarVideo(byte pixels[][][],
                                   String caminho,
                                   double fps) {

        int qFrames = pixels.length;
        int altura = pixels[0].length;
        int largura = pixels[0][0].length;

        int fourcc = VideoWriter.fourcc('a', 'v', 'c', '1');   // identificação codec .mp4
        VideoWriter escritor = new VideoWriter(
                caminho, fourcc, fps, new Size(largura, altura), true);

        if (!escritor.isOpened()) {
            System.err.println("Erro ao gravar vídeo no caminho sugerido");
        }

        Mat matrizRgb = new Mat(altura, largura, CvType.CV_8UC3); //voltamos a operar no RGB (limitação da lib)

        byte linha[] = new byte[largura * 3];                // BGR intercalado

        for (int f = 0; f < qFrames; f++) {
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    byte g = (byte) pixels[f][y][x];
                    int i = x * 3;
                    linha[i] = linha[i + 1] = linha[i + 2] = g;     // cinza → B,G,R
                }
                matrizRgb.put(y, 0, linha);
            }
            escritor.write(matrizRgb);
        }
        escritor.release(); //limpando o buffer 
    }

    public static void main(String[] args) {

        String caminhoVideo = "C:\\Users\\paola\\OneDrive\\Desktop\\video3.mp4";
        String caminhoGravar = "C:\\Users\\paola\\OneDrive\\Desktop\\video2.mp4";
        double fps = 24.0; //isso deve mudar se for outro vídeo (avaliar metadados ???)

        System.out.println("Carregando o vídeo... " + caminhoVideo);
        byte pixels[][][] = carregarVideo(caminhoVideo);

        System.out.printf("Frames: %d   Resolução: %d x %d \n",
                pixels.length, pixels[0][0].length, pixels[0].length);

        System.out.println("processamento remove ruído 1");
        long startTime = System.currentTimeMillis();
        removerSalPimenta(pixels); //voce deve implementar esta funcao

        System.out.println("processamento remove ruído 2");
        //removerBorroesTempo(pixels); //voce deve implementar esta funcao

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;

        System.out.println("Salvando...  " + caminhoGravar);
        gravarVideo(pixels, caminhoGravar, fps);

        System.out.println("Término do processamento");
        System.out.println("Tempo de execução: " + durationSeconds + " segundos");
    }

    private static void removerBorroesTempo(byte[][][] pixels) {
        //remover borrões entre frames

    }

    private static void removerSalPimenta(byte[][][] pixels) {
        int numThreads = 4; //0 para sequencial, 2/4/8/16 para paralelo
        int qFrames = pixels.length;

        //verifica se será sequencial ou paralelo
        if (numThreads > 0) {
            //calcula o número de frames para cada thread
            int framesPorThreads = qFrames / numThreads;

            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                //calcula o índice inicial do intervalo de frames que a thread irá processar
                int inicio = i * framesPorThreads;
                // Calcula o índice final do intervalo de frames para a thread i.
                // Se for a última thread, processa até o último frame (qFrames),
                // caso contrário, processa até inicio + framesPorThreads
                int fim = (i == numThreads - 1) ? qFrames : inicio + framesPorThreads;

                //cria uma nova thread do tipo FiltroThreads, que processará os frames
                threads[i] = new FiltroThreads(pixels, inicio, fim);
                threads[i].start();
            }
            //para cada thread criada, espera a sua finalização para garantir que
            //todas as threads terminem o processamento antes de salvar o arquivo
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            //chama filtro para lógica sequencial
            aplicarFiltroSalPimenta(pixels, 0, pixels.length);

        }
    }

    private static void aplicarFiltroSalPimenta(byte[][][] pixels, int inicio, int fim) {

        //percorre os frames
        for (int i = inicio; i < fim; i++) {
            //percorre as linhas de cada frame
            for (int y = 0; y < pixels[0].length; y++) {
                //percorre as colunas de cada frame
                for (int x = 0; x < pixels[0][0].length; x++) {
                    ArrayList<Integer> vizinhos = new ArrayList<>();

                    //percorrendo "vizinhos" do pixel
                    for (int vizinhosY = -1; vizinhosY <= 1; vizinhosY++) {
                        for (int vizinhoX = -1; vizinhoX <= 1; vizinhoX++) {
                            int coordenadaY = y + vizinhosY;
                            int coodenadaX = x + vizinhoX;

                            //acresentando ao array apenas os vizinhos válidos (que existam)
                            if (coordenadaY >= 0 && coordenadaY < pixels[0].length && coodenadaX >= 0 && coodenadaX < pixels[0][0].length) {
                                int valor = pixels[i][coordenadaY][coodenadaX] & 0xFF;
                                vizinhos.add(valor);
                            }
                        }
                    }

                    //calculando a média
                    int soma = 0;
                    for (int valor : vizinhos) {
                        soma += valor;
                    }
                    int media = soma / vizinhos.size();
                    //atualiza o pixel com valor da mediana
                    pixels[i][y][x] = (byte) media;
                }
            }
        }
    }
}

