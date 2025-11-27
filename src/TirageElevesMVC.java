import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
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
    private JPanel resultatPanel;
    private JLabel resultatLabel;
    private JButton chargerButton, tirerButton, afficherButton;
    private EleveController controller;
    
    // Couleurs
    private final Color PRIMARY_COLOR = new Color(59, 130, 246); // Bleu
    private final Color SECONDARY_COLOR = new Color(139, 92, 246); // Violet
    private final Color SUCCESS_COLOR = new Color(34, 197, 94); // Vert
    private final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Gris clair
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = Color.BLACK;
    
    public EleveView() {
        setupUI();
    }
    
    public void setController(EleveController controller) {
        this.controller = controller;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet hover
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
        setTitle("Tirage Aléatoire d'Élèves");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Tirage Aléatoire d'Élèves", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        chargerButton = createStyledButton("Charger CSV", PRIMARY_COLOR);
        tirerButton = createStyledButton("Tirer au Sort", SECONDARY_COLOR);
        afficherButton = createStyledButton("Afficher Liste", SUCCESS_COLOR);
        
        tirerButton.setEnabled(false);
        afficherButton.setEnabled(false);
        
        chargerButton.addActionListener(e -> controller.chargerCSV());
        tirerButton.addActionListener(e -> controller.tirerAuSort());
        afficherButton.addActionListener(e -> controller.afficherEleves());
        
        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
        buttonPanel.add(afficherButton);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // ===== PANNEAU RÉSULTAT =====
        resultatPanel = new JPanel(new BorderLayout());
        resultatPanel.setBackground(new Color(243, 244, 246));
        resultatPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        resultatPanel.setPreferredSize(new Dimension(850, 100));
        
        resultatLabel = new JLabel("Cliquez sur 'Tirer au Sort' pour commencer", SwingConstants.CENTER);
        resultatLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resultatLabel.setForeground(TEXT_COLOR);
        
        resultatPanel.add(resultatLabel, BorderLayout.CENTER);
        
        // ===== TABLE =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel tableTitle = new JLabel("Liste des élèves", SwingConstants.LEFT);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(TEXT_COLOR);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        
        String[] colonnes = {"Nom", "Prénom"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(191, 219, 254));
        table.setSelectionForeground(TEXT_COLOR);
        
        // Style de l'en-tête
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Centrer le contenu des cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ===== ASSEMBLAGE =====
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(resultatPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH);
        
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
        resultatLabel.setText(resultat);
        resultatPanel.setBackground(new Color(254, 249, 195)); // Jaune clair
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
        
        if (!activer) {
            tirerButton.setBackground(new Color(156, 163, 175));
            afficherButton.setBackground(new Color(156, 163, 175));
        } else {
            tirerButton.setBackground(SECONDARY_COLOR);
            afficherButton.setBackground(SUCCESS_COLOR);
        }
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
    
    public EleveController(EleveModel model, EleveView view) {
        this.model = model;
        this.view = view;
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
                "Attention",
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            EleveModel model = new EleveModel();
            EleveView view = new EleveView();
            EleveController controller = new EleveController(model, view);
            view.setController(controller);
            view.setVisible(true);
        });
    }
}