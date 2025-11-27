import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

// ============= MODÈLE =============
class Eleve {
    private String nom;
    private String prenom;
    
    public Eleve(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }
    
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    
    @Override
    public String toString() {
        return prenom + " " + nom;
    }
}

class EleveModel {
    private List<Eleve> eleves;
    private List<ModelListener> listeners;
    
    public EleveModel() {
        eleves = new ArrayList<>();
        listeners = new ArrayList<>();
    }
    
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.onDataChanged();
        }
    }
    
    public void chargerCSV(File fichier) throws IOException {
        eleves.clear();
        BufferedReader br = new BufferedReader(new FileReader(fichier));
        String ligne;
        boolean premiereLigne = true;
        
        while ((ligne = br.readLine()) != null) {
            if (premiereLigne) {
                premiereLigne = false;
                if (ligne.toLowerCase().contains("nom") && ligne.toLowerCase().contains("prenom")) {
                    continue;
                }
            }
            
            String[] parts = ligne.split("[,;]");
            if (parts.length >= 2) {
                String nom = parts[0].trim();
                String prenom = parts[1].trim();
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    eleves.add(new Eleve(nom, prenom));
                }
            }
        }
        br.close();
        notifyListeners();
    }
    
    public Eleve tirerAuSort() {
        if (eleves.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return eleves.get(random.nextInt(eleves.size()));
    }
    
    public List<Eleve> getEleves() {
        return new ArrayList<>(eleves);
    }
    
    public int getNombreEleves() {
        return eleves.size();
    }
    
    public boolean isEmpty() {
        return eleves.isEmpty();
    }
}

interface ModelListener {
    void onDataChanged();
}

// ============= VUE =============
class EleveView extends JFrame implements ModelListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel resultatLabel;
    private JButton chargerButton, tirerButton, afficherButton;
    private EleveController controller;
    
    public EleveView() {
        setupUI();
    }
    
    public void setController(EleveController controller) {
        this.controller = controller;
    }
    
    private void setupUI() {
        setTitle("Tirage Aléatoire d'Élèves - MVC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        chargerButton = new JButton("Charger CSV");
        tirerButton = new JButton("Tirer au Sort");
        afficherButton = new JButton("Afficher Tous les Élèves");
        
        tirerButton.setEnabled(false);
        afficherButton.setEnabled(false);
        
        chargerButton.addActionListener(e -> controller.chargerCSV());
        tirerButton.addActionListener(e -> controller.tirerAuSort());
        afficherButton.addActionListener(e -> controller.afficherEleves());
        
        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
        buttonPanel.add(afficherButton);
        
        // Panel central avec le label résultat
        JPanel centerPanel = new JPanel(new BorderLayout());
        resultatLabel = new JLabel("Aucun tirage effectué", SwingConstants.CENTER);
        resultatLabel.setFont(new Font("Arial", Font.BOLD, 20));
        resultatLabel.setForeground(new Color(0, 102, 204));
        resultatLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        resultatLabel.setPreferredSize(new Dimension(600, 80));
        centerPanel.add(resultatLabel, BorderLayout.NORTH);
        
        // Table
        String[] colonnes = {"Nom", "Prénom"};
        tableModel = new DefaultTableModel(colonnes, 0);
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    public File choisirFichier() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            public String getDescription() {
                return "Fichiers CSV (*.csv)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    public void afficherMessage(String message, String titre, int type) {
        JOptionPane.showMessageDialog(this, message, titre, type);
    }
    
    public void afficherResultatTirage(String resultat) {
        resultatLabel.setText("🎲 Élève tiré au sort : " + resultat);
    }
    
    public void afficherListeEleves(List<Eleve> eleves) {
        tableModel.setRowCount(0);
        for (Eleve e : eleves) {
            tableModel.addRow(new Object[]{e.getNom(), e.getPrenom()});
        }
    }
    
    public void activerBoutons(boolean activer) {
        tirerButton.setEnabled(activer);
        afficherButton.setEnabled(activer);
    }
    
    @Override
    public void onDataChanged() {
        // Réagir aux changements du modèle
        activerBoutons(!controller.getModel().isEmpty());
    }
}

// ============= CONTRÔLEUR =============
class EleveController {
    private EleveModel model;
    private EleveView view;
    
    public EleveController(EleveModel model, EleveView view) {
        this.model = model;
        this.view = view;
        
        // Enregistrer la vue comme listener du modèle
        model.addListener(view);
    }
    
    public EleveModel getModel() {
        return model;
    }
    
    public void chargerCSV() {
        File fichier = view.choisirFichier();
        if (fichier != null) {
            try {
                model.chargerCSV(fichier);
                view.afficherMessage(
                    model.getNombreEleves() + " élève(s) chargé(s) avec succès !",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
                );
                view.activerBoutons(true);
            } catch (IOException ex) {
                view.afficherMessage(
                    "Erreur lors de la lecture du fichier : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    public void tirerAuSort() {
        Eleve eleveChoisi = model.tirerAuSort();
        if (eleveChoisi != null) {
            view.afficherResultatTirage(eleveChoisi.toString());
        } else {
            view.afficherMessage(
                "Aucun élève chargé !",
                "Erreur",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    public void afficherEleves() {
        view.afficherListeEleves(model.getEleves());
    }
}

// ============= MAIN =============
public class TirageElevesMVC {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Création du modèle
            EleveModel model = new EleveModel();
            
            // Création de la vue
            EleveView view = new EleveView();
            
            // Création du contrôleur
            EleveController controller = new EleveController(model, view);
            
            // Liaison vue-contrôleur
            view.setController(controller);
            
            // Affichage
            view.setVisible(true);
        });
    }
}
