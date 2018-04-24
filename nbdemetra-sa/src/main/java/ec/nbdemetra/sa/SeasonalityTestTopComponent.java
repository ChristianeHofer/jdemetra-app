/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.sa;

import ec.nbdemetra.ui.ActiveViewManager;
import ec.nbdemetra.ui.ComponentFactory;
import ec.nbdemetra.ui.IActiveView;
import ec.tss.Ts;
import ec.tss.TsInformationType;
import ec.tss.TsStatus;
import ec.tss.html.HtmlUtil;
import ec.tss.html.implementation.HtmlSeasonalityDiagnostics;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.AHtmlView;
import ec.ui.ATsChart;
import ec.ui.chart.JTsChart;
import ec.ui.interfaces.ITsAble;
import ec.ui.interfaces.ITsCollectionView.TsUpdateMode;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ec.nbdemetra.sa//SeasonalityTest//EN",
        autostore = false)
@TopComponent.Description(preferredID = "SeasonalityTestTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@ActionID(category = "Tool", id = "ec.nbdemetra.sa.SeasonalityTestTopComponent")
@ActionReference(path = "Menu/Statistical methods/Seasonal Adjustment/Tools", position = 333)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SeasonalityTestAction",
        preferredID = "SeasonalityTestTopComponent")
@Messages({
    "CTL_SeasonalityTestAction=Seasonality Tests",
    "CTL_SeasonalityTestTopComponent=Seasonality Tests Window",
    "HINT_SeasonalityTestTopComponent=This is a Seasonality Tests window"
})
public final class SeasonalityTestTopComponent extends TopComponent implements ITsAble, IActiveView, ExplorerManager.Provider {

    private Node node;
    private boolean isLog = false;
    private int diffOrder = 1;
    private int lastYears = 0;

    private final ATsChart jTsChart1;
    private final AHtmlView jEditorPane1;
    
    public SeasonalityTestTopComponent() {
        initComponents();
        setName(Bundle.CTL_SeasonalityTestTopComponent());
        setToolTipText(Bundle.HINT_SeasonalityTestTopComponent());
        
        jTsChart1 = ComponentFactory.getDefault().newTsChart();
        jTsChart1.setTsUpdateMode(TsUpdateMode.Single);

        jEditorPane1 = ComponentFactory.getDefault().newHtmlView();

        node = new InternalNode();
        jTsChart1.addPropertyChangeListener(JTsChart.TS_COLLECTION_PROPERTY, evt -> showTests());
        
        jSplitPane1.setTopComponent(jTsChart1);
        jSplitPane1.setBottomComponent(jEditorPane1);
        
        associateLookup(ExplorerUtils.createLookup(ActiveViewManager.getInstance().getExplorerManager(), getActionMap()));
    }

//    @Override
//    public void open() {
//        super.open();
//        Mode mode = WindowManager.getDefault().findMode("properties");
//        if (mode != null && mode.canDock(this)) {
//            mode.dockInto(this);
//        }
//    }
    @Override
    public void componentOpened() {
        super.componentOpened();
        this.jTsChart1.connect();
        SwingUtilities.invokeLater(() -> {
            jSplitPane1.setDividerLocation(.3);
            jSplitPane1.setResizeWeight(.3);
        });
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        this.jTsChart1.dispose();
    }

    @Override
    public void componentActivated() {
        ActiveViewManager.getInstance().set(this);
    }

