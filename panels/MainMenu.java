package panels;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;

public class MainMenu extends JPanel implements ActionListener, DocumentListener {
    private Screen parent;

    private JButton joinGameButton;
    private JButton hostGameButton;
    private JButton instructionMenuButton;

    private JTextField nameField;

    public MainMenu(Screen p) {
        parent = p;

        setLayout(null);

        joinGameButton = new MenuButton("Join game");
        add(joinGameButton);
        joinGameButton.addActionListener(this);
        joinGameButton.setEnabled(false);

        hostGameButton = new MenuButton("Host game");
        add(hostGameButton);
        hostGameButton.addActionListener(this);
        hostGameButton.setEnabled(false);

        instructionMenuButton = new MenuButton("Instructions");
        add(instructionMenuButton);
        instructionMenuButton.addActionListener(this);

        nameField = new JTextField();
        add(nameField);
        nameField.getDocument().addDocumentListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                joinGameButton.setBounds(getWidth() / 2 - 75, getHeight() / 2 + 75, 150, 50);
                hostGameButton.setBounds(getWidth() / 2 - 75, getHeight() / 2 + 150, 150, 50);
                instructionMenuButton.setBounds(getWidth() / 2 - 75, getHeight() / 2, 150, 50);
                nameField.setBounds(getWidth() / 2 - 75, getHeight() / 2 - 50, 150, 25);
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawString("Username:", getWidth() / 2 - 75, getHeight() / 2 - 75);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == joinGameButton) {
            parent.setName(nameField.getText());
            parent.switchPanels(PanelType.JOIN_GAME);
        } else if (e.getSource() == hostGameButton) {
            parent.setName(nameField.getText());
            parent.switchPanels(PanelType.HOST_GAME);
        } else if (e.getSource() == instructionMenuButton) {
            parent.switchPanels(PanelType.INSTRUCTION_MENU);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        checkUsername();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        checkUsername();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        checkUsername();
    }

    private void checkUsername() {
        boolean validName = nameField.getText().length() > 0 && nameField.getText().length() < 30 && nameField.getText().matches("^[a-zA-Z ]+$");
        joinGameButton.setEnabled(validName);
        hostGameButton.setEnabled(validName);
    }
}
