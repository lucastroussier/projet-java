import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TirageModel {
    public enum ModeTirage {
        NOTES, TICKETS
    }

    public static class TirageResult {
        private final Eleve gagnant;
        private final List<Eleve> listeAnimation;
        private final boolean modeNotes;

        public TirageResult(Eleve gagnant, List<Eleve> listeAnimation, boolean modeNotes) {
            this.gagnant = gagnant;
            this.listeAnimation = listeAnimation;
            this.modeNotes = modeNotes;
        }

        public Eleve getGagnant() {
            return gagnant;
        }

        public List<Eleve> getListeAnimation() {
            return listeAnimation;
        }

        public boolean isModeNotes() {
            return modeNotes;
        }
    }

    private ModeTirage modeActuel;
    private final List<Eleve> eleves;
    private final List<Eleve> elevesDejaChoisis;
    private Eleve dernierEleve;

    public TirageModel(ModeTirage modeActuel) {
        this.modeActuel = modeActuel;
        this.eleves = new ArrayList<>();
        this.elevesDejaChoisis = new ArrayList<>();
    }

    public ModeTirage getModeActuel() {
        return modeActuel;
    }

    public List<Eleve> getEleves() {
        return Collections.unmodifiableList(eleves);
    }

    public List<Eleve> getElevesDejaChoisis() {
        return Collections.unmodifiableList(elevesDejaChoisis);
    }

    public Eleve getDernierEleve() {
        return dernierEleve;
    }

    public int getNombreElevesDisponibles() {
        int count = 0;
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalTicketsRestants() {
        int total = 0;
        for (Eleve e : eleves) {
            total += e.getTicketsRestants();
        }
        return total;
    }

    public void changerMode(ModeTirage mode) {
        this.modeActuel = mode;
        remettreToutes();
    }

    public void chargerCSV(File fichier) throws IOException {
        eleves.clear();
        elevesDejaChoisis.clear();
        dernierEleve = null;

        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            boolean premiereLigne = true;

            while ((ligne = br.readLine()) != null) {
                if (premiereLigne) {
                    premiereLigne = false;
                    if (ligne.toLowerCase().contains("nom")) {
                        continue;
                    }
                }

                String[] parts = ligne.split("[,;]");
                if (modeActuel == ModeTirage.NOTES) {
                    if (parts.length >= 3) {
                        String nom = parts[0].trim();
                        String prenom = parts[1].trim();
                        try {
                            double note = Double.parseDouble(parts[2].trim());
                            if (!nom.isEmpty() && !prenom.isEmpty()) {
                                eleves.add(new Eleve(nom, prenom, note, 0));
                            }
                        } catch (NumberFormatException ignored) {
                            // Ignorer les lignes invalides
                        }
                    }
                } else {
                    if (parts.length >= 4) {
                        String nom = parts[0].trim();
                        String prenom = parts[1].trim();
                        try {
                            double note = Double.parseDouble(parts[2].trim());
                            int tickets = Integer.parseInt(parts[3].trim());
                            if (!nom.isEmpty() && !prenom.isEmpty()) {
                                eleves.add(new Eleve(nom, prenom, note, tickets));
                            }
                        } catch (NumberFormatException ignored) {
                            // Ignorer les lignes invalides
                        }
                    }
                }
            }
        }
    }

    public TirageResult tirerNotes(double poids) {
        List<Eleve> elevesDisponibles = new ArrayList<>();
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) {
                elevesDisponibles.add(e);
            }
        }

        if (elevesDisponibles.isEmpty()) {
            throw new IllegalStateException("Aucun élève disponible !");
        }

        double noteMax = elevesDisponibles.stream().mapToDouble(Eleve::getNote).max().orElse(20.0);
        List<Double> poidsList = new ArrayList<>();
        double sommesPoids = 0.0;

        for (Eleve e : elevesDisponibles) {
            double poidsEleve = Math.pow(noteMax - e.getNote() + 1, poids);
            poidsList.add(poidsEleve);
            sommesPoids += poidsEleve;
        }

        Random random = new Random();
        double valeur = random.nextDouble() * sommesPoids;
        double cumul = 0.0;
        Eleve gagnant = null;

        for (int i = 0; i < elevesDisponibles.size(); i++) {
            cumul += poidsList.get(i);
            if (valeur <= cumul) {
                gagnant = elevesDisponibles.get(i);
                break;
            }
        }

        if (gagnant == null) {
            gagnant = elevesDisponibles.get(elevesDisponibles.size() - 1);
        }

        gagnant.setDejaTire(true);
        elevesDejaChoisis.add(gagnant);
        dernierEleve = gagnant;

        return new TirageResult(gagnant, elevesDisponibles, true);
    }

    public TirageResult tirerTickets() {
        List<Eleve> listeAvecTickets = new ArrayList<>();
        for (Eleve e : eleves) {
            for (int i = 0; i < e.getTicketsRestants(); i++) {
                listeAvecTickets.add(e);
            }
        }

        if (listeAvecTickets.isEmpty()) {
            throw new IllegalStateException("Aucun ticket disponible !");
        }

        Random random = new Random();
        Eleve gagnant = listeAvecTickets.get(random.nextInt(listeAvecTickets.size()));

        gagnant.consommerTicket();
        if (!elevesDejaChoisis.contains(gagnant)) {
            elevesDejaChoisis.add(gagnant);
        }
        dernierEleve = gagnant;

        return new TirageResult(gagnant, listeAvecTickets, false);
    }

    public void remettreIndividuel() {
        if (dernierEleve != null) {
            if (modeActuel == ModeTirage.NOTES) {
                dernierEleve.setDejaTire(false);
                elevesDejaChoisis.remove(dernierEleve);
            } else {
                dernierEleve.resetTickets();
                dernierEleve.consommerTicket();
                int ticketsUtilises = dernierEleve.getTicketsInitiaux() - dernierEleve.getTicketsRestants();
                if (ticketsUtilises == 0) {
                    elevesDejaChoisis.remove(dernierEleve);
                }
            }
            dernierEleve = null;
        }
    }

    public void remettreToutes() {
        if (modeActuel == ModeTirage.NOTES) {
            for (Eleve e : eleves) {
                e.setDejaTire(false);
            }
        } else {
            for (Eleve e : eleves) {
                e.resetTickets();
            }
        }
        elevesDejaChoisis.clear();
        dernierEleve = null;
    }
}

class Eleve {
    private final String nom;
    private final String prenom;
    private final double note;
    private final int ticketsInitiaux;
    private int ticketsRestants;
    private boolean dejaTire;

    public Eleve(String nom, String prenom, double note, int tickets) {
        this.nom = nom;
        this.prenom = prenom;
        this.note = note;
        this.ticketsInitiaux = tickets;
        this.ticketsRestants = tickets;
        this.dejaTire = false;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public double getNote() {
        return note;
    }

    public int getTicketsInitiaux() {
        return ticketsInitiaux;
    }

    public int getTicketsRestants() {
        return ticketsRestants;
    }

    public boolean isDejaTire() {
        return dejaTire;
    }

    public void setDejaTire(boolean dejaTire) {
        this.dejaTire = dejaTire;
    }

    public void consommerTicket() {
        if (ticketsRestants > 0) {
            ticketsRestants--;
        }
    }

    public void resetTickets() {
        ticketsRestants = ticketsInitiaux;
    }

    public boolean aDesTickets() {
        return ticketsRestants > 0;
    }

    @Override
    public String toString() {
        return prenom + " " + nom;
    }
}