    @Override
    public void componentDeactivated() {
        ActiveViewManager.getInstance().set(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    private void showTests() {
        Ts cur = getTs();
        if (cur == null) {
            jEditorPane1.loadContent("");
        } else {
            test(cur);
        }
    }

    private void test(Ts cur) {
        if (cur.hasData() == TsStatus.Undefined) {
            cur.load(TsInformationType.Data);
        }
        TsData s = cur.getTsData();
        if (s == null) {
            return;
        }
        if (lastYears > 0) {
            int nmax = lastYears * s.getFrequency().intValue();
            int nbeg = s.getLength() - nmax;
            nbeg-=diffOrder;
            if (nbeg > 0) {
                s = s.drop(nbeg,0);
            }
        }

        if (isLog) {
            s = s.log();
        }
        SeasonalityTests tests = SeasonalityTests.seasonalityTest(s, diffOrder, diffOrder <= 1, true);
        HtmlSeasonalityDiagnostics html = new HtmlSeasonalityDiagnostics(tests);
        jEditorPane1.loadContent(HtmlUtil.toString(html));
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public Ts getTs() {
        if (jTsChart1.getTsCollection().isEmpty()) {
            return null;
        }
        return jTsChart1.getTsCollection().get(0);
    }

    @Override
    public void setTs(Ts ts) {
        jTsChart1.getTsCollection().replace(ts);
        //test(ts);
    }

    @Override
    public boolean fill(JMenu menu) {
        return false;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return ActiveViewManager.getInstance().getExplorerManager();
    }

    @Override
    public boolean hasContextMenu() {
        return false;
    }

    class InternalNode extends AbstractNode {
        @Messages({
            "seasonalityTestTopComponent.internalNode.displayName=Seasonality tests"
        })
        InternalNode() {
            super(Children.LEAF);
            setDisplayName(Bundle.seasonalityTestTopComponent_internalNode_displayName());
        }

        @Override
        @Messages({
            "seasonalityTestTopComponent.transform.name=Transform",
            "seasonalityTestTopComponent.transform.displayName=Transformation",
            "seasonalityTestTopComponent.log.name=Log",
            "seasonalityTestTopComponent.log.desc=When marked, logarithmic transformation is used.",
            "seasonalityTestTopComponent.differencing.name=Differencing",
            "seasonalityTestTopComponent.differencing.desc=An order of a regular differencing of the series.",
            "seasonalityTestTopComponent.lastYears.name=Last years",
            "seasonalityTestTopComponent.lastYears.desc=Number of years at the end of the series taken into account (0 = whole series)."
        })
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Set transform = Sheet.createPropertiesSet();
            transform.setName(Bundle.seasonalityTestTopComponent_transform_name());
            transform.setDisplayName(Bundle.seasonalityTestTopComponent_transform_displayName());
            Property<Boolean> log = new Property(Boolean.class) {
                @Override
                public boolean canRead() {
                    return true;
                }

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return isLog;
                }

                @Override
                public boolean canWrite() {
                    return true;
                }

                @Override
                public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    isLog = (Boolean) t;
                    showTests();
                }
            };

            log.setName(Bundle.seasonalityTestTopComponent_log_name());
            log.setShortDescription(Bundle.seasonalityTestTopComponent_log_desc());
            transform.put(log);
            Property<Integer> diff = new Property(Integer.class) {
                @Override
                public boolean canRead() {
                    return true;
                }

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return diffOrder;
                }

                @Override
                public boolean canWrite() {
                    return true;
                }

                @Override
                public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    diffOrder = (Integer) t;
                    showTests();
                }
            };

            diff.setName(Bundle.seasonalityTestTopComponent_differencing_name());
            diff.setShortDescription(Bundle.seasonalityTestTopComponent_differencing_desc());
            transform.put(diff);
            Node.Property<Integer> length = new Node.Property(Integer.class) {
                @Override
                public boolean canRead() {
                    return true;
                }

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return lastYears;
                }

                @Override
                public boolean canWrite() {
                    return true;
                }

                @Override
                public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    lastYears = (Integer) t;
                    showTests();
                }
            };
            length.setName(Bundle.seasonalityTestTopComponent_lastYears_name());
            length.setShortDescription(Bundle.seasonalityTestTopComponent_lastYears_desc());
            transform.put(length);
            sheet.put(transform);
            return sheet;
        }
    }
}
