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

        VideoProcessing.aplicarFiltroBorroesTempo(cuboPixels, vizinhosQtd, taxaVariacao, inicio, fim);

    }
}
