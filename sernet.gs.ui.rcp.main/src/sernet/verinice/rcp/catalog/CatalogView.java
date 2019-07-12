/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 * Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 * Urs Zeidler uz[at]sernet.de - initial API and implementation
 * Alexander Ben Nasrallah an[at]sernet.de - Implementation
 * Daniel Murygin - Implementation
 ******************************************************************************/

package sernet.verinice.rcp.catalog;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorMultiPage;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.editors.EditorRegistry;
import sernet.gs.ui.rcp.main.bsi.views.BSIModelViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.bp.rcp.BaseProtectionTreeComparator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.iso27k.rcp.Messages;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.DeleteSelectionAction;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.catalog.ICatalogModelListener;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.tree.ElementManager;

/**
 * The CatalogView shows catalog elements in a tree view. A catalog consists of
 * any elements. All elements in a catalog are immutable. Elements in a catalog
 * are templates for the elements in other views.
 * 
 * @author Urs Zeidler uz[at]sernet.de
 * @author Daniel Murygin
 */
public class CatalogView extends RightsEnabledView implements ILinkedWithEditorView {

    private static final Logger logger = Logger.getLogger(CatalogView.class);

    protected TreeViewer viewer;
    private TreeContentProvider contentProvider;
    private ElementManager elementManager;
    private Lock lock = new ReentrantLock();
    private IModelLoadListener modelLoadListener;
    private ICatalogModelListener modelUpdateListener;
    private DrillDownAdapter drillDownAdapter;
    private Action doubleClickAction;
    private ExpandAction expandAction;
    private CollapseAction collapseAction;
    private ShowAccessControlEditAction accessControlEditAction;
    private Action linkWithEditorAction;
    private DeleteSelectionAction deleteAction;
    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);
    private BSIModelViewLabelProvider bsiLableProvider = new BSIModelViewLabelProvider();

    private boolean linkingActive;

    private Action expandAllAction;

    private Action collapseAllAction;

    public static final String ID = "sernet.verinice.rcp.catalog.CatalogView"; //$NON-NLS-1$

    public CatalogView() {
        super();
        elementManager = new ElementManager();
    }

    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            logger.error("Error while creating organization view", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ISMView_2);
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.CATALOGVIEW;
    }

    @Override
    public String getViewId() {
        return ID;
    }

    protected void initView(Composite parent) {
        contentProvider = new TreeContentProvider(elementManager);
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.setComparator(new BaseProtectionTreeComparator());
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new TreeLabelProvider() {
            @Override
            public Image getImage(Object obj) {
                if (obj instanceof IBSIStrukturElement || obj instanceof IBSIStrukturKategorie) {
                    return bsiLableProvider.getImage(obj);
                }
                return super.getImage(obj);
            }

            @Override
            public String getText(Object obj) {
                if (obj instanceof IBSIStrukturElement || obj instanceof IBSIStrukturKategorie) {
                    return bsiLableProvider.getText(obj);
                }
                return super.getText(obj);
            }
        });

        toggleLinking(Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.LINK_TO_EDITOR));
        getSite().setSelectionProvider(viewer);
        hookContextMenu();
        makeActions();
        addActions();
        fillToolBar();
        addDndListeners();

        getSite().getPage().addPartListener(linkWithEditorPartListener);
        viewer.refresh(true);
    }

    private void addDndListeners() {
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { BaseProtectionModelingTransfer.getInstance() };
        viewer.addDragSupport(ops, transfers, new CatalogDragListener(viewer));
    }

    protected void startInitDataJob() {
        if (logger.isDebugEnabled()) {
            logger.debug("Catalogview: startInitDataJob"); //$NON-NLS-1$
        }
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    logger.error("Error while loading data for catalog view.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ISMView_4, //$NON-NLS-1$
                            e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    protected void initData() {
        if (logger.isDebugEnabled()) {
            logger.debug("MotITBPCatalogVIEW: initData"); //$NON-NLS-1$
        }
        lock.lock();
        try {
            if (CnAElementFactory.isModernizedBpCatalogLoaded()) {
                if (modelUpdateListener == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating modelUpdateListener for MotITBPView."); //$NON-NLS-1$
                    }
                    modelUpdateListener = new TreeUpdateListener(viewer, elementManager);
                    CnAElementFactory.getInstance().getCatalogModel()
                            .addCatalogModelListener(modelUpdateListener);//
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            setInput(CnAElementFactory.getInstance().getCatalogModel());
                            viewer.refresh();
                        }
                    });
                }
            } else if (modelLoadListener == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("CatalogModel No model loaded, adding model load listener."); //$NON-NLS-1$
                }
                // model is not loaded yet: add a listener to load data when
                // it's loaded
                modelLoadListener = new DefaultModelLoadListener() {
                    @Override
                    public void loaded(CatalogModel model) {
                        startInitDataJob();
                    }
                };
                CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the input to the viewer.
     */
    public void setInput(CatalogModel catalogModel) {
        viewer.setInput(catalogModel);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        MenuListener menuListener = new MenuListener();
        menuMgr.addMenuListener(menuListener);
        viewer.addSelectionChangedListener(menuListener);
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    Object sel = ((IStructuredSelection) selection).getFirstElement();
                    try {
                        openEditorReadOnly(sel);
                    } catch (PartInitException e) {
                        logger.error("Error opening the BSIElement editor for: " + sel, e); //$NON-NLS-1$
                    }
                }
            }
        };
        doubleClickAction.setText(Messages.CatalogView_open_in_editor);
        doubleClickAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.OPEN_EDIT));

        makeExpandAndCollapseActions();

        linkWithEditorAction = new Action(Messages.ISMView_5, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction.setChecked(isLinkingActive());
        linkWithEditorAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));

        deleteAction = new DeleteSelectionAction();
        // We have to bind key for custom actions by hand.
        viewer.getTree().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.DEL && viewer.getTree().getSelectionCount() == 1) {
                    deleteAction.run();
                }
            }
        });

        accessControlEditAction = new ShowAccessControlEditAction(
                getViewSite().getWorkbenchWindow(), Messages.CatalogView_AccessControl);
    }

    private void makeExpandAndCollapseActions() {
        expandAction = new ExpandAction(viewer, contentProvider);
        expandAction.setText(Messages.ISMView_7);
        expandAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAction = new CollapseAction(viewer);
        collapseAction.setText(Messages.ISMView_8);
        collapseAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

        expandAllAction = new Action() {
            @Override
            public void run() {
                viewer.expandAll();
            }
        };
        expandAllAction.setText(Messages.ISMView_9);
        expandAllAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAllAction = new Action() {
            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText(Messages.ISMView_10);
        collapseAllAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
    }

    protected void openEditorReadOnly(Object sel) throws PartInitException {
        CnATreeElement element = (CnATreeElement) sel;
        IEditorPart editor = EditorRegistry.getInstance().getOpenEditor(element.getId());
        if (editor == null) {
            EditorFactory.getInstance().updateAndOpenObject(element, true);
        } else {
            getSite().getPage().openEditor(editor.getEditorInput(),
                    BSIElementEditorMultiPage.EDITOR_ID);
        }
    }

    private void addActions() {
        viewer.addDoubleClickListener(event -> doubleClickAction.run());

        viewer.addSelectionChangedListener(expandAction);
        viewer.addSelectionChangedListener(collapseAction);
        viewer.addSelectionChangedListener(deleteAction);
    }

    protected void fillToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        drillDownAdapter.addNavigationActions(manager);
        manager.add(linkWithEditorAction);
    }

    protected void toggleLinking(boolean checked) {
        this.linkingActive = checked;
        if (checked) {
            Optional.ofNullable(getSite().getPage().getActiveEditor())
                    .ifPresent(this::editorActivated);
        }
    }

    protected boolean isLinkingActive() {
        return linkingActive;
    }

    @Override
    public void editorActivated(IEditorPart activeEditor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(activeEditor);

        if (logger.isDebugEnabled()) {
            logger.debug("Element in editor: " + element.getUuid()); //$NON-NLS-1$
        }
        if (element != null) {
            viewer.setSelection(new StructuredSelection(element), true);
        } else {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Tree is expanded."); //$NON-NLS-1$
        }
    }

    @Override
    public void dispose() {
        elementManager.clearCache();
        if (CnAElementFactory.isModernizedBpCatalogLoaded()) {
            CnAElementFactory.getInstance().getCatalogModel()
                    .removeCatalogModelListener(modelUpdateListener);
        }
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
    }

    private class MenuListener implements IMenuListener, ISelectionChangedListener {

        private IStructuredSelection selection;

        @Override
        public void menuAboutToShow(IMenuManager manager) {
            manager.add(new GroupMarker("content")); //$NON-NLS-1$
            manager.add(new Separator());
            manager.add(doubleClickAction);
            manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new Separator());
            // Create a "Copy" context menu entry. Usually, we would do that
            // declaratively in the plugin.xml file. To be able to do that, we
            // would have to register the context menu with the workbench, but
            // that would add the menu entries from the CnATreeElement
            // objectContributions (such as change_icon), and we don't want
            // those in the catalog view.
            CommandContributionItemParameter copyParameter = new CommandContributionItemParameter(
                    PlatformUI.getWorkbench(), null, IWorkbenchCommandConstants.EDIT_COPY,
                    CommandContributionItem.STYLE_PUSH);
            copyParameter.icon = ImageCache.getInstance().getImageDescriptor(ImageCache.COPY);
            CommandContributionItem copyItem = new CommandContributionItem(copyParameter);
            manager.add(copyItem);

            CommandContributionItemParameter copyWithLinksParameter = new CommandContributionItemParameter(
                    PlatformUI.getWorkbench(), null,
                    "sernet.verinice.iso27k.rcp.commands.copywithlinks",
                    CommandContributionItem.STYLE_PUSH);
            copyWithLinksParameter.icon = ImageCache.getInstance()
                    .getImageDescriptor(ImageCache.COPY);
            CommandContributionItem copyWithLinksItem = new CommandContributionItem(
                    copyWithLinksParameter);
            manager.add(copyWithLinksItem);

            if (CnAElementFactory.selectionOnlyContainsScopes((IStructuredSelection) selection)) {
                manager.add(deleteAction);
            }
            manager.add(accessControlEditAction);
            manager.add(new Separator());
            manager.add(expandAction);
            manager.add(collapseAction);
            drillDownAdapter.addNavigationActions(manager);
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            selection = (IStructuredSelection) event.getSelection();
        }
    }

}
