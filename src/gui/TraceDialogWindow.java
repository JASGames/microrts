package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Timer;

import org.jdom.input.SAXBuilder;

import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.Trace;
import rts.TraceEntry;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.FlowLayout;

public class TraceDialogWindow extends JDialog implements AdjustmentListener{


    // Misc variables
    int currentGameCycle = 0; // Stores the current game cycle
    int finalPosition = 0; // Stores the ending game cycle
    int PERIOD = 0; // Stores the period of delay for playback


    Trace currentTrace = null;
    PhysicalGameStatePanel statePanel;

    File[] dirFiles;
    File trace;

    // J Components
    private javax.swing.Timer timer = null;
    private JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    private JButton b1; // Play
    private JButton b2; // Pause
    private JButton b3; // Restart
    private JButton b4;
    private JButton b5;
    private JButton b6;
    private JButton b7;
    private JButton b8;
    private JButton b9;


    private JLabel lblControls;
    private JLabel lblDelay;
    private JTextField delayText;
    private JButton btnNewButton;


    public boolean errorCheck(String delayInput) {

        if (delayInput == null || delayInput.equals("") || delayInput.equals(" "))
        {
            return false;
        }

        if (!delayInput.matches("[0-9]+"))
        {
            return false;
        }

        return true;

    }

