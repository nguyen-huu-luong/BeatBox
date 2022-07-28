import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.midi.*;
import java.io.*;

public class MyBeatBox {
    
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theframe;
    String[] instrucments = {"Bass Drum", "Closed Hi-Hat", 
    "Open Hi-Hat","Acoustic Snare", "Crash Cymbal", "Hand Clap", 
    "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", 
    "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", 
    "Open Hi Conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args){
        MyBeatBox obj = new MyBeatBox();
        obj.buildGUI();
    }

    public void buildGUI(){
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        theframe = new JFrame("My beat box");
        Box nameItem = new Box(BoxLayout.Y_AXIS);
        Box buttonItem = new Box(BoxLayout.Y_AXIS);

        theframe.setDefaultCloseOperation(theframe.EXIT_ON_CLOSE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton Startbutton = new JButton("start");
        Startbutton.addActionListener(new StartEvent());
        JButton Stopbutton = new JButton("stop");
        Stopbutton.addActionListener(new StopEvent());
        JButton upTempobutton = new JButton("Tempo Up");
        upTempobutton.addActionListener(new UpTempEvent());
        JButton downTempobutton = new JButton("Tempo Down");
        downTempobutton.addActionListener(new DownTempEvent());
        JButton serializelt = new JButton("serializelt");
        serializelt.addActionListener(new MySendListener());
        JButton restore = new JButton("restore");
        restore.addActionListener(new  MyReadInListener());

        buttonItem.add(Startbutton);
        buttonItem.add(Stopbutton);
        buttonItem.add(downTempobutton);
        buttonItem.add(upTempobutton);
        buttonItem.add(serializelt);
        buttonItem.add(restore);

        for(int i=0; i<instrucments.length; i++){
            nameItem.add(new Label(instrucments[i]));
        }

        GridLayout grid = new GridLayout(16,16);
        grid.setHgap(2);
        grid.setVgap(1);
        JPanel CheckboxItem = new JPanel(grid);

        checkboxList = new ArrayList<JCheckBox>();
        for(int i = 0; i < 256; i++){
            JCheckBox item = new JCheckBox();
            item.setSelected(false);
            CheckboxItem.add(item);
            checkboxList.add(item);
        }
        
        panel.add(BorderLayout.CENTER,CheckboxItem);
        panel.add(BorderLayout.EAST,buttonItem);
        panel.add(BorderLayout.WEST,nameItem);
        theframe.getContentPane().add(panel);

        setUpMidi();
        theframe.setBounds(50,50,300,300);
        theframe.pack();
        theframe.setVisible(true);


    }

    public void  setUpMidi(){

        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void  buildTrackAndStart(){

        int[] trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i = 0; i < 16; i++){

            trackList = new int[16];
            int key = instruments[i];

            for(int j = 0; j < 16; j++){

                JCheckBox checkbox = (JCheckBox)checkboxList.get(j + 16*i);
                if(checkbox.isSelected()){
                    trackList[j] = key;
                }
                else{
                    trackList[j] = 0;
                }
            }

            makeTrack(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }
        track.add(makeEvent(192,9,1,0,15));

        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void makeTrack(int[] arr){
        for(int i = 0; i < 16; i++){
            int key = arr[i];

            if(key != 0){
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    class StartEvent implements ActionListener{
        public void actionPerformed(ActionEvent event){
            buildTrackAndStart();
        }
    }

    class StopEvent implements ActionListener{
        public void actionPerformed(ActionEvent event){
            sequencer.stop();
        }
    }

    class UpTempEvent implements ActionListener{
        public void actionPerformed(ActionEvent event){
            float uptemp = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(uptemp*1.03));
        }
    }

    class DownTempEvent implements ActionListener{
        public void actionPerformed(ActionEvent event){
            float uptemp = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(uptemp*0.97));
        }
    }

    class MySendListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            
            boolean[] serial = new boolean[256];

            for(int i = 0; i < 256; i++){
                JCheckBox item = (JCheckBox)checkboxList.get(i);
                if(item.isSelected()){
                    serial[i] = true;
                }
            }

            try{
                JFileChooser save = new JFileChooser();
                int create = save.showSaveDialog(theframe);
                BufferedWriter  wti;
                File path = null;
                if(create == JFileChooser.APPROVE_OPTION){
                    try{
                        path = save.getSelectedFile();
                        wti = new BufferedWriter(new FileWriter(save.getSelectedFile()));
                    }
                    catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
                String name = path.getAbsolutePath();

                FileOutputStream file = new FileOutputStream(new File(name));
                ObjectOutputStream os = new ObjectOutputStream(file);

                os.writeObject(serial);

                os.close();
            }

            catch(Exception ex){
                ex.printStackTrace();
            }

        }
    }

    class MyReadInListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            boolean[] serial = null;
            try{
                JFileChooser save = new JFileChooser();
                int create = save.showOpenDialog(theframe);
                File choose = save.getSelectedFile();
                FileInputStream file = new FileInputStream(new File(choose.getAbsolutePath()));
                ObjectInputStream in = new ObjectInputStream(file);

                serial = (boolean[]) in.readObject();
                in.close();

            }
            catch(Exception ex){
                ex.printStackTrace();
            }

            for(int i = 0 ; i < 256; i++){
                if(serial[i]){
                    checkboxList.get(i).setSelected(true);
                }
                else{
                    checkboxList.get(i).setSelected(false);
                }
            }

        sequencer.stop();
        buildTrackAndStart();
        }
    }

    public MidiEvent makeEvent(int command, int channel, int data1, int data2, int tick){

        MidiEvent event = null;
        try{
            ShortMessage mess = new ShortMessage(command, channel, data1, data2);
            event = new MidiEvent(mess, tick);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        return event;
    }
}


