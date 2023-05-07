package panels;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
public class Screen extends JFrame implements Runnable{
    private JPanel activePanel;
    private JPanel[] panels;
    private MainMenu mainMenu;
    private JoinGameMenu joinGameMenu;
    private HostGameMenu hostGameMenu;

    public Screen() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        mainMenu = new MainMenu(this);
        add(mainMenu);
        
        joinGameMenu = new JoinGameMenu(this);
        add(joinGameMenu);
        
        hostGameMenu = new HostGameMenu(this);
        add(hostGameMenu);
        
        activePanel = mainMenu;
        panels = new JPanel[]{mainMenu, joinGameMenu, hostGameMenu};

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                activePanel.setBounds(0, 0, getWidth(), getHeight());
                activePanel.repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    public void start() {
        pack();
        setVisible(true);

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(25);
            } catch(Exception e) {
                e.printStackTrace();
            }
            activePanel.repaint();
        }
    }

    protected void switchPanels(PanelType panelType) {
        switch(panelType) {
            case MAIN_MENU-> {
                activePanel = mainMenu;
            }
            case HOST_GAME-> {
                activePanel = hostGameMenu;
                hostGameMenu.startClientSearch();
            }
            case JOIN_GAME-> {
                activePanel = joinGameMenu;
                joinGameMenu.startHostSearch();
            }
            case GAME_LOBBY -> {
                
            }
            case PLAYING_GAME -> {

            }
        }

        for(JPanel panel : panels) {
            if(panel != activePanel) {
                if(panel.isVisible()) {
                    panel.setVisible(false);
                }
            }
        }

        if(!activePanel.isVisible()) {
            activePanel.setVisible(true);
        }

        activePanel.setBounds(getBounds());
        activePanel.repaint();
    }
}
