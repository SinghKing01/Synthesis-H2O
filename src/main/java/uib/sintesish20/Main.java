package uib.sintesish20;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    static final int NHIDROGENS = 4;
    static final int NOXIGENS = 2;

    static Semaphore notComplet[] = new Semaphore[NOXIGENS]; //Bloquea al Oxigeno 
    static Semaphore semH[] = new Semaphore[NHIDROGENS];
    static Semaphore lock = new Semaphore(1);

    static Oxigen Ox0 = new Oxigen(0); //id = 0
    static Oxigen Ox1 = new Oxigen(1); // id = 1

    public static void main(String[] args) throws InterruptedException {

        Thread hidrogen[] = new Thread[NHIDROGENS];
        Thread oxigen[] = new Thread[NOXIGENS];

        init();

        for (int i = 0; i < NOXIGENS; i++) {
            if (i == 0) {
                oxigen[i] = new Thread(Ox0);
            } else {
                oxigen[i] = new Thread(Ox1);
            }
            oxigen[i].start();
        }
        for (int i = 0; i < NHIDROGENS; i++) {
            hidrogen[i] = new Thread(new Hidrogen(i));
            hidrogen[i].start();
        }
        for (int i = 0; i < NOXIGENS; i++) {
            oxigen[i].join();
        }
        for (int i = 0; i < NHIDROGENS; i++) {
            hidrogen[i].join();
        }
    }

    public static class Oxigen implements Runnable {

        int id;
        int contador = 0;
        int h0 = 0, h1 = 0;

        public Oxigen(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                lock.acquire();
                System.out.println("Aquest és l'Oxigen " + id);
                if (lock.availablePermits() == 0) {
                    lock.release(4);
                }
                for (int x = 0; x < 4; x++) {
                    notComplet[id].acquire();
                    System.out.println("----------->L'oxigen " + id + " sintetitza aigua");
                    for (int i = 0; i < 4; i++) {
                        if (id == 0) {
                            System.out.print("*");
                        } else {
                            System.out.print("+");
                        }
                        Thread.sleep(5);
                    }
                    semH[h0].release();
                    semH[h1].release();
                    contador = 0;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static class Hidrogen implements Runnable {

        int id;
        Oxigen Ox;

        public Hidrogen(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                lock.acquire();
                System.out.println("\tAquest és l'Hidrogen " + id);
                if (lock.availablePermits() == 0) {
                    lock.release(1);
                }
                for (int y = 0; y < 4; y++) {
                    semH[id].acquire();
                    lock.acquire();
                    Ox = getMin();
                    if(Ox.contador == 0){
                        Ox.h0 = id;
                        System.out.println("\tL'Hidrogen senar " + id + " espera per un altre hidrogen per completar Oxigen " + Ox.id);
                    }else{
                        Ox.h1 = id;
                        System.out.println("\tL'Hidrogen parell " + id + " allibera un oxigen per fer aigua, ha completat Oxigen " + Ox.id);
                    }
                    Ox.contador++;
                    lock.release();
                    if (Ox.contador == NHIDROGENS) {
                        notComplet[Ox.id].release();
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Oxigen getMin() {
        Oxigen aim = Ox0;
        if (Ox0.contador < NHIDROGENS) {
            aim = Ox0;
        } else if (Ox1.contador < NHIDROGENS) {
            aim = Ox1;
        }
        return aim;
    }

    public static void init() {
        for (int i = 0; i < NOXIGENS; i++) {
            notComplet[i] = new Semaphore(0);
        }
        for (int i = 0; i < NHIDROGENS; i++) {
            semH[i] = new Semaphore(1);
        }
    }
}
