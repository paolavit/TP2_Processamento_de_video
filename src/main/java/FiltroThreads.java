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
        VideoProcessing.aplicarFiltroSalPimenta(pixels, inicio, fim);
    }
}
