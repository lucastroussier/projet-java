import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TirageView extends JFrame {
    private final TirageController controller;
    private TirageModel.ModeTirage modeActuel;

    private JTable tableTous;
    private JTable tableTires;
    private DefaultTableModel tableModelTous;
    private DefaultTableModel tableModelTires;
    private JPanel resultatPanel;
    private JLabel resultatLabel;
    private JButton chargerButton;
    private JButton tirerButton;
//private JButton afficherButton;
    private JButton remettreToutes;
    private JButton remettreIndividuelButton;
    private JSlider poidsSlider;
    private JLabel poidsLabel;
    private JLabel infoLabel;
    private JLabel modeLabel;
    private javax.swing.Timer animationTimer;
    private List<Confetti> confettis;
    private javax.swing.Timer confettiTimer;

    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SECONDARY_COLOR = new Color(139, 92, 246);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(234, 179, 8);
    private final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = Color.BLACK;

    public TirageView(TirageController controller) {
        this.controller = controller;
        this.modeActuel = controller.getModeActuel();
        this.confettis = new ArrayList<>();
        setupUI();
    }

    public static TirageModel.ModeTirage demanderMode() {
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
        return (choix == 1) ? TirageModel.ModeTirage.TICKETS : TirageModel.ModeTirage.NOTES;
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
        setTitle("Tirage Pondéré d'Élèves - Mode " + (modeActuel == TirageModel.ModeTirage.NOTES ? "Notes" : "Tickets"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Tirage Pondéré d'Élèves", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        modeLabel = new JLabel("Mode : " + (modeActuel == TirageModel.ModeTirage.NOTES ? "Tirage basé sur les Notes" : "Tirage basé sur les Tickets"), SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        modeLabel.setForeground(SECONDARY_COLOR);
        titlePanel.add(modeLabel, BorderLayout.SOUTH);

        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        controlPanel.setBackground(BACKGROUND_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        chargerButton = createStyledButton("Charger CSV", PRIMARY_COLOR);
        tirerButton = createStyledButton("Tirer au Sort", SECONDARY_COLOR);
      //  afficherButton = createStyledButton("Afficher Liste", SUCCESS_COLOR);
        remettreToutes = createStyledButton("Remettre Tous", WARNING_COLOR);

        tirerButton.setEnabled(false);
      
      //  afficherButton.setEnabled(false);
        remettreToutes.setEnabled(false);

        chargerButton.addActionListener(e -> chargerCSV());
        tirerButton.addActionListener(e -> tirerAuSort());
      //  afficherButton.addActionListener(e -> afficherEleves());
        remettreToutes.addActionListener(e -> remettreToutes());

        buttonPanel.add(chargerButton);
        buttonPanel.add(tirerButton);
        //buttonPanel.add(afficherButton);
        buttonPanel.add(remettreToutes);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);

        if (modeActuel == TirageModel.ModeTirage.NOTES) {
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

        infoLabel = new JLabel("Chargez un fichier CSV pour commencer", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(107, 114, 128));
        controlPanel.add(infoLabel, BorderLayout.SOUTH);

        headerPanel.add(controlPanel, BorderLayout.CENTER);

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

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setBackground(BACKGROUND_COLOR);

        JPanel tableTousPanel = createTablePanel("Tous les élèves", true);
        tablesPanel.add(tableTousPanel);

        JPanel tableTiresPanel = createTablePanel("Élèves déjà tirés", false);
        tablesPanel.add(tableTiresPanel);

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

        String[] colonnes = modeActuel == TirageModel.ModeTirage.NOTES
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
        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setBackground(pourTous ? PRIMARY_COLOR : WARNING_COLOR);
            header.setForeground(Color.WHITE);
            header.setPreferredSize(new Dimension(0, 40));
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        int columnCount = table.getColumnModel().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
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
                controller.chargerCSV(fichier);
                JOptionPane.showMessageDialog(this,
                    controller.getEleves().size() + " élève(s) chargé(s) avec succès !",
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

    private void tirerAuSort() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        try {
            double poids = modeActuel == TirageModel.ModeTirage.NOTES ? poidsSlider.getValue() / 10.0 : 0.0;
            TirageModel.TirageResult result = controller.tirerAuSort(poids);
            lancerAnimationTirage(result);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Attention", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void lancerAnimationTirage(TirageModel.TirageResult result) {
        tirerButton.setEnabled(false);

        final Eleve eleveGagnant = result.getGagnant();
        final List<Eleve> liste = result.getListeAnimation();
        final int[] compteur = {0};
        final int dureeAnimation = 50;
        Random random = new Random();

        animationTimer = new javax.swing.Timer(50, null);
        animationTimer.addActionListener(e -> {
            if (compteur[0] < dureeAnimation) {
                Eleve eleveAleatoire = liste.get(random.nextInt(liste.size()));
                resultatLabel.setText(eleveAleatoire.toString());
                resultatLabel.setForeground(SECONDARY_COLOR);
                compteur[0]++;
            } else {
                animationTimer.stop();

                afficherResultatTirage(eleveGagnant);
                lancerConfettis();
                afficherEleves();
                mettreAJourInfo();
                tirerButton.setEnabled(true);

                if (modeActuel == TirageModel.ModeTirage.NOTES && controller.getNombreElevesDisponibles() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Tous les élèves ont été tirés !",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                } else if (modeActuel == TirageModel.ModeTirage.TICKETS && controller.getTotalTicketsRestants() == 0) {
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

        for (int i = 0; i < 100; i++) {
            confettis.add(new Confetti(resultatPanel.getWidth(), resultatPanel.getHeight()));
        }

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
        if (modeActuel == TirageModel.ModeTirage.TICKETS) {
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

        if (modeActuel == TirageModel.ModeTirage.NOTES) {
            for (Eleve e : controller.getEleves()) {
                if (!e.isDejaTire()) {
                    tableModelTous.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
                }
            }

            for (Eleve e : controller.getElevesDejaChoisis()) {
                tableModelTires.addRow(new Object[]{e.getNom(), e.getPrenom(), e.getNote()});
            }
        } else {
            for (Eleve e : controller.getEleves()) {
                if (e.getTicketsRestants() > 0) {
                    tableModelTous.addRow(new Object[]{
                        e.getNom(),
                        e.getPrenom(),
                        e.getTicketsRestants() + "/" + e.getTicketsInitiaux()
                    });
                }
            }

            for (Eleve e : controller.getElevesDejaChoisis()) {
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
        controller.remettreIndividuel();
        remettreIndividuelButton.setVisible(false);
        afficherEleves();
        mettreAJourInfo();
    }

    private void remettreToutes() {
        controller.remettreToutes();
        remettreIndividuelButton.setVisible(false);
        afficherEleves();
        mettreAJourInfo();
    }

    private void activerBoutons(boolean activer) {
        tirerButton.setEnabled(activer);
        //afficherButton.setEnabled(activer);
        remettreToutes.setEnabled(activer);

        if (!activer) {
            tirerButton.setBackground(new Color(156, 163, 175));
         //   afficherButton.setBackground(new Color(156, 163, 175));
            remettreToutes.setBackground(new Color(156, 163, 175));
        } else {
            tirerButton.setBackground(SECONDARY_COLOR);
          //  afficherButton.setBackground(SUCCESS_COLOR);
            remettreToutes.setBackground(WARNING_COLOR);
        }
    }

    private void mettreAJourInfo() {
        if (modeActuel == TirageModel.ModeTirage.NOTES) {
            infoLabel.setText(controller.getNombreElevesDisponibles() + " élèves disponibles sur " + controller.getEleves().size());
        } else {
            infoLabel.setText(controller.getTotalTicketsRestants() + " tickets disponibles");
        }
    }

    private class Confetti {
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
            vy += 0.1;
        }

        public boolean isOutOfBounds(int height) {
            return y > height;
        }
    }

    private class ConfettiPanel extends JPanel {
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TirageModel.ModeTirage mode = demanderMode();
            TirageModel model = new TirageModel(mode);
            TirageController controller = new TirageController(model);
            TirageView app = new TirageView(controller);
            app.setVisible(true);
        });
    }
}
