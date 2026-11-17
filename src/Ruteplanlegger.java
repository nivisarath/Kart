import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class Ruteplanlegger {
    static class KoeElement implements Comparable<KoeElement> {
        int node;
        long tid;

        KoeElement(int node, long tid) {
            this.node = node;
            this.tid = tid;
        }

        @Override
        public int compareTo(KoeElement annen) {
            return Long.compare(this.tid, annen.tid);
        }
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader leser =
                     new BufferedReader(new FileReader("Data/Norden/noder.txt"))) {

            int antallNoder = Integer.parseInt(leser.readLine().trim());

            double[] breddegrader = new double[antallNoder];
            double[] lengdegrader = new double[antallNoder];

            for (int i = 0; i < antallNoder; i++) {
                StringTokenizer felt =
                        new StringTokenizer(leser.readLine());

                int nummer = Integer.parseInt(felt.nextToken());
                double breddegrad = Double.parseDouble(felt.nextToken());
                double lengdegrad = Double.parseDouble(felt.nextToken());

                breddegrader[nummer] = breddegrad;
                lengdegrader[nummer] = lengdegrad;
            }

            System.out.println("Lest inn " + antallNoder + " noder.");
            System.out.println("Node 0: "
                    + breddegrader[0] + ", " + lengdegrader[0]);

            try (BufferedReader kantLeser =
                         new BufferedReader(new FileReader("Data/Norden/kanter.txt"))) {

                int antallKanter =
                        Integer.parseInt(kantLeser.readLine().trim());

                int[] fra = new int[antallKanter];
                int[] til = new int[antallKanter];
                int[] reisetid = new int[antallKanter];

                int[] forsteKant = new int[antallNoder];
                int[] nesteKant = new int[antallKanter];

                Arrays.fill(forsteKant, -1);

                for (int i = 0; i < antallKanter; i++) {
                    StringTokenizer felt =
                            new StringTokenizer(kantLeser.readLine());

                    fra[i] = Integer.parseInt(felt.nextToken());
                    til[i] = Integer.parseInt(felt.nextToken());
                    reisetid[i] = Integer.parseInt(felt.nextToken());

                    nesteKant[i] = forsteKant[fra[i]];
                    forsteKant[fra[i]] = i;
                }

                System.out.println("Lest inn " + antallKanter + " kanter.");
                int start = 2001238;
                int maal = 2763740;

                long[] avstand = new long[antallNoder];
                int[] forrige = new int[antallNoder];

                Arrays.fill(avstand, Long.MAX_VALUE);
                Arrays.fill(forrige, -1);

                avstand[start] = 0;
                PriorityQueue<KoeElement> koe = new PriorityQueue<>();
                koe.add(new KoeElement(start, 0));
                int antallUndersokte = 0;
                long startTid = System.nanoTime();

                while (!koe.isEmpty()) {
                    KoeElement aktuell = koe.poll();
                    int node = aktuell.node;

                    // Hopp over gamle køelementer hvis vi har funnet en bedre tid.
                    if (aktuell.tid != avstand[node]) {
                        continue;
                    }

                    antallUndersokte++;

                    // Nå vet vi at raskeste vei til målet er funnet.
                    if (node == maal) {
                        break;
                    }

                    for (int kant = forsteKant[node];
                         kant != -1;
                         kant = nesteKant[kant]) {

                        int nabo = til[kant];
                        long nyTid = avstand[node] + reisetid[kant];

                        if (nyTid < avstand[nabo]) {
                            avstand[nabo] = nyTid;
                            forrige[nabo] = node;
                            koe.add(new KoeElement(nabo, nyTid));
                        }
                    }
                }

                long sluttTid = System.nanoTime();

                if (avstand[maal] == Long.MAX_VALUE) {
                    System.out.println("Fant ingen rute.");
                } else {
                    long sekunder = avstand[maal] / 100;
                    long timer = sekunder / 3600;
                    long minutter = (sekunder % 3600) / 60;
                    long restSekunder = sekunder % 60;

                    System.out.printf("Reisetid: %02d:%02d:%02d%n",
                            timer, minutter, restSekunder);
                }
                ArrayList<Integer> rute = new ArrayList<>();
                int node = maal;

                while (node != -1) {
                    rute.add(node);
                    node = forrige[node];
                }

                Collections.reverse(rute);

                try (PrintWriter skriver = new PrintWriter("rute.csv")) {
                    for (int nodenummer : rute) {
                        skriver.println(breddegrader[nodenummer]
                                + "," + lengdegrader[nodenummer]);
                    }
                }

                System.out.println("Ruten er lagret i rute.csv.");
                try (PrintWriter skriver = new PrintWriter("rute_kart.csv")) {
                    int antall = Math.min(1000, rute.size());

                    for (int i = 0; i < antall; i++) {
                        int indeks = 0;

                        if (antall > 1) {
                            indeks = (int) ((long) i * (rute.size() - 1)
                                    / (antall - 1));
                        }

                        int nodenummer = rute.get(indeks);

                        skriver.println(breddegrader[nodenummer]
                                + "," + lengdegrader[nodenummer]);
                    }
                }

                System.out.println("Undersøkte noder: " + antallUndersokte);
                System.out.println("Søket tok: "
                        + (sluttTid - startTid) / 1_000_000.0 + " ms.");
                System.out.println("Veier ut fra node 0:");

                for (int kant = forsteKant[0];
                     kant != -1;
                     kant = nesteKant[kant]) {

                    System.out.println("Til node " + til[kant]
                            + ", tid: " + reisetid[kant]
                            + " hundredels sekunder.");
                }
            }
        }
    }
}