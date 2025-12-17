import java.io.File;
import java.io.IOException;
import java.util.List;

public class TirageController {
    private final TirageModel model;

    public TirageController(TirageModel model) {
        this.model = model;
    }

    public TirageModel.ModeTirage getModeActuel() {
        return model.getModeActuel();
    }

    public void chargerCSV(File fichier) throws IOException {
        model.chargerCSV(fichier);
    }

    public TirageModel.TirageResult tirerAuSort(double poids) {
        if (model.getModeActuel() == TirageModel.ModeTirage.NOTES) {
            return model.tirerNotes(poids);
        }
        return model.tirerTickets();
    }

    public void remettreIndividuel() {
        model.remettreIndividuel();
    }

    public void remettreToutes() {
        model.remettreToutes();
    }

    public List<Eleve> getEleves() {
        return model.getEleves();
    }

    public List<Eleve> getElevesDejaChoisis() {
        return model.getElevesDejaChoisis();
    }

    public int getNombreElevesDisponibles() {
        return model.getNombreElevesDisponibles();
    }

    public int getTotalTicketsRestants() {
        return model.getTotalTicketsRestants();
    }

    public Eleve getDernierEleve() {
        return model.getDernierEleve();
    }
}
