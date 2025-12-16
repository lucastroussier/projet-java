import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class TirageElevesMVC extends JFrame {
    // Mode de tirage
    private enum ModeTirage {
        NOTES, TICKETS
    }
    
    private ModeTirage modeActuel;
    
    // Données
    private List<Eleve> eleves;
    private List<Eleve> elevesDejaChoisis;
    private Eleve dernierEleve;
    
    // Composants UI
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
    private JLabel modeLabel;
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
    
    // Classe interne Eleve
    class Eleve {
        private String nom;
        private String prenom;
        private double note;
        private int ticketsInitiaux;
        private int ticketsRestants;
        private boolean dejaTire; // Pour mode NOTES uniquement
        
        public Eleve(String nom, String prenom, double note, int tickets) {
            this.nom = nom;
            this.prenom = prenom;
            this.note = note;
            this.ticketsInitiaux = tickets;
            this.ticketsRestants = tickets;
            this.dejaTire = false;
        }
        
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public double getNote() { return note; }
        public int getTicketsInitiaux() { return ticketsInitiaux; }
        public int getTicketsRestants() { return ticketsRestants; }
        public boolean isDejaTire() { return dejaTire; }
        
        public void setDejaTire(boolean dejaTire) { this.dejaTire = dejaTire; }
        
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
        
        // Choix du mode au démarrage
        choisirMode();
        
        setupUI();
    }
    
    private void choisirMode() {
        String[] options = {"Tirage basé sur les Notes", "Tirage basé sur les Tickets"};
        int choix = JOptionPane.showOptionDialog(
            null,
            "Choisissez le mode de tirage :",
            "Mode de Tirage",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        modeActuel = (choix == 1) ? ModeTirage.TICKETS : ModeTirage.NOTES;
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
        setTitle("Tirage Pondéré d'Élèves - Mode " + (modeActuel == ModeTirage.NOTES ? "Notes" : "Tickets"));
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
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Tirage Pondéré d'Élèves", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        
        modeLabel = new JLabel("Mode : " + (modeActuel == ModeTirage.NOTES ? "Tirage basé sur les Notes" : "Tirage basé sur les Tickets"), SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        modeLabel.setForeground(SECONDARY_COLOR);
        titlePanel.add(modeLabel, BorderLayout.SOUTH);
        
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        
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
        
        chargerButton.addActionListener(e -> chargerCSV());
        tirerButton.addActionListener(e -> tirerAuSort());
        afficherButton.addActionListener(e -> afficherEleves());
        remettreToutes.addActionListener(e -> remettreToutes());
        
        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
        buttonPanel.add(afficherButton);
        buttonPanel.add(remettreToutes);
        
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Slider pour le poids (uniquement en mode NOTES)
        if (modeActuel == ModeTirage.NOTES) {
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
            
            poidsLabel = new JLabel("1.0");
            poidsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            poidsLabel.setForeground(PRIMARY_COLOR);
            
            poidsSlider.addChangeListener(e -> {
                double valeur = poidsSlider.getValue() / 10.0;
                poidsLabel.setText(String.format("%.1f", valeur));
            });
            
            poidsPanel.add(poidsLabelText);
            poidsPanel.add(poidsSlider);
            poidsPanel.add(poidsLabel);
            
            controlPanel.add(poidsPanel, BorderLayout.CENTER);
        }
        
        // Info label
        infoLabel = new JLabel("Chargez un fichier CSV pour commencer", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(107, 114, 128));
        controlPanel.add(infoLabel, BorderLayout.SOUTH);
        
        headerPanel.add(controlPanel, BorderLayout.CENTER);
        
        // ===== PANNEAU RÉSULTAT =====
        resultatPanel = new ConfettiPanel();
        resultatPanel.setLayout(new BorderLayout(10, 0));
        resultatPanel.setBackground(new Color(243, 244, 246));
        resultatPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        resultatPanel.setPreferredSize(new Dimension(1050, 120));
        
        resultatLabel = new JLabel("En attente du premier tirage", SwingConstants.CENTER);
        resultatLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        resultatLabel.setForeground(TEXT_COLOR);
        
        remettreIndividuelButton = createStyledButton("Remettre", SUCCESS_COLOR);
        remettreIndividuelButton.setVisible(false);
        remettreIndividuelButton.addActionListener(e -> remettreIndividuel());
        
        resultatPanel.add(resultatLabel, BorderLayout.CENTER);
        resultatPanel.add(remettreIndividuelButton, BorderLayout.EAST);
        
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
        
        String[] colonnes = modeActuel == ModeTirage.NOTES 
            ? new String[]{"Nom", "Prénom", "Note"}
            : new String[]{"Nom", "Prénom", "Tickets"};
            
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
        header.setForeground(Color.WHITE);
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
            File fichier = fileChooser.getSelectedFile();
            try {
                lireCSV(fichier);
                JOptionPane.showMessageDialog(this, 
                    eleves.size() + " élève(s) chargé(s) avec succès !",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                activerBoutons(true);
                mettreAJourInfo();
                afficherEleves();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la lecture du fichier : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
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
            
            if (modeActuel == ModeTirage.NOTES) {
                // Mode NOTES : nom, prenom, note (3 colonnes)
                if (parts.length >= 3) {
                    String nom = parts[0].trim();
                    String prenom = parts[1].trim();
                    try {
                        double note = Double.parseDouble(parts[2].trim());
                        if (!nom.isEmpty() && !prenom.isEmpty()) {
                            eleves.add(new Eleve(nom, prenom, note, 0));
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les lignes avec des valeurs invalides
                    }
                }
            } else {
                // Mode TICKETS : nom, prenom, note, tickets (4 colonnes)
                if (parts.length >= 4) {
                    String nom = parts[0].trim();
                    String prenom = parts[1].trim();
                    try {
                        double note = Double.parseDouble(parts[2].trim());
                        int tickets = Integer.parseInt(parts[3].trim());
                        if (!nom.isEmpty() && !prenom.isEmpty()) {
                            eleves.add(new Eleve(nom, prenom, note, tickets));
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les lignes avec des valeurs invalides
                    }
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
        
        if (modeActuel == ModeTirage.NOTES) {
            tirerAuSortNotes();
        } else {
            tirerAuSortTickets();
        }
    }
    
    private void tirerAuSortNotes() {
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
        
        lancerAnimationTirage(elevesDisponibles, gagnant, random, true);
    }
    
    private void tirerAuSortTickets() {
        // Créer la liste avec duplication basée sur les tickets
        List<Eleve> listeAvecTickets = new ArrayList<>();
        
        for (Eleve e : eleves) {
            for (int i = 0; i < e.getTicketsRestants(); i++) {
                listeAvecTickets.add(e);
            }
        }
        
        if (listeAvecTickets.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Aucun ticket disponible !",
                "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Tirage aléatoire simple
        Random random = new Random();
        Eleve gagnant = listeAvecTickets.get(random.nextInt(listeAvecTickets.size()));
        
        lancerAnimationTirage(listeAvecTickets, gagnant, random, false);
    }
    
    private void lancerAnimationTirage(List<Eleve> liste, Eleve gagnant, Random random, boolean modeNotes) {
        // Désactiver le bouton pendant l'animation
        tirerButton.setEnabled(false);
        
        // Animation de type machine à sous
        final Eleve eleveGagnant = gagnant;
        final int[] compteur = {0};
        final int dureeAnimation = 50;
        
        animationTimer = new javax.swing.Timer(50, null);
        animationTimer.addActionListener(e -> {
            if (compteur[0] < dureeAnimation) {
                // Afficher un élève aléatoire
                Eleve eleveAleatoire = liste.get(random.nextInt(liste.size()));
                resultatLabel.setText(eleveAleatoire.toString());
                resultatLabel.setForeground(SECONDARY_COLOR);
                compteur[0]++;
            } else {
                // Arrêter l'animation et afficher le gagnant
                animationTimer.stop();
                dernierEleve = eleveGagnant;
                
                if (modeNotes) {
                    eleveGagnant.setDejaTire(true);
                    elevesDejaChoisis.add(eleveGagnant);
                } else {
                    eleveGagnant.consommerTicket();
                    // Ajouter à la liste des tirés seulement si c'était le premier tirage de cet élève
                    if (!elevesDejaChoisis.contains(eleveGagnant)) {
                        elevesDejaChoisis.add(eleveGagnant);
                    }
                }
                
                afficherResultatTirage(eleveGagnant);
                lancerConfettis();
                afficherEleves();
                mettreAJourInfo();
                tirerButton.setEnabled(true);
                
                if (modeNotes && getNombreElevesDisponibles() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Tous les élèves ont été tirés !",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                } else if (!modeNotes && getTotalTicketsRestants() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Tous les tickets ont été utilisés !",
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
        String message = "🎉 " + eleve.toString() + " 🎉";
        if (modeActuel == ModeTirage.TICKETS) {
            message += " (" + eleve.getTicketsRestants() + " ticket(s) restant(s))";
        }
        resultatLabel.setText(message);
        resultatLabel.setForeground(new Color(139, 92, 246));
        resultatPanel.setBackground(new Color(254, 249, 195));
        remettreIndividuelButton.setVisible(true);
    }
    
    private void afficherEleves() {
        tableModelTous.setRowCount(0);
        tableModelTires.setRowCount(0);
        
        if (modeActuel == ModeTirage.NOTES) {
            for (Eleve e : eleves) {
                if (!e.isDejaTire()) {
                    tableModelTous.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
                }
            }
            
            for (Eleve e : elevesDejaChoisis) {
                tableModelTires.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
            }
        } else {
            // Mode TICKETS
            for (Eleve e : eleves) {
                if (e.getTicketsRestants() > 0) {
                    tableModelTous.addRow(new Object[]{
                        e.getNom(), 
                        e.getPrenom(), 
                        e.getTicketsRestants() + "/" + e.getTicketsInitiaux()
                    });
                }
            }
            
            for (Eleve e : elevesDejaChoisis) {
                int ticketsUtilises = e.getTicketsInitiaux() - e.getTicketsRestants();
                tableModelTires.addRow(new Object[]{
                    e.getNom(), 
                    e.getPrenom(), 
                    ticketsUtilises + " tirage(s)"
                });
            }
        }
    }
    
    private void remettreIndividuel() {
        if (dernierEleve != null) {
            if (modeActuel == ModeTirage.NOTES) {
                dernierEleve.setDejaTire(false);
                elevesDejaChoisis.remove(dernierEleve);
            } else {
                // En mode TICKETS, remettre un ticket
                dernierEleve.resetTickets();
                dernierEleve.consommerTicket(); // Remet à ticketsInitiaux - 1
                int ticketsUtilises = dernierEleve.getTicketsInitiaux() - dernierEleve.getTicketsRestants();
                if (ticketsUtilises == 0) {
                    elevesDejaChoisis.remove(dernierEleve);
                }
            }
            remettreIndividuelButton.setVisible(false);
            afficherEleves();
            mettreAJourInfo();
            dernierEleve = null;
        }
    }
    
    private void remettreToutes() {
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
    
    private int getTotalTicketsRestants() {
        int total = 0;
        for (Eleve e : eleves) {
            total += e.getTicketsRestants();
        }
        return total;
    }
    
    private void mettreAJourInfo() {
        if (modeActuel == ModeTirage.NOTES) {
            infoLabel.setText(getNombreElevesDisponibles() + " élèves disponibles sur " + eleves.size());
        } else {
            infoLabel.setText(getTotalTicketsRestants() + " tickets disponibles");
        }
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