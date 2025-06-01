public class FiltroThreads extends Thread{

    @Override
    public void run() {


        System.out.println(Thread.currentThread().getName());
    }
}
