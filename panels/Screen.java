package panels;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
public class Screen extends JFrame implements Runnable{
    private JPanel activePanel;
    private JPanel[] panels;
    private MainMenu mainMenu;
    private JoinGameMenu joinGameMenu;
    private HostGameMenu hostGameMenu;
    private LobbyMenu lobbyMenu;
    private GamePanel gamePanel;
    private InstructionsPanel instructionMenu;
    private String username;
    private boolean debugMode;

    public Screen(boolean debug) {
        debugMode = debug;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        mainMenu = new MainMenu(this);
        add(mainMenu);
        
        joinGameMenu = new JoinGameMenu(this);
        add(joinGameMenu);
        
        hostGameMenu = new HostGameMenu(this);
        add(hostGameMenu);

        lobbyMenu = new LobbyMenu(this);
        add(lobbyMenu);

        gamePanel = new GamePanel(this);
        add(gamePanel);

        instructionMenu = new InstructionsPanel(this);
        add(instructionMenu);
        
        activePanel = mainMenu;
        panels = new JPanel[]{mainMenu, joinGameMenu, hostGameMenu, lobbyMenu,instructionMenu};

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                activePanel.setBounds(0, 0, getWidth(), getHeight());
                activePanel.repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if(activePanel == hostGameMenu) {
                    hostGameMenu.exitClientSearch();
                }
                if(activePanel == lobbyMenu) {
                    lobbyMenu.leaveLobby();
                }
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
                activePanel = lobbyMenu;
                lobbyMenu.joinHost(joinGameMenu.selectedHost());
            }
            case PLAYING_GAME -> {
                if(activePanel == lobbyMenu) {
                    gamePanel.startGame(lobbyMenu.getHost());
                } else if(activePanel == hostGameMenu) {
                    gamePanel.startGame(hostGameMenu.getHost());
                }
                activePanel = gamePanel;
            }
            case INSTRUCTION_MENU -> {
                activePanel = instructionMenu;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + panelType);
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

        activePanel.setBounds(0, 0, getWidth(), getHeight());
        activePanel.repaint();
    }

    public void setName(String name) {
        username = name;
        hostGameMenu.setName(name);
    }

    public String getName() {
        return username;
    }

    public boolean debugMode() {
        return debugMode;
    }
}
