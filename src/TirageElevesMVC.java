import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

<<<<<<< HEAD
public class TirageElevesMVC extends JFrame {
    // Données
    private List<Eleve> eleves;
    private List<Eleve> elevesDejaChoisis;
    private Eleve dernierEleve;
    
    // Composants UI
=======
// ============= MODÈLE =============
class Eleve {
    private String nom;
    private String prenom;
    private double note;
    private boolean dejaTire;
    
    public Eleve(String nom, String prenom, double note) {
        this.nom = nom;
        this.prenom = prenom;
        this.note = note;
        this.dejaTire = false;
    }
    
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public double getNote() { return note; }
    public boolean isDejaTire() { return dejaTire; }
    public void setDejaTire(boolean dejaTire) { this.dejaTire = dejaTire; }
    
    @Override
    public String toString() {
        return prenom + " " + nom;
    }
}

class EleveModel {
    private List<Eleve> eleves;
    private List<Eleve> elevesDejaChoisis;
    private List<ModelListener> listeners;
    
    public EleveModel() {
        eleves = new ArrayList<>();
        elevesDejaChoisis = new ArrayList<>();
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
        elevesDejaChoisis.clear();
        BufferedReader br = new BufferedReader(new FileReader(fichier));
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
            if (parts.length >= 3) {
                String nom = parts[0].trim();
                String prenom = parts[1].trim();
                try {
                    double note = Double.parseDouble(parts[2].trim());
                    if (!nom.isEmpty() && !prenom.isEmpty()) {
                        eleves.add(new Eleve(nom, prenom, note));
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les lignes avec des notes invalides
                }
            }
        }
        br.close();
        notifyListeners();
    }
    
    public Eleve tirerAuSortPondere(double poids) {
        List<Eleve> elevesDisponibles = new ArrayList<>();
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) {
                elevesDisponibles.add(e);
            }
        }
        
        if (elevesDisponibles.isEmpty()) {
            return null;
        }
        
        // Calcul des poids : plus la note est basse, plus le poids est élevé
        double noteMax = elevesDisponibles.stream().mapToDouble(Eleve::getNote).max().orElse(20.0);
        List<Double> poidsList = new ArrayList<>();
        double sommesPoids = 0.0;
        
        for (Eleve e : elevesDisponibles) {
            // Formule : poids = (noteMax - note + 1) ^ facteurPoids
            double poidsEleve = Math.pow(noteMax - e.getNote() + 1, poids);
            poidsList.add(poidsEleve);
            sommesPoids += poidsEleve;
        }
        
        // Tirage pondéré
        Random random = new Random();
        double valeur = random.nextDouble() * sommesPoids;
        double cumul = 0.0;
        
        for (int i = 0; i < elevesDisponibles.size(); i++) {
            cumul += poidsList.get(i);
            if (valeur <= cumul) {
                Eleve eleveChoisi = elevesDisponibles.get(i);
                eleveChoisi.setDejaTire(true);
                elevesDejaChoisis.add(eleveChoisi);
                notifyListeners();
                return eleveChoisi;
            }
        }
        
        // Fallback (ne devrait pas arriver)
        Eleve eleveChoisi = elevesDisponibles.get(elevesDisponibles.size() - 1);
        eleveChoisi.setDejaTire(true);
        elevesDejaChoisis.add(eleveChoisi);
        notifyListeners();
        return eleveChoisi;
    }
    
    public void remettreEleve(Eleve eleve) {
        eleve.setDejaTire(false);
        elevesDejaChoisis.remove(eleve);
        notifyListeners();
    }
    
    public void remettreToutes() {
        for (Eleve e : eleves) {
            e.setDejaTire(false);
        }
        elevesDejaChoisis.clear();
        notifyListeners();
    }
    
    public List<Eleve> getEleves() {
        return new ArrayList<>(eleves);
    }
    
    public List<Eleve> getElevesDejaChoisis() {
        return new ArrayList<>(elevesDejaChoisis);
    }
    
    public int getNombreEleves() {
        return eleves.size();
    }
    
    public int getNombreElevesDisponibles() {
        int count = 0;
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) count++;
        }
        return count;
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
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
    private JTable tableTous;
    private JTable tableTires;
    private DefaultTableModel tableModelTous;
    private DefaultTableModel tableModelTires;
    private JPanel resultatPanel;
    private JLabel resultatLabel;
    private JButton chargerButton, tirerButton, afficherButton, remettreToutes;
    private JButton remettreIndividuelButton;
    private JSlider poidsSlider;
    private JLabel poidsLabel;
    private JLabel infoLabel;
