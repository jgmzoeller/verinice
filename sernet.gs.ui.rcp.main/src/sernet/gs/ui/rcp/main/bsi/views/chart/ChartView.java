/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.NullListener;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * Displays charts to visualize progress and other data.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ChartView extends ViewPart {

    public static final String ID = "sernet.gs.ui.rcp.main.chartview"; //$NON-NLS-1$
    private static final Logger LOG = Logger.getLogger(ChartView.class);

    private IModelLoadListener loadListener;

    private ChangeListener changeListener;

    private Composite parent;

    private ChartComposite frame;

    private Action chooseBarDiagramAction;

    private IChartGenerator currentChartGenerator;

    private Emptychart emptyChart;

    private MaturitySpiderChart maturitySpiderChart;

    protected SamtProgressChart samtProgressChart;

    private Action chooseMaturityDiagramAction;

    private ISelectionListener selectionListener;

    // use getElement()!
    protected CnATreeElement element = null;

    private Action chooseMaturityBarDiagramAction;

    private MaturityBarChart maturityBarChart;

    @Override
    public void createPartControl(Composite parent) {
        initView(parent);
        startInitDataJob();
    }

    private void initView(Composite parent) {
        this.parent = parent;
        frame = new ChartComposite(parent, SWT.NONE, null, true);
        createChartGenerators();
        createSelectionListeners();
        hookSelectionListeners();
        hookPageSelection();
        createMenus();
        setDescription();

        currentChartGenerator = getDefaultChartGenerator();
    }

    protected void setDescription() {
        this.setContentDescription(Messages.ChartView_0);
    }

    protected synchronized void startInitDataJob() {
        if (CnAElementFactory.isModelLoaded()) {
            JobScheduler.scheduleInitJob(createDrawChartJob());
        }
        // if models are not loaded yet: loadListener does this job
    }

    protected IChartGenerator getDefaultChartGenerator() {
        return samtProgressChart;
    }

    private void createChartGenerators() {
        emptyChart = new Emptychart();
        maturitySpiderChart = new MaturitySpiderChart();
        maturityBarChart = new MaturityBarChart();
        samtProgressChart = new SamtProgressChart();

    }

    protected void createMenus() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        chooseBarDiagramAction = new Action(Messages.ChartView_1, SWT.CHECK) {
            @Override
            public void run() {
                currentChartGenerator = samtProgressChart;
                drawChart();
            }
        };
        chooseBarDiagramAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.CHART_BAR));

        chooseMaturityDiagramAction = new Action(Messages.ChartView_7, SWT.CHECK) {
            @Override
            public void run() {
                currentChartGenerator = maturitySpiderChart;
                drawChart();
                setContentDescription(Messages.ChartView_8);
            }
        };
        chooseMaturityDiagramAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.CHART_PIE));

        chooseMaturityBarDiagramAction = new Action(Messages.ChartView_9, SWT.CHECK) {
            @Override
            public void run() {
                currentChartGenerator = maturityBarChart;
                drawChart();
                setContentDescription(Messages.ChartView_10);
            }
        };
        chooseMaturityBarDiagramAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.CHART_BAR));

        menuManager.add(chooseBarDiagramAction);
        menuManager.add(new Separator());
        menuManager.add(chooseMaturityDiagramAction);
        menuManager.add(chooseMaturityBarDiagramAction);

        fillLocalToolBar();
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(chooseBarDiagramAction);
        manager.add(new Separator());
        manager.add(chooseMaturityDiagramAction);
        manager.add(chooseMaturityBarDiagramAction);
    }

    protected synchronized void drawChart() {
        createDrawChartJob().schedule();
    }

    private WorkspaceJob createDrawChartJob() {
        return new WorkspaceJob(Messages.ChartView_6) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                if (parent != null && !parent.isDisposed() && frame != null
                        && !frame.isDisposed()) {
                    final JFreeChart chart;
                    CnATreeElement currentElement = null;
                    if (currentChartGenerator instanceof ISelectionChartGenerator) {
                        currentElement = getElement();
                        if (currentElement == null) {
                            currentElement = getDefaultElement();
                        }
                        chart = ((ISelectionChartGenerator) currentChartGenerator)
                                .createChart(currentElement);
                    } else {
                        chart = currentChartGenerator.createChart();
                    }
                    if (chart != null) {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                try {
                                    if (frame.isDisposed()) {
                                        return;
                                    }
                                    frame.setChart(chart);
                                    frame.forceRedraw();
                                } catch (Exception e) {
                                    // chart disposed:
                                    LOG.error(e);
                                }
                            }
                        });
                    }
                }
                return Status.OK_STATUS;
            }
        };
    }

    private void createSelectionListeners() {
        loadListener = new IModelLoadListener() {

            public void closed(BSIModel model) {
                Display.getDefault().asyncExec(() -> {
                    currentChartGenerator = emptyChart;
                    drawChart();
                });
            }

            public void loaded(final BSIModel model) {
                Display.getDefault().asyncExec(() -> CnAElementFactory.getLoadedModel()
                        .addBSIModelListener(changeListener));
            }

            public void loaded(ISO27KModel model) {
                Display.getDefault().asyncExec(() -> {
                    CnAElementFactory.getInstance().getISO27kModel()
                            .addISO27KModelListener(changeListener);
                    currentChartGenerator = getDefaultChartGenerator();
                    drawChart();
                });

            }

            @Override
            public void loaded(BpModel model) {
                Display.getDefault().asyncExec(() -> CnAElementFactory.getInstance().getBpModel()
                        .addModITBOModelListener(changeListener));
            }

            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        };

        changeListener = createChangeListener();
    }

    protected ChangeListener createChangeListener() {
        return new ChangeListener();
    }

    private void hookPageSelection() {
        selectionListener = this::pageSelectionChanged;
        getSite().getPage().addPostSelectionListener(selectionListener);
    }

    /**
     * Method is called if the selection in the GUI is changed.
     * 
     * @param part
     * @param selection
     *            the newly selected GUI element
     */
    protected synchronized void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        Object firstSelection = ((IStructuredSelection) selection).getFirstElement();
        CnATreeElement selectedElement = (CnATreeElement) firstSelection;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Selection changed, selected element: " + selectedElement); //$NON-NLS-1$
        }

        if (firstSelection instanceof CnATreeElement) {
            if (this.element != null && selectedElement == this.element) {
                return;
            }
            this.element = selectedElement;
            drawChart();
        }
    }

    protected void hookSelectionListeners() {
        CnAElementFactory.getInstance().addLoadListener(loadListener);
        if (CnAElementFactory.getLoadedModel() != null) {
            CnAElementFactory.getLoadedModel().addBSIModelListener(changeListener);
        }
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        getSite().getPage().removePostSelectionListener(selectionListener);
        CnAElementFactory.getInstance().removeLoadListener(loadListener);
        if (CnAElementFactory.getLoadedModel() != null) {
            CnAElementFactory.getLoadedModel().removeBSIModelListener(changeListener);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Disposing chart view " + this); //$NON-NLS-1$
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        frame.setFocus();
    }

    /**
     * Currently selected element for which a chart is generated
     * 
     * @return currently selected element or null if nothing is selected
     */
    protected CnATreeElement getElement() {
        return element;
    }

    /**
     * The default element for which a chart is generated or null
     * 
     * @return the default element or null
     */
    protected CnATreeElement getDefaultElement() {
        return null;
    }

    protected class ChangeListener extends NullListener
            implements IBSIModelListener, IISO27KModelListener, IBpModelListener {

        @Override
        public void modelRefresh(Object source) {
            drawChart();
        }

        @Override
        public void modelReload(BpModel newModel) {
            // do nothing
        }

        @Override
        public void modelReload(ISO27KModel newModel) {
            // do nothing
        }

    }

}