    // Method to load the evolution run trace
    public void loadTrace() {
        for (File file : dirFiles)
        {
            if (file.getName().equals("game_evolution.xml"))
            {
                trace = file;
            }
        }

        try {
            // create a new trace based on the trace file stored.
            currentTrace = new Trace(new SAXBuilder().build(trace.getAbsolutePath()).getRootElement());
            // Set the current game cycle to 0
            currentGameCycle = 0;

            // Calculate the final position of the trace entry
            for(TraceEntry te:currentTrace.getEntries()) {
                if (te.getTime()>finalPosition) {
                    // Set final position
                    finalPosition = te.getTime();
                    // Set the maximum value of the scrollBar
                    scrollBar.setMaximum(finalPosition+10);

                }
            }
            // Set the current statePanel view to be that of the current trace at current game cycle
            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
            statePanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TraceDialogWindow(File dir, Frame parent) {
        Point parentLocation = parent.getLocation();
        int parentWidth = parent.getWidth()+20;
        setLocation(parentLocation.x+parentWidth, parentLocation.y);



        dirFiles = dir.listFiles();

        getContentPane().setLayout(new BorderLayout());

        JPanel p2 = new JPanel();
        statePanel = new PhysicalGameStatePanel((GameState)null);
        statePanel.setPreferredSize(new Dimension(600, 600));
        loadTrace();


        getContentPane().add(p2, BorderLayout.SOUTH);
        {
        }

        JPanel p1east = new JPanel();
        {
            b1 = new JButton("Play");
            b1.setAlignmentX(Component.CENTER_ALIGNMENT);
            b1.setAlignmentY(Component.TOP_ALIGNMENT);
            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (errorCheck(delayText.getText()) == true) {
                        PERIOD = Integer.parseInt(delayText.getText());

                        if (timer == null) {
                            timer = new javax.swing.Timer(PERIOD, new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (currentGameCycle != finalPosition) {
                                        currentGameCycle++;
                                        statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                                        statePanel.repaint();
                                        scrollBar.setValue(currentGameCycle);
                                        scrollBar.repaint();
                                    }
                                    else if (currentGameCycle == finalPosition)
                                    {
                                        timer.stop();
                                    }

                                }

                            });
                            timer.start();
                        } else {
                            timer.setDelay(PERIOD);
                            timer.start();
                        }
                    }else {
                        JOptionPane.showMessageDialog(new JFrame(), "Error: Please ensure that the delay input is:\n"
                                + "1) Not Blank\n"
                                + "2) Is an integer value\n"
                                + "3) Is greater than '0'.");
                    }

                }
            });
        }
        {
            b2 = new JButton("Pause");
            b2.setAlignmentX(Component.CENTER_ALIGNMENT);
            b2.setAlignmentY(Component.TOP_ALIGNMENT);
            b2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (timer != null) {
                        timer.stop();
                    }
                }
            });
        }
        {
            b3 = new JButton("Restart");
            b3.setAlignmentX(Component.CENTER_ALIGNMENT);
            b3.setAlignmentY(Component.TOP_ALIGNMENT);
            b3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (timer != null) {
                        timer.stop();

                    }
                    timer = null;
                    currentGameCycle = 0;
                    statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                    statePanel.repaint();
                    scrollBar.setValue(currentGameCycle);
                    scrollBar.repaint();
                }
            });
        }
        {
            b4 = new JButton("-1 Frame");
            b4.setAlignmentX(Component.CENTER_ALIGNMENT);
            b4.setAlignmentY(Component.TOP_ALIGNMENT);
            b4.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (currentGameCycle>0) {
                        currentGameCycle--;
                        scrollBar.setValue(currentGameCycle);
                        scrollBar.repaint();

                        statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                        statePanel.repaint();
                    }
                }
            });
        }
        {
            b5 = new JButton("+1 Action");
            b5.setAlignmentX(Component.CENTER_ALIGNMENT);
            b5.setAlignmentY(Component.TOP_ALIGNMENT);
            b5.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    for(TraceEntry te:currentTrace.getEntries()) {
                        if (te.getTime()>currentGameCycle) {
                            currentGameCycle = te.getTime();
                            scrollBar.setValue(currentGameCycle);
                            scrollBar.repaint();
                            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                            statePanel.repaint();
                            break;
                        }
                    }
                }
            });
            {
                b6 = new JButton("-1 Action");
                b6.setAlignmentX(Component.CENTER_ALIGNMENT);
                b6.setAlignmentY(Component.TOP_ALIGNMENT);
                b6.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {

                        TraceEntry target = null;
                        for(TraceEntry te:currentTrace.getEntries()) {
                            if (te.getTime()<currentGameCycle) {
                                if (target==null || te.getTime()>target.getTime()) {
                                    target = te;
                                }
                            }
                        }
                        if (target!=null) {
                            currentGameCycle = target.getTime();
                            scrollBar.setValue(currentGameCycle);
                            scrollBar.repaint();
                            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                            statePanel.repaint();
                        }
                    }
                });
            }
            b7 = new JButton("-5 Action");
            b7.setAlignmentX(Component.CENTER_ALIGNMENT);
            b7.setAlignmentY(Component.TOP_ALIGNMENT);
            b7.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    for (int i=0; i<5;i++)
                    {

                        TraceEntry target = null;
                        for(TraceEntry te:currentTrace.getEntries()) {
                            if (te.getTime()<currentGameCycle) {
                                if (target==null || te.getTime()>target.getTime()) {
                                    target = te;
                                }
                            }
                        }
                        if (target!=null) {
                            currentGameCycle = target.getTime();
                            scrollBar.setValue(currentGameCycle);
                            scrollBar.repaint();
                            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                            statePanel.repaint();
                        }
                    }
                }
            });
            {
                b8 = new JButton("+1 Frame");
                b8.setAlignmentX(Component.CENTER_ALIGNMENT);
                b8.setAlignmentY(Component.TOP_ALIGNMENT);
                b8.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (!statePanel.getState().gameover()) {
                            currentGameCycle++;
                            GameState tmp_gs = currentTrace.getGameStateAtCycle(currentGameCycle);
                            scrollBar.setValue(currentGameCycle);
                            scrollBar.repaint();
                            statePanel.setStateDirect(tmp_gs);
                            statePanel.repaint();
                        }
                    }
                });
            }
        }

        // +5 actions
        {
            b9 = new JButton("+5 Action");
            b9.setAlignmentX(Component.CENTER_ALIGNMENT);
            b9.setAlignmentY(Component.TOP_ALIGNMENT);
            b9.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    for (int i=0; i<5;i++)
                    {


                        for(TraceEntry te:currentTrace.getEntries()) {
                            if (te.getTime()>currentGameCycle) {
                                currentGameCycle = te.getTime();
                                scrollBar.setValue(currentGameCycle);
                                scrollBar.repaint();
                                statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                                statePanel.repaint();
                                break;
                            }
                        }
                    }
                }
            });

        }


        scrollBar.addAdjustmentListener(this);
        scrollBar.setUnitIncrement(1);
        scrollBar.setMinimum(0);

        lblControls = new JLabel("Controls:");

        lblDelay = new JLabel("Delay:");

        delayText = new JTextField();
        delayText.setText("1");
        delayText.setColumns(10);
        GroupLayout gl_p1east = new GroupLayout(p1east);
        gl_p1east.setHorizontalGroup(
                gl_p1east.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_p1east.createSequentialGroup()
                                .addGap(21)
                                .addGroup(gl_p1east.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblControls, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(gl_p1east.createSequentialGroup()
                                                .addGroup(gl_p1east.createParallelGroup(Alignment.LEADING, false)
                                                        .addComponent(lblDelay, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(delayText, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(b1, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                                        .addComponent(b2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(gl_p1east.createSequentialGroup()
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(b3, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(10)
                                                .addGroup(gl_p1east.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(b8)
                                                        .addComponent(b5)
                                                        .addComponent(b4)
                                                        .addComponent(b6)
                                                        .addComponent(b7)
                                                        .addComponent(b9))))
                                .addContainerGap(59, Short.MAX_VALUE))
        );
        gl_p1east.setVerticalGroup(
                gl_p1east.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_p1east.createSequentialGroup()
                                .addGap(11)
                                .addComponent(lblControls)
                                .addGap(11)
                                .addGroup(gl_p1east.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_p1east.createSequentialGroup()
                                                .addComponent(b8)
                                                .addGap(6)
                                                .addComponent(b5)
                                                .addGap(6)
                                                .addComponent(b9)
                                                .addGap(18)
                                                .addComponent(b4)
                                                .addGap(6)
                                                .addComponent(b6)
                                                .addGap(6)
                                                .addComponent(b7))
                                        .addGroup(gl_p1east.createSequentialGroup()
                                                .addComponent(b1)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(b2)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(b3)
                                                .addGap(128)
                                                .addComponent(lblDelay)
                                                .addGap(6)
                                                .addComponent(delayText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(70, Short.MAX_VALUE))
        );
        p1east.setLayout(gl_p1east);
        GroupLayout gl_p2 = new GroupLayout(p2);
        gl_p2.setHorizontalGroup(
                gl_p2.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_p2.createSequentialGroup()
                                .addGap(6)
                                .addComponent(p1east, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE)
                                .addGap(18)
                                .addGroup(gl_p2.createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(gl_p2.createSequentialGroup()
                                                .addGap(10)
                                                .addComponent(scrollBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(statePanel, GroupLayout.PREFERRED_SIZE, 935, GroupLayout.PREFERRED_SIZE))
                                .addGap(24))
        );
        gl_p2.setVerticalGroup(
                gl_p2.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_p2.createSequentialGroup()
                                .addContainerGap(255, Short.MAX_VALUE)
                                .addGroup(gl_p2.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_p2.createSequentialGroup()
                                                .addComponent(p1east, GroupLayout.PREFERRED_SIZE, 355, GroupLayout.PREFERRED_SIZE)
                                                .addGap(189))
                                        .addGroup(Alignment.TRAILING, gl_p2.createSequentialGroup()
                                                .addComponent(statePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(scrollBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap())))
        );
        statePanel.setLayout(null);
        p2.setLayout(gl_p2);
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {


        // Get the value of the scrollBar
        int i = e.getValue();

        // Set the current game cycle to the current value of scrollBar
        currentGameCycle = i;

        // Update the state panel to reflect the current game cycle
        statePanel.setStateDirect(currentTrace.getGameStateAtCycle(i));
        statePanel.repaint();


    }

    public void run() {
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(1260, 685);
        this.setVisible(true);
        this.setModal(false);
        this.setTitle("Trace Results");
    }
}
