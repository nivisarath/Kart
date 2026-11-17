import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Ruteplanlegger {
    static class KoeElement implements Comparable<KoeElement> {
        int node;
        long tid;
        long prioritet;

        // Brukes av Dijkstra og landemerkeberegningen.
        KoeElement(int node, long tid) {
            this(node, tid, tid);
        }

        // Brukes av ALT.
        KoeElement(int node, long tid, long prioritet) {
            this.node = node;
            this.tid = tid;
            this.prioritet = prioritet;
        }

        @Override
        public int compareTo(KoeElement annen) {
            return Long.compare(this.prioritet, annen.prioritet);
        }
    }

    static long[] beregnAvstander(
            int start,
            int[] forste,
            int[] neste,
            int[] endepunkt,
            int[] reisetid) {

        long[] avstand = new long[forste.length];
        Arrays.fill(avstand, Long.MAX_VALUE);
        avstand[start] = 0;

        PriorityQueue<KoeElement> koe = new PriorityQueue<>();
        koe.add(new KoeElement(start, 0));

        while (!koe.isEmpty()) {
            KoeElement aktuell = koe.poll();
            int node = aktuell.node;

            if (aktuell.tid != avstand[node]) {
                continue;
            }

            for (int kant = forste[node];
                 kant != -1;
                 kant = neste[kant]) {

                int nabo = endepunkt[kant];
                long nyTid = aktuell.tid + reisetid[kant];

                if (nyTid < avstand[nabo]) {
                    avstand[nabo] = nyTid;
                    koe.add(new KoeElement(nabo, nyTid));
                }
            }
        }

        return avstand;
    }

    static long anslaaResttid(
            int node,
            int maal,
            long[][] fraLandemerker,
            long[][] tilLandemerker) {

        long estimat = 0;

        for (int i = 0; i < fraLandemerker.length; i++) {
            long landemerkeTilMaal = fraLandemerker[i][maal];
            long landemerkeTilNode = fraLandemerker[i][node];

            if (landemerkeTilMaal != Long.MAX_VALUE
                    && landemerkeTilNode != Long.MAX_VALUE) {

                long forslag = landemerkeTilMaal - landemerkeTilNode;
                estimat = Math.max(estimat, forslag);
            }

            long nodeTilLandemerke = tilLandemerker[i][node];
            long maalTilLandemerke = tilLandemerker[i][maal];

            if (nodeTilLandemerke != Long.MAX_VALUE
                    && maalTilLandemerke != Long.MAX_VALUE) {

                long forslag = nodeTilLandemerke - maalTilLandemerke;
                estimat = Math.max(estimat, forslag);
            }
        }

        return estimat;
    }
    static ArrayList<Integer> finnInteressepunkter(
            int start,
            int type,
            int[] forsteKant,
            int[] nesteKant,
            int[] til,
            int[] reisetid,
            int[] interessekode,
            String[] stedsnavn) {

        long[] avstand = new long[forsteKant.length];
        Arrays.fill(avstand, Long.MAX_VALUE);
        avstand[start] = 0;

        ArrayList<Integer> funnet = new ArrayList<>();
        PriorityQueue<KoeElement> koe = new PriorityQueue<>();
        koe.add(new KoeElement(start, 0));

        while (!koe.isEmpty() && funnet.size() < 4) {
            KoeElement aktuell = koe.poll();
            int node = aktuell.node;

            if (aktuell.tid != avstand[node]) {
                continue;
            }

            if ((interessekode[node] & type) != 0) {
                funnet.add(node);

                System.out.println(stedsnavn[node]
                        + " | node " + node
                        + " | " + avstand[node] / 100.0
                        + " sekunder fra start");

                if (funnet.size() == 4) {
                    break;
                }
            }

            for (int kant = forsteKant[node];
                 kant != -1;
                 kant = nesteKant[kant]) {

                int nabo = til[kant];
                long nyTid = aktuell.tid + reisetid[kant];

                if (nyTid < avstand[nabo]) {
                    avstand[nabo] = nyTid;
                    koe.add(new KoeElement(nabo, nyTid));
                }
            }
        }

        return funnet;
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

                int[] forsteOmvendt = new int[antallNoder];
                int[] nesteOmvendt = new int[antallKanter];

                Arrays.fill(forsteKant, -1);
                Arrays.fill(forsteOmvendt, -1);

                for (int i = 0; i < antallKanter; i++) {
                    StringTokenizer felt =
                            new StringTokenizer(kantLeser.readLine());

                    fra[i] = Integer.parseInt(felt.nextToken());
                    til[i] = Integer.parseInt(felt.nextToken());
                    reisetid[i] = Integer.parseInt(felt.nextToken());

                    nesteKant[i] = forsteKant[fra[i]];
                    forsteKant[fra[i]] = i;

                    nesteOmvendt[i] = forsteOmvendt[til[i]];
                    forsteOmvendt[til[i]] = i;
                }

                System.out.println("Lest inn " + antallKanter + " kanter.");
                int[] interessekode = new int[antallNoder];
                String[] stedsnavn = new String[antallNoder];
                int antallInteressepunkter = 0;

                try (BufferedReader punktLeser = new BufferedReader(
                        new FileReader("Data/Norden/interessepkt.txt",
                                StandardCharsets.UTF_8))) {

                    String linje;

                    while ((linje = punktLeser.readLine()) != null) {
                        StringTokenizer felt = new StringTokenizer(linje);

                        // Hopp over tomme linjer og eventuell linje med bare antall.
                        if (felt.countTokens() < 2) {
                            continue;
                        }

                        int nummer = Integer.parseInt(felt.nextToken());
                        int kode = Integer.parseInt(felt.nextToken());

                        int forsteSitat = linje.indexOf('"');
                        int sisteSitat = linje.lastIndexOf('"');

                        String navn = "";

                        if (forsteSitat >= 0 && sisteSitat > forsteSitat) {
                            navn = linje.substring(forsteSitat + 1, sisteSitat);
                        }

                        interessekode[nummer] |= kode;
                        stedsnavn[nummer] = navn;
                        antallInteressepunkter++;
                    }
                }

                System.out.println("Lest inn "
                        + antallInteressepunkter + " interessepunkter.");
                System.out.println("\nFire ladestasjoner nær Odense:");

                ArrayList<Integer> ladestasjoner = finnInteressepunkter(
                        2763740, 4,
                        forsteKant, nesteKant, til, reisetid,
                        interessekode, stedsnavn);

                System.out.println("\nFire spise- eller drikkesteder nær Gløshaugen:");

                ArrayList<Integer> matsteder = finnInteressepunkter(
                        2001238, 24,
                        forsteKant, nesteKant, til, reisetid,
                        interessekode, stedsnavn);
                try (PrintWriter skriver = new PrintWriter("ladestasjoner.csv")) {
                    for (int nummer : ladestasjoner) {
                        skriver.println(breddegrader[nummer]
                                + "," + lengdegrader[nummer]);
                    }
                }

                try (PrintWriter skriver = new PrintWriter("matsteder.csv")) {
                    for (int nummer : matsteder) {
                        skriver.println(breddegrader[nummer]
                                + "," + lengdegrader[nummer]);
                    }
                }

                System.out.println("Interessepunktene er lagret i kartfiler.");
                int[] landemerker = {
                        2800567, 1853145, 647826, 3168086
                };

                long[][] fraLandemerker = new long[landemerker.length][];
                long[][] tilLandemerker = new long[landemerker.length][];
                if (new File("landemerker.bin").exists()) {
                    try (DataInputStream leserLandemerker = new DataInputStream(
                            new BufferedInputStream(
                                    new FileInputStream("landemerker.bin")))) {

                        int lagretAntallNoder = leserLandemerker.readInt();
                        int lagretAntallLandemerker = leserLandemerker.readInt();

                        if (lagretAntallNoder != antallNoder
                                || lagretAntallLandemerker != landemerker.length) {
                            throw new IOException(
                                    "Landemerkefilen passer ikke. Slett landemerker.bin og kjør igjen.");
                        }

                        for (int i = 0; i < landemerker.length; i++) {
                            int lagretLandemerke = leserLandemerker.readInt();

                            if (lagretLandemerke != landemerker[i]) {
                                throw new IOException(
                                        "Landemerkene er endret. Slett landemerker.bin og kjør igjen.");
                            }

                            fraLandemerker[i] = new long[antallNoder];
                            tilLandemerker[i] = new long[antallNoder];

                            for (int node = 0; node < antallNoder; node++) {
                                fraLandemerker[i][node] = leserLandemerker.readLong();
                                tilLandemerker[i][node] = leserLandemerker.readLong();
                            }
                        }
                    }

                    System.out.println("Landemerkene er lest fra fil.");
                } else {
                    for (int i = 0; i < landemerker.length; i++) {
                        System.out.println("Beregner landemerke "
                                + (i + 1) + " av " + landemerker.length);

                        fraLandemerker[i] = beregnAvstander(
                                landemerker[i],
                                forsteKant,
                                nesteKant,
                                til,
                                reisetid);

                        tilLandemerker[i] = beregnAvstander(
                                landemerker[i],
                                forsteOmvendt,
                                nesteOmvendt,
                                fra,
                                reisetid);
                    }

                    System.out.println("Alle landemerkene er ferdig beregnet.");
                    try (DataOutputStream skriver = new DataOutputStream(
                            new BufferedOutputStream(
                                    new FileOutputStream("landemerker.bin")))) {

                        skriver.writeInt(antallNoder);
                        skriver.writeInt(landemerker.length);

                        for (int i = 0; i < landemerker.length; i++) {
                            skriver.writeInt(landemerker[i]);

                            for (int node = 0; node < antallNoder; node++) {
                                skriver.writeLong(fraLandemerker[i][node]);
                                skriver.writeLong(tilLandemerker[i][node]);
                            }
                        }
                    }

                    System.out.println("Landemerkene er lagret i landemerker.bin.");
                }

                // Gløshaugen: 2001238, Otilienborg: 1987066, Odense: 2763740.
                // Endre maal for å velge rute.
                // brukALT = true bruker ALT, false bruker Dijkstra.
                    int start = 2001238;
                    int maal = 1987066;
                    boolean brukALT = true;

                long[] resttid = new long[antallNoder];
                Arrays.fill(resttid, -1);

                    long[] avstand = new long[antallNoder];
                    int[] forrige = new int[antallNoder];

                    Arrays.fill(avstand, Long.MAX_VALUE);
                    Arrays.fill(forrige, -1);

                    avstand[start] = 0;
                    PriorityQueue<KoeElement> koe = new PriorityQueue<>();
                resttid[start] = 0;

                if (brukALT) {
                    resttid[start] = anslaaResttid(
                            start, maal, fraLandemerker, tilLandemerker);
                }

                koe.add(new KoeElement(start, 0, resttid[start]));
                    int antallUndersokte = 0;
                int antallKoUttak = 0;
                    long startTid = System.nanoTime();

                    while (!koe.isEmpty()) {
                        KoeElement aktuell = koe.poll();
                        antallKoUttak++;
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
                                if (resttid[nabo] == -1) {
                                    resttid[nabo] = 0;

                                    if (brukALT) {
                                        resttid[nabo] = anslaaResttid(
                                                nabo, maal, fraLandemerker, tilLandemerker);
                                    }
                                }

                                koe.add(new KoeElement(
                                        nabo, nyTid, nyTid + resttid[nabo]));
                            }
                        }
                    }

                    long sluttTid = System.nanoTime();

                if (avstand[maal] == Long.MAX_VALUE) {
                    System.out.println("Fant ingen rute.");
                    return;
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
                System.out.println("Totalt antall køuttak: " + antallKoUttak);
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