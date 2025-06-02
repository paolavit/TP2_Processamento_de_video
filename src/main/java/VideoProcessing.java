
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
                    pixels[y][x] = (byte)(linha[x] & 0xFF); //shift de correção - unsig
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

        String caminhoVideo = "C:\\Users\\paola\\OneDrive\\Desktop\\video.mp4";
        String caminhoGravar = "C:\\Users\\paola\\OneDrive\\Desktop\\video2.mp4";
        double fps = 24.0; //isso deve mudar se for outro vídeo (avaliar metadados ???)

        System.out.println("Carregando o vídeo... " + caminhoVideo);
        byte pixels[][][] = carregarVideo(caminhoVideo);

        System.out.printf("Frames: %d   Resolução: %d x %d \n",
                pixels.length, pixels[0][0].length, pixels[0].length);

        System.out.println("processamento remove ruído 1");
        removerSalPimenta(pixels); //voce deve implementar esta funcao
        
        System.out.println("processamento remove ruído 2");
        //removerBorroesTempo(pixels); //voce deve implementar esta funcao
        
        System.out.println("Salvando...  " + caminhoGravar);
        gravarVideo(pixels, caminhoGravar, fps);
        System.out.println("Término do processamento");

    }

    private static void removerBorroesTempo(byte[][][] pixels) {
        //remover borrões entre frames

    }

    private static void removerSalPimenta(byte[][][] pixels) {
    //remover borrões do tipo "Sal e pimenta"

    int numThreads = 4; // 2,4,8,16,32
        //guarda a quantidade de frames
        int qFrames = pixels.length;
        //calcula quantidade de frames por threads
        int framesPorThreads = qFrames / numThreads;

        //instancia uma nova Thread
        Thread[] threads = new Thread[numThreads];


        for(int i = 0; i<numThreads; i++){
            //define o frame que a thread irá começar a percorrer
            int inicio = i*framesPorThreads;

            //verifica se é a última thread
            //se for, fim = quantidade de frames
            //senão, fim = inicio + quantidade de frames por thread (para processar somente sua faixa)
            int fim = (i==numThreads-1) ? qFrames : inicio + framesPorThreads;

            threads[i] = new FiltroThreads(pixels, inicio, fim);
            //iciando a thread
            threads[i].start();
        }

        //percorrendo todas as threads
        for(Thread t: threads){
          try{
              //espera uma thread terinar antes de passar para a próxima
              t.join();
          }catch (InterruptedException e){
              e.printStackTrace();
          }
        }






    }
}
