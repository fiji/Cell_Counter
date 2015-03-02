/*
 *
 * @author Kurt De Vos (C) 2005
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;
import ij.CompositeImage;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class CellCounter extends JFrame implements ActionListener, ItemListener{
    private static final String ADD = "Add";
    private static final String REMOVE = "Remove";
    private static final String INITIALIZE = "Initialize";
    private static final String RESULTS = "Results";
    private static final String DELETE = "Delete";
    private static final String DELMODE = "Delete Mode";
    private static final String KEEPORIGINAL = "Keep Original";
    private static final String SHOWNUMBERS = "Show Numbers";
    private static final String SHOWALL = "Show All";
    private static final String RESET = "Reset";
    private static final String EXPORTMARKERS = "Save Markers";
    private static final String LOADMARKERS = "Load Markers";
    private static final String EXPORTIMG = "Export Image";
    private static final String MEASURE = "Measure...";
    
    
    private Vector typeVector;
    private Vector dynRadioVector;
    private Vector txtFieldVector;
    private CellCntrMarkerVector markerVector;
    private CellCntrMarkerVector currentMarkerVector;
    
    private JPanel dynPanel;
    private JPanel dynButtonPanel;
    private JPanel statButtonPanel;
    private JPanel dynTxtPanel;
    private JCheckBox delCheck;
    private JCheckBox newCheck;
    private JCheckBox numbersCheck;
    private JCheckBox showAllCheck;
    private ButtonGroup radioGrp;
    private JSeparator separator;
    private JButton addButton;
    private JButton removeButton;
    private JButton initializeButton;
    private JButton resultsButton;
    private JButton deleteButton;
    private JButton resetButton;
    private JButton exportButton;
    private JButton loadButton;
    private JButton exportimgButton;
    private JButton measureButton;
    
    private boolean keepOriginal=false;
    
    private CellCntrImageCanvas ic;
    
    private ImagePlus img;
    private ImagePlus counterImg;
    
    private GridLayout dynGrid;
    
    private boolean isJava14;
    
    static CellCounter instance;
    
    public CellCounter(){
        super("Cell Counter");
        isJava14 = IJ.isJava14(); 
        if(!isJava14){
            IJ.showMessage("You are using a pre 1.4 version of java, exporting and loading marker data is disabled");
        }
        setResizable(false);
        typeVector = new Vector();
        txtFieldVector = new Vector();
        dynRadioVector = new Vector();
        initGUI();
        populateTxtFields();
        instance = this;
    }
    
    /** Show the GUI threadsafe*/
    private static class GUIShower implements Runnable {
        final JFrame jFrame;
        public GUIShower(JFrame jFrame) {
            this.jFrame = jFrame;
        }
        public void run() {
            jFrame.pack();
            jFrame.setLocation(1000, 200);
            jFrame.setVisible(true);
        }
    }
    
    private void initGUI(){
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gb = new GridBagLayout();
        getContentPane().setLayout(gb);
        
        radioGrp = new ButtonGroup();//to group the radiobuttons
        
        dynGrid = new GridLayout(8,1);
        dynGrid.setVgap(2);
        
        //this panel will keep the dynamic GUI parts
        dynPanel = new JPanel();
        dynPanel.setBorder(BorderFactory.createTitledBorder("Counters"));
        dynPanel.setLayout(gb);
        
        //this panel keeps the radiobuttons
        dynButtonPanel = new JPanel();
        dynButtonPanel.setLayout(dynGrid);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx=5;
        gb.setConstraints(dynButtonPanel,gbc);
        dynPanel.add(dynButtonPanel);
        
        //this panel keeps the score
        dynTxtPanel=new JPanel();
        dynTxtPanel.setLayout(dynGrid);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx=5;
        gb.setConstraints(dynTxtPanel,gbc);
        dynPanel.add(dynTxtPanel);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx=5;
        gb.setConstraints(dynPanel,gbc);
        getContentPane().add(dynPanel);
        
        
        dynButtonPanel.add(makeDynRadioButton(1));
        dynButtonPanel.add(makeDynRadioButton(2));
        dynButtonPanel.add(makeDynRadioButton(3));
        dynButtonPanel.add(makeDynRadioButton(4));
        dynButtonPanel.add(makeDynRadioButton(5));
        dynButtonPanel.add(makeDynRadioButton(6));
        dynButtonPanel.add(makeDynRadioButton(7));
        dynButtonPanel.add(makeDynRadioButton(8));
        
        // create a "static" panel to hold control buttons
        statButtonPanel = new JPanel();
        statButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        statButtonPanel.setLayout(gb);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        newCheck = new JCheckBox(KEEPORIGINAL);
        newCheck.setToolTipText("Keep original");
        newCheck.setSelected(false);
        newCheck.addItemListener(this);
        gb.setConstraints(newCheck,gbc);
        statButtonPanel.add(newCheck);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        initializeButton = makeButton(INITIALIZE, "Initialize image to count");
        gb.setConstraints(initializeButton,gbc);
        statButtonPanel.add(initializeButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3,0,3,0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1,1));
        gb.setConstraints(separator,gbc);
        statButtonPanel.add(separator);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addButton = makeButton(ADD, "add a counter type");
        gb.setConstraints(addButton,gbc);
        statButtonPanel.add(addButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        removeButton = makeButton(REMOVE, "remove last counter type");
        gb.setConstraints(removeButton,gbc);
        statButtonPanel.add(removeButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.insets = new Insets(3,0,3,0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1,1));
        gb.setConstraints(separator,gbc);
        statButtonPanel.add(separator);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        deleteButton = makeButton(DELETE, "delete last marker");
        deleteButton.setEnabled(false);
        gb.setConstraints(deleteButton,gbc);
        statButtonPanel.add(deleteButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        delCheck = new JCheckBox(DELMODE);
        delCheck.setToolTipText("When selected\nclick on the marker\nyou want to remove");
        delCheck.setSelected(false);
        delCheck.addItemListener(this);
        delCheck.setEnabled(false);
        gb.setConstraints(delCheck,gbc);
        statButtonPanel.add(delCheck);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3,0,3,0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1,1));
        gb.setConstraints(separator,gbc);
        statButtonPanel.add(separator);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        resultsButton = makeButton(RESULTS, "show results in results table");
        resultsButton.setEnabled(false);
        gb.setConstraints(resultsButton,gbc);
        statButtonPanel.add(resultsButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        resetButton=makeButton(RESET, "reset all counters");
        resetButton.setEnabled(false);
        gb.setConstraints(resetButton,gbc);
        statButtonPanel.add(resetButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3,0,3,0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1,1));
        gb.setConstraints(separator,gbc);
        statButtonPanel.add(separator);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        numbersCheck = new JCheckBox(SHOWNUMBERS);
        numbersCheck.setToolTipText("When selected, numbers are shown");
        numbersCheck.setSelected(true);
        numbersCheck.setEnabled(false);
        numbersCheck.addItemListener(this);
        gb.setConstraints(numbersCheck,gbc);
        statButtonPanel.add(numbersCheck);
        
        showAllCheck = new JCheckBox(SHOWALL);
        showAllCheck.setToolTipText("When selected, all stack markers are shown");
        showAllCheck.setSelected(false);
        showAllCheck.setEnabled(false);
        showAllCheck.addItemListener(this);
        gb.setConstraints(showAllCheck,gbc);
        statButtonPanel.add(showAllCheck);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        exportButton = makeButton(EXPORTMARKERS, "Save markers to file");
        exportButton.setEnabled(false);
        gb.setConstraints(exportButton,gbc);
        statButtonPanel.add(exportButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        loadButton = makeButton(LOADMARKERS, "Load markers from file");
        if (!isJava14) loadButton.setEnabled(false);
        gb.setConstraints(loadButton,gbc);
        statButtonPanel.add(loadButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        exportimgButton= makeButton(EXPORTIMG, "Export image with markers");
        exportimgButton.setEnabled(false);
        gb.setConstraints(exportimgButton,gbc);
        statButtonPanel.add(exportimgButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3,0,3,0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1,1));
        gb.setConstraints(separator,gbc);
        statButtonPanel.add(separator);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        measureButton = makeButton(MEASURE, "Measure pixel intensity of marker points");
        measureButton.setEnabled(false);
        gb.setConstraints(measureButton,gbc);
        statButtonPanel.add(measureButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx=5;
        gb.setConstraints(statButtonPanel,gbc);
        getContentPane().add(statButtonPanel);
        
        Runnable runner = new GUIShower(this);
        EventQueue.invokeLater(runner);
    }
    
    private JTextField makeDynamicTextArea(){
        JTextField txtFld = new JTextField();
        txtFld.setHorizontalAlignment(JTextField.CENTER);
        txtFld.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        txtFld.setEditable(false);
        txtFld.setText("0");
        txtFieldVector.add(txtFld);
        return txtFld;
    }
    
    void populateTxtFields(){
        ListIterator it = typeVector.listIterator();
        while (it.hasNext()){
            int index = it.nextIndex();
            CellCntrMarkerVector markerVector = (CellCntrMarkerVector)it.next();
            int count = markerVector.size();
            JTextField tArea = (JTextField)txtFieldVector.get(index);
            tArea.setText(""+count);
        }
        validateLayout();
    }
    
    private JRadioButton makeDynRadioButton(int id){
        JRadioButton jrButton = new JRadioButton("Type "+ id);
        jrButton.addActionListener(this);
        dynRadioVector.add(jrButton);
        radioGrp.add(jrButton);
        markerVector = new CellCntrMarkerVector(id);
        typeVector.add(markerVector);
        dynTxtPanel.add(makeDynamicTextArea());
        return jrButton;
    }
    
    private JButton makeButton(String name, String tooltip){
        JButton jButton = new JButton(name);
        jButton.setToolTipText(tooltip);
        jButton.addActionListener(this);
        return jButton;
    }
    
    private void initializeImage(){
        reset();
        img = WindowManager.getCurrentImage();
        boolean v139t = IJ.getVersion().compareTo("1.39t")>=0;
        if (img==null){
            IJ.noImage();
        }else if (img.getStackSize() == 1) {
            ImageProcessor ip = img.getProcessor();
            ip.resetRoi();
            if (keepOriginal)
                ip = ip.crop();
            counterImg = new ImagePlus("Counter Window - "+img.getTitle(), ip);
            Vector displayList = v139t?img.getCanvas().getDisplayList():null;
            ic = new CellCntrImageCanvas(counterImg,typeVector,this,displayList);
            new ImageWindow(counterImg, ic);
        } else if (img.getStackSize() > 1){
            ImageStack stack = img.getStack();
            int size = stack.getSize();
            ImageStack counterStack = img.createEmptyStack();
            for (int i = 1; i <= size; i++){
                ImageProcessor ip = stack.getProcessor(i);
                if (keepOriginal)
                   ip = ip.crop();
                counterStack.addSlice(stack.getSliceLabel(i), ip);
            }
            counterImg = new ImagePlus("Counter Window - "+img.getTitle(), counterStack);
            counterImg.setDimensions(img.getNChannels(), img.getNSlices(), img.getNFrames());
            if (img.isComposite()) {
                counterImg = new CompositeImage(counterImg, ((CompositeImage)img).getMode());
                ((CompositeImage) counterImg).copyLuts(img);
            }
            counterImg.setOpenAsHyperStack(img.isHyperStack());
            Vector displayList = v139t?img.getCanvas().getDisplayList():null;
            ic = new CellCntrImageCanvas(counterImg,typeVector,this,displayList);
            new StackWindow(counterImg, ic);
        }
        if (!keepOriginal){
            img.changes = false;
            img.close();
        }
        delCheck.setEnabled(true);
        numbersCheck.setEnabled(true);
        showAllCheck.setSelected(false);
        if (counterImg.getStackSize()>1)
            showAllCheck.setEnabled(true);
        addButton.setEnabled(true);
        removeButton.setEnabled(true);
        resultsButton.setEnabled(true);
        deleteButton.setEnabled(true);
        resetButton.setEnabled(true);
        if (isJava14) exportButton.setEnabled(true);
        exportimgButton.setEnabled(true);
        measureButton.setEnabled(true);
    }
    
    void validateLayout(){
        dynPanel.validate();
        dynButtonPanel.validate();
        dynTxtPanel.validate();
        statButtonPanel.validate();
        validate();
        pack();
    }
    
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        
        if (command.compareTo(ADD) == 0) {
            int i = dynRadioVector.size() + 1;
            dynGrid.setRows(i);
            dynButtonPanel.add(makeDynRadioButton(i));
            validateLayout();
            
            if (ic != null)
                ic.setTypeVector(typeVector);
        } else if (command.compareTo(REMOVE) == 0) {
            if (dynRadioVector.size() > 1) {
                JRadioButton rbutton = (JRadioButton)dynRadioVector.lastElement();
                dynButtonPanel.remove(rbutton);
                radioGrp.remove(rbutton);
                dynRadioVector.removeElementAt(dynRadioVector.size() - 1);
                dynGrid.setRows(dynRadioVector.size());
            }
            if (txtFieldVector.size() > 1) {
                JTextField field = (JTextField)txtFieldVector.lastElement();
                dynTxtPanel.remove(field);
                txtFieldVector.removeElementAt(txtFieldVector.size() - 1);
            }
            if (typeVector.size() > 1) {
                typeVector.removeElementAt(typeVector.size() - 1);
            }
            validateLayout();
            
            if (ic != null)
                ic.setTypeVector(typeVector);
        } else if (command.compareTo(INITIALIZE) == 0){
            initializeImage();
        } else if (command.startsWith("Type")){ //COUNT
            if (ic == null){
                IJ.error("You need to initialize first");
                return;
            }
            int index = Integer.parseInt(command.substring(command.indexOf(" ")+1,command.length()));
            //ic.setDelmode(false); // just in case
            currentMarkerVector = (CellCntrMarkerVector)typeVector.get(index-1);
            ic.setCurrentMarkerVector(currentMarkerVector);
        } else if (command.compareTo(DELETE) == 0){
            ic.removeLastMarker();
        } else if (command.compareTo(RESET) == 0){
            reset();
        } else if (command.compareTo(RESULTS) == 0){
            report();
        }else if (command.compareTo(EXPORTMARKERS) == 0){
            exportMarkers();
        }else if (command.compareTo(LOADMARKERS) == 0){
            if (ic == null)
                initializeImage();
            loadMarkers();
            validateLayout();
        }else if (command.compareTo(EXPORTIMG) == 0){
            ic.imageWithMarkers().show();
        }else if (command.compareTo(MEASURE) == 0){
            measure();
        }
        if (ic!=null)
            ic.repaint();
        populateTxtFields();
    }
    
    public void itemStateChanged(ItemEvent e){
        if (e.getItem().equals(delCheck)){
            if (e.getStateChange()==ItemEvent.SELECTED){
                ic.setDelmode(true);
            }else{
                ic.setDelmode(false);
            }
        }else if (e.getItem().equals(newCheck)){
            if (e.getStateChange()==ItemEvent.SELECTED){
                keepOriginal=true;
            }else{
                keepOriginal=false;
            }
        }else if (e.getItem().equals(numbersCheck)){
            if (e.getStateChange()==ItemEvent.SELECTED){
                ic.setShowNumbers(true);
            }else{
                ic.setShowNumbers(false);
            }
            ic.repaint();
        }else if (e.getItem().equals(showAllCheck)){
            if (e.getStateChange()==ItemEvent.SELECTED){
                ic.setShowAll(true);
            }else{
                ic.setShowAll(false);
            }
            ic.repaint();
        }
    }
    
     public void measure(){
        ic.measure();
    }
     
    public void reset(){
        if (typeVector.size()<1){
            return;
        }
        ListIterator mit = typeVector.listIterator();
        while (mit.hasNext()){
            CellCntrMarkerVector mv = (CellCntrMarkerVector)mit.next();
            mv.clear();
        }
        if (ic!=null)
            ic.repaint();
    }
    
    public void report(){
        String labels = "Slice\t";
        boolean isStack = counterImg.getStackSize()>1;
        //add the types according to the button vector!!!!
        ListIterator it = dynRadioVector.listIterator();
        while (it.hasNext()){
            JRadioButton button = (JRadioButton)it.next();
            String str = button.getText(); //System.out.println(str);
            labels = labels.concat(str+"\t");
        }
        IJ.setColumnHeadings(labels);
        String results = "";
        if (isStack){
            for (int slice=1; slice<=counterImg.getStackSize(); slice++){
                results="";
                ListIterator mit = typeVector.listIterator();
                int types = typeVector.size();
                int[] typeTotals = new int[types];
                while (mit.hasNext()){
                    int type = mit.nextIndex();
                    CellCntrMarkerVector mv = (CellCntrMarkerVector)mit.next();
                    ListIterator tit = mv.listIterator();
                    while(tit.hasNext()){
                        CellCntrMarker m = (CellCntrMarker)tit.next();
                        if (m.getZ() == slice){
                            typeTotals[type]++;
                        }
                    }
                }
                results=results.concat(slice+"\t");
                for(int i=0; i<typeTotals.length;i++){
                    results = results.concat(typeTotals[i]+"\t");
                }
                IJ.write(results);
            }
            IJ.write("");
        }
        results = "Total\t";
        ListIterator mit = typeVector.listIterator();
        while (mit.hasNext()){
            CellCntrMarkerVector mv = (CellCntrMarkerVector)mit.next();
            int count = mv.size();
            results = results.concat(count+"\t");
        }
        IJ.write(results);
    }
    
    public void loadMarkers(){
        String filePath = getFilePath(new JFrame(), "Select Marker File", OPEN);
        ReadXML rxml = new ReadXML(filePath);
        String storedfilename = rxml.readImgProperties(rxml.IMAGE_FILE_PATH);
        if(storedfilename.equals(img.getTitle())){
            Vector loadedvector = rxml.readMarkerData();
            typeVector = loadedvector;
            ic.setTypeVector(typeVector);
            int index = Integer.parseInt(rxml.readImgProperties(rxml.CURRENT_TYPE));
            currentMarkerVector = (CellCntrMarkerVector)typeVector.get(index); 
            ic.setCurrentMarkerVector(currentMarkerVector);
            
            while(dynRadioVector.size()>typeVector.size()){
                if (dynRadioVector.size() > 1) {
                    JRadioButton rbutton = (JRadioButton)dynRadioVector.lastElement();
                    dynButtonPanel.remove(rbutton);
                    radioGrp.remove(rbutton);
                    dynRadioVector.removeElementAt(dynRadioVector.size() - 1);
                    dynGrid.setRows(dynRadioVector.size());
                }
                if (txtFieldVector.size() > 1) {
                    JTextField field = (JTextField)txtFieldVector.lastElement();
                    dynTxtPanel.remove(field);
                    txtFieldVector.removeElementAt(txtFieldVector.size() - 1);
                }
            }
            JRadioButton butt = (JRadioButton)(dynRadioVector.get(index));
            butt.setSelected(true);
            
        }else{
            IJ.error("These Markers do not belong to the current image");
        }
    }
    
    public void exportMarkers(){
        String filePath = getFilePath(new JFrame(), "Save Marker File (.xml)", SAVE);
        if (!filePath.endsWith(".xml"))
            filePath+=".xml";
        WriteXML wxml = new WriteXML(filePath);
        wxml.writeXML(img.getTitle(), typeVector, typeVector.indexOf(currentMarkerVector));
    }
    
    public static final int SAVE=FileDialog.SAVE, OPEN=FileDialog.LOAD;
    private String getFilePath(JFrame parent, String dialogMessage, int dialogType){
        switch(dialogType){
            case(SAVE):
                dialogMessage = "Save "+dialogMessage;
                break;
            case(OPEN):
                dialogMessage = "Open "+dialogMessage;
                break;
        }
        FileDialog fd ;
        String[] filePathComponents = new String[2];
        int PATH = 0;
        int FILE = 1;
        fd = new FileDialog(parent, dialogMessage, dialogType);
        switch(dialogType){
            case(SAVE):
                String filename = img.getTitle();
                fd.setFile("CellCounter_"+filename.substring(0,filename.lastIndexOf(".")+1)+"xml");
                break;
        }
        fd.setVisible(true);
        filePathComponents[PATH] = fd.getDirectory();
        filePathComponents[FILE] = fd.getFile();
        return filePathComponents[PATH]+filePathComponents[FILE];
    }
    
    public Vector getButtonVector() {
        return dynRadioVector;
    }
    
    public void setButtonVector(Vector buttonVector) {
        this.dynRadioVector = buttonVector;
    }
    
    public CellCntrMarkerVector getCurrentMarkerVector() {
        return currentMarkerVector;
    }
    
    public void setCurrentMarkerVector(CellCntrMarkerVector currentMarkerVector) {
        this.currentMarkerVector = currentMarkerVector;
    }
    
    public static void setType(String type) {
		if (instance==null || instance.ic==null || type==null)
    		return;
    	int index = Integer.parseInt(type)-1;
    	int buttons = instance.dynRadioVector.size();
    	if (index<0 || index>=buttons)
    		return;
    	JRadioButton rbutton = (JRadioButton)instance.dynRadioVector.elementAt(index);
    	instance.radioGrp.setSelected(rbutton.getModel(), true);
		instance.currentMarkerVector = (CellCntrMarkerVector)instance.typeVector.get(index);
		instance.ic.setCurrentMarkerVector(instance.currentMarkerVector);
    }
    
}