<<<<<<< HEAD
    private javax.swing.Timer animationTimer;
    private List<Confetti> confettis;
    private javax.swing.Timer confettiTimer;
    
    // Couleurs
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SECONDARY_COLOR = new Color(139, 92, 246);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(234, 179, 8);
    private final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = Color.BLACK;
=======
    private EleveController controller;
    
    // Couleurs
// Couleurs — Thème Dark Mode moderne
private final Color PRIMARY_COLOR = new Color(99, 179, 237);     // Bleu pastel
private final Color SECONDARY_COLOR = new Color(167, 139, 250);  // Violet doux
private final Color SUCCESS_COLOR = new Color(74, 222, 128);     // Vert néon doux
private final Color WARNING_COLOR = new Color(251, 146,  60);  // Orange doux
private final Color BACKGROUND_COLOR = new Color(30, 41, 59);    // Bleu nuit
private final Color CARD_COLOR = new Color(51, 65, 85);          // Gris bleuté foncé
private final Color TEXT_COLOR = new Color(241, 245, 249);       // Gris clair presque blanc
private final Color TEXT_NOIR = new Color(51, 65, 85);       // Gris clair presque blanc


>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
    
    // Classe interne Eleve
    class Eleve {
        private String nom;
        private String prenom;
        private double note;
        private boolean dejaTire;
        
        public Eleve(String nom, String prenom, double note) {
            this.nom = nom;
            this.prenom = prenom;
            this.note = note;
            this.dejaTire = false;
        }
        
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public double getNote() { return note; }
        public boolean isDejaTire() { return dejaTire; }
        public void setDejaTire(boolean dejaTire) { this.dejaTire = dejaTire; }
        
        @Override
        public String toString() {
            return prenom + " " + nom;
        }
    }
    
    // Classe pour les confettis
    class Confetti {
        double x, y;
        double vx, vy;
        Color color;
        int size;
        
        public Confetti(int panelWidth, int panelHeight) {
            x = Math.random() * panelWidth;
            y = -10;
            vx = (Math.random() - 0.5) * 4;
            vy = Math.random() * 3 + 2;
            size = (int)(Math.random() * 8) + 4;
            
            Color[] colors = {
                new Color(255, 107, 107),
                new Color(78, 205, 196),
                new Color(255, 195, 18),
                new Color(199, 121, 208),
                new Color(106, 176, 255),
                new Color(255, 159, 243)
            };
            color = colors[(int)(Math.random() * colors.length)];
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += 0.1; // gravité
        }
        
        public boolean isOutOfBounds(int height) {
            return y > height;
        }
    }
    
    // Panel personnalisé pour les confettis
    class ConfettiPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (confettis != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                for (Confetti c : confettis) {
                    g2d.setColor(c.color);
                    g2d.fillOval((int)c.x, (int)c.y, c.size, c.size);
                }
            }
        }
    }
    
    public TirageElevesMVC() {
        eleves = new ArrayList<>();
        elevesDejaChoisis = new ArrayList<>();
        dernierEleve = null;
        confettis = new ArrayList<>();
        setupUI();
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(170, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void setupUI() {
        setTitle("Tirage Pondéré d'Élèves");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Tirage Pondéré d'Élèves", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel des boutons et contrôles
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        chargerButton = createStyledButton("Charger CSV", PRIMARY_COLOR);
        tirerButton = createStyledButton("Tirer au Sort", SECONDARY_COLOR);
        afficherButton = createStyledButton("Afficher Liste", SUCCESS_COLOR);
        remettreToutes = createStyledButton("Remettre Tous", WARNING_COLOR);
        
        tirerButton.setEnabled(false);
        afficherButton.setEnabled(false);
        remettreToutes.setEnabled(false);
        
<<<<<<< HEAD
        chargerButton.addActionListener(e -> chargerCSV());
        tirerButton.addActionListener(e -> tirerAuSort());
        afficherButton.addActionListener(e -> afficherEleves());
        remettreToutes.addActionListener(e -> remettreToutes());
        
        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
        buttonPanel.add(afficherButton);
=======
        chargerButton.addActionListener(e -> controller.chargerCSV());
        tirerButton.addActionListener(e -> controller.tirerAuSort());
        remettreToutes.addActionListener(e -> controller.remettreToutes());
        
        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        buttonPanel.add(remettreToutes);
        
        // Slider pour le poids
        JPanel poidsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        poidsPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel poidsLabelText = new JLabel("Facteur de pondération :");
        poidsLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        poidsLabelText.setForeground(TEXT_COLOR);
        
        poidsSlider = new JSlider(0, 50, 10);
        poidsSlider.setPreferredSize(new Dimension(300, 40));
        poidsSlider.setMajorTickSpacing(10);
        poidsSlider.setMinorTickSpacing(5);
        poidsSlider.setPaintTicks(true);
        poidsSlider.setPaintLabels(true);
        poidsSlider.setBackground(BACKGROUND_COLOR);
<<<<<<< HEAD
=======
        ((Hashtable<?, ?>) poidsSlider.getLabelTable()).forEach((k, v) -> {
        ((JLabel) v).setForeground(PRIMARY_COLOR);
        });
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        
        poidsLabel = new JLabel("1.0");
        poidsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        poidsLabel.setForeground(PRIMARY_COLOR);
<<<<<<< HEAD
        
=======
         
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        poidsSlider.addChangeListener(e -> {
            double valeur = poidsSlider.getValue() / 10.0;
            poidsLabel.setText(String.format("%.1f", valeur));
        });
        
        poidsPanel.add(poidsLabelText);
        poidsPanel.add(poidsSlider);
        poidsPanel.add(poidsLabel);
        
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(poidsPanel, BorderLayout.CENTER);
        
        // Info label
        infoLabel = new JLabel("Chargez un fichier CSV pour commencer", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(107, 114, 128));
        controlPanel.add(infoLabel, BorderLayout.SOUTH);
        
        headerPanel.add(controlPanel, BorderLayout.CENTER);
        
        // ===== PANNEAU RÉSULTAT =====
<<<<<<< HEAD
        resultatPanel = new ConfettiPanel();
        resultatPanel.setLayout(new BorderLayout(10, 0));
=======
        resultatPanel = new JPanel(new BorderLayout(10, 0));
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        resultatPanel.setBackground(new Color(243, 244, 246));
        resultatPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
<<<<<<< HEAD
        resultatPanel.setPreferredSize(new Dimension(1050, 120));
        
        resultatLabel = new JLabel("En attente du premier tirage", SwingConstants.CENTER);
        resultatLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        resultatLabel.setForeground(TEXT_COLOR);
        
        remettreIndividuelButton = createStyledButton("Remettre", SUCCESS_COLOR);
        remettreIndividuelButton.setVisible(false);
        remettreIndividuelButton.addActionListener(e -> remettreIndividuel());
        
        resultatPanel.add(resultatLabel, BorderLayout.CENTER);
        resultatPanel.add(remettreIndividuelButton, BorderLayout.EAST);
        
=======
        resultatPanel.setPreferredSize(new Dimension(1050, 80));
        
        resultatLabel = new JLabel("En attente du premier tirage", SwingConstants.CENTER);
        resultatLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        resultatLabel.setForeground(TEXT_NOIR);
        
        remettreIndividuelButton = createStyledButton("Remettre", SUCCESS_COLOR);
        remettreIndividuelButton.setVisible(false);
        remettreIndividuelButton.addActionListener(e -> controller.remettreIndividuel());
        
        resultatPanel.add(resultatLabel, BorderLayout.CENTER);
        resultatPanel.add(remettreIndividuelButton, BorderLayout.EAST);
        
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        // ===== TABLES =====
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setBackground(BACKGROUND_COLOR);
        
        // Table tous les élèves
        JPanel tableTousPanel = createTablePanel("Tous les élèves", true);
        tablesPanel.add(tableTousPanel);
        
        // Table élèves déjà tirés
        JPanel tableTiresPanel = createTablePanel("Élèves déjà tirés", false);
        tablesPanel.add(tableTiresPanel);
        
        // ===== ASSEMBLAGE =====
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(resultatPanel, BorderLayout.NORTH);
        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createTablePanel(String titre, boolean pourTous) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel tableTitle = new JLabel(titre, SwingConstants.LEFT);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_COLOR);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        panel.add(tableTitle, BorderLayout.NORTH);
        
        String[] colonnes = {"Nom", "Prénom", "Note"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(new Color(191, 219, 254));
        table.setSelectionForeground(TEXT_COLOR);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(pourTous ? PRIMARY_COLOR : WARNING_COLOR);
<<<<<<< HEAD
        header.setForeground(Color.WHITE);
=======
        header.setForeground(Color.black);
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        if (pourTous) {
            tableTous = table;
            tableModelTous = model;
        } else {
            tableTires = table;
            tableModelTires = model;
        }
        
        return panel;
    }
    
    private void chargerCSV() {
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
<<<<<<< HEAD
            File fichier = fileChooser.getSelectedFile();
            try {
                lireCSV(fichier);
                JOptionPane.showMessageDialog(this, 
                    eleves.size() + " élève(s) chargé(s) avec succès !",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                activerBoutons(true);
                mettreAJourInfo();
=======
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    public void afficherMessage(String message, String titre, int type) {
        JOptionPane.showMessageDialog(this, message, titre, type);
    }
    
    public void afficherResultatTirage(Eleve eleve) {
    resultatLabel.setText(eleve.toString() + " (Note: " + eleve.getNote() + ")");
    resultatPanel.setBackground(BACKGROUND_COLOR);
    
    // ⭐ Modifier la couleur du texte
    resultatLabel.setForeground(PRIMARY_COLOR); // ou une autre couleur
    
    remettreIndividuelButton.setVisible(true);
}

    
    public void afficherListeEleves(List<Eleve> tous, List<Eleve> tires) {
        tableModelTous.setRowCount(0);
        for (Eleve e : tous) {
            if (!e.isDejaTire()) {
                tableModelTous.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
            }
        }
        
        tableModelTires.setRowCount(0);
        for (Eleve e : tires) {
            tableModelTires.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
        }
    }
    
    public void activerBoutons(boolean activer) {
        tirerButton.setEnabled(activer);
        afficherButton.setEnabled(activer);
        remettreToutes.setEnabled(activer);
        
        if (!activer) {
            tirerButton.setBackground(new Color(156, 163, 175));
            afficherButton.setBackground(new Color(156, 163, 175));
            remettreToutes.setBackground(new Color(156, 163, 175));
        } else {
            tirerButton.setBackground(SECONDARY_COLOR);
            afficherButton.setBackground(SUCCESS_COLOR);
            remettreToutes.setBackground(WARNING_COLOR);
        }
    }
    
    public double getPoids() {
        return poidsSlider.getValue() / 10.0;
    }
    
    public void cacherBoutonRemettre() {
        remettreIndividuelButton.setVisible(false);
    }
    
    public void mettreAJourInfo(int disponibles, int total) {
        infoLabel.setText(disponibles + " élèves disponibles sur " + total);
    }
    
    @Override
    public void onDataChanged() {
        activerBoutons(!controller.getModel().isEmpty());
    }
}

// ============= CONTRÔLEUR =============
class EleveController {
    private EleveModel model;
    private EleveView view;
    private Eleve dernierEleve;
    
    public EleveController(EleveModel model, EleveView view) {
        this.model = model;
        this.view = view;
        model.addListener(view);
        this.dernierEleve = null;
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
                view.mettreAJourInfo(model.getNombreElevesDisponibles(), model.getNombreEleves());
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
                afficherEleves();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la lecture du fichier : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
<<<<<<< HEAD
    private void lireCSV(File fichier) throws IOException {
        eleves.clear();
        elevesDejaChoisis.clear();
        BufferedReader br = new BufferedReader(new FileReader(fichier));
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
            if (parts.length >= 3) {
                String nom = parts[0].trim();
                String prenom = parts[1].trim();
                try {
                    double note = Double.parseDouble(parts[2].trim());
                    if (!nom.isEmpty() && !prenom.isEmpty()) {
                        eleves.add(new Eleve(nom, prenom, note));
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les lignes avec des notes invalides
                }
            }
        }
        br.close();
    }
    
    private void tirerAuSort() {
        // Arrêter les animations en cours
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }
        
        double poids = poidsSlider.getValue() / 10.0;
        List<Eleve> elevesDisponibles = new ArrayList<>();
        
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) {
                elevesDisponibles.add(e);
            }
        }
        
        if (elevesDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Aucun élève disponible !",
                "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calcul des poids
        double noteMax = elevesDisponibles.stream().mapToDouble(Eleve::getNote).max().orElse(20.0);
        List<Double> poidsList = new ArrayList<>();
        double sommesPoids = 0.0;
        
        for (Eleve e : elevesDisponibles) {
            double poidsEleve = Math.pow(noteMax - e.getNote() + 1, poids);
            poidsList.add(poidsEleve);
            sommesPoids += poidsEleve;
        }
        
        // Tirage pondéré
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
        
        // Désactiver le bouton pendant l'animation
        tirerButton.setEnabled(false);
        
        // Animation de type machine à sous
        final Eleve eleveGagnant = gagnant;
        final int[] compteur = {0};
        final int dureeAnimation = 50; // nombre de changements
        
        animationTimer = new javax.swing.Timer(50, null);
        animationTimer.addActionListener(e -> {
            if (compteur[0] < dureeAnimation) {
                // Afficher un élève aléatoire
                Eleve eleveAleatoire = elevesDisponibles.get(random.nextInt(elevesDisponibles.size()));
                resultatLabel.setText(eleveAleatoire.toString());
                resultatLabel.setForeground(SECONDARY_COLOR);
                compteur[0]++;
            } else {
                // Arrêter l'animation et afficher le gagnant
                animationTimer.stop();
                dernierEleve = eleveGagnant;
                eleveGagnant.setDejaTire(true);
                elevesDejaChoisis.add(eleveGagnant);
                
                afficherResultatTirage(eleveGagnant);
                lancerConfettis();
                afficherEleves();
                mettreAJourInfo();
                tirerButton.setEnabled(true);
                
                if (getNombreElevesDisponibles() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Tous les élèves ont été tirés !",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        animationTimer.start();
    }
    
    private void lancerConfettis() {
        confettis.clear();
        
        // Créer 100 confettis
        for (int i = 0; i < 100; i++) {
            confettis.add(new Confetti(resultatPanel.getWidth(), resultatPanel.getHeight()));
        }
        
        // Animation des confettis
        confettiTimer = new javax.swing.Timer(30, null);
        confettiTimer.addActionListener(e -> {
            boolean tousSortis = true;
            
            for (Confetti c : confettis) {
                c.update();
                if (!c.isOutOfBounds(resultatPanel.getHeight())) {
                    tousSortis = false;
                }
            }
            
            resultatPanel.repaint();
            
            if (tousSortis) {
                confettiTimer.stop();
                confettis.clear();
            }
        });
        
        confettiTimer.start();
    }
    
    private void afficherResultatTirage(Eleve eleve) {
        resultatLabel.setText("🎉 " + eleve.toString() + " 🎉");
        resultatLabel.setForeground(new Color(139, 92, 246));
        resultatPanel.setBackground(new Color(254, 249, 195));
        remettreIndividuelButton.setVisible(true);
    }
    
    private void afficherEleves() {
        tableModelTous.setRowCount(0);
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) {
                tableModelTous.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
            }
        }
        
        tableModelTires.setRowCount(0);
        for (Eleve e : elevesDejaChoisis) {
            tableModelTires.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
        }
    }
    
    private void remettreIndividuel() {
        if (dernierEleve != null) {
            dernierEleve.setDejaTire(false);
            elevesDejaChoisis.remove(dernierEleve);
            remettreIndividuelButton.setVisible(false);
            afficherEleves();
            mettreAJourInfo();
            dernierEleve = null;
        }
=======
    public void tirerAuSort() {
        double poids = view.getPoids();
        dernierEleve = model.tirerAuSortPondere(poids);
        
        if (dernierEleve != null) {
            view.afficherResultatTirage(dernierEleve);
            afficherEleves();
            view.mettreAJourInfo(model.getNombreElevesDisponibles(), model.getNombreEleves());
            
            if (model.getNombreElevesDisponibles() == 0) {
                view.afficherMessage(
                    "Tous les élèves ont été tirés !",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } else {
            view.afficherMessage(
                "Aucun élève disponible !",
                "Attention",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    public void afficherEleves() {
        view.afficherListeEleves(model.getEleves(), model.getElevesDejaChoisis());
    }
    
    public void remettreIndividuel() {
        if (dernierEleve != null) {
            model.remettreEleve(dernierEleve);
            view.cacherBoutonRemettre();
            afficherEleves();
            view.mettreAJourInfo(model.getNombreElevesDisponibles(), model.getNombreEleves());
            dernierEleve = null;
        }
    }
    
    public void remettreToutes() {
        model.remettreToutes();
        view.cacherBoutonRemettre();
        afficherEleves();
        view.mettreAJourInfo(model.getNombreElevesDisponibles(), model.getNombreEleves());
        dernierEleve = null;
>>>>>>> 1040e7a450ec83444222b385c5fa3a27baa150d1
    }
    
    private void remettreToutes() {
        for (Eleve e : eleves) {
            e.setDejaTire(false);
        }
        elevesDejaChoisis.clear();
        remettreIndividuelButton.setVisible(false);
        afficherEleves();
        mettreAJourInfo();
        dernierEleve = null;
    }
    
    private void activerBoutons(boolean activer) {
        tirerButton.setEnabled(activer);
        afficherButton.setEnabled(activer);
        remettreToutes.setEnabled(activer);
        
        if (!activer) {
            tirerButton.setBackground(new Color(156, 163, 175));
            afficherButton.setBackground(new Color(156, 163, 175));
            remettreToutes.setBackground(new Color(156, 163, 175));
        } else {
            tirerButton.setBackground(SECONDARY_COLOR);
            afficherButton.setBackground(SUCCESS_COLOR);
            remettreToutes.setBackground(WARNING_COLOR);
        }
    }
    
    private int getNombreElevesDisponibles() {
        int count = 0;
        for (Eleve e : eleves) {
            if (!e.isDejaTire()) count++;
        }
        return count;
    }
    
    private void mettreAJourInfo() {
        infoLabel.setText(getNombreElevesDisponibles() + " élèves disponibles sur " + eleves.size());
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            TirageElevesMVC app = new TirageElevesMVC();
            app.setVisible(true);
        });
    }
}
